package br.com.clinica.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import java.io.InputStream;

public class SupabaseStorageService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String supabaseUrl;       // ex: https://xxxxx.supabase.co
    private final String serviceRoleKey;    // service_role key
    private final String bucket;            // anexos
    private final int signedUrlSeconds;     // 600

    public SupabaseStorageService() {
        Properties p = loadProps();
        this.supabaseUrl = req(p, "supabase.url");
        this.serviceRoleKey = req(p, "supabase.service_role_key");
        this.bucket = p.getProperty("supabase.bucket", "anexos").trim();
        this.signedUrlSeconds = parseInt(p.getProperty("supabase.signed_url_seconds", "600"), 600);
    }

    public String getBucket() {
        return bucket;
    }

    /** Upload do arquivo (PUT /storage/v1/object/{bucket}/{path}) */
    public void uploadPdf(String storagePath, Path file) throws IOException, InterruptedException {
        byte[] bytes = Files.readAllBytes(file);

        String encodedPath = encodePath(storagePath);
        URI uri = URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodedPath);

        HttpRequest req = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("apikey", serviceRoleKey)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("Content-Type", "application/pdf")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();

        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("Falha no upload (HTTP " + resp.statusCode() + "): " + resp.body());
        }
    }

    /** Gera signed URL (POST /storage/v1/object/sign/{bucket}/{path}) */
    public String createSignedUrl(String storagePath) throws IOException, InterruptedException {
        String encodedPath = encodePath(storagePath);
        URI uri = URI.create(supabaseUrl + "/storage/v1/object/sign/" + bucket + "/" + encodedPath);

        String body = "{\"expiresIn\":" + signedUrlSeconds + "}";
        HttpRequest req = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("apikey", serviceRoleKey)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("Falha ao assinar URL (HTTP " + resp.statusCode() + "): " + resp.body());
        }

        // resposta costuma vir como: {"signedURL":"..."}
        String signed = extractJsonString(resp.body(), "signedURL");
        if (signed == null || signed.isBlank()) {
            throw new IOException("Resposta sem signedURL: " + resp.body());
        }

        // Às vezes vem relativo (ex.: "/storage/v1/object/sign/..." ou "/object/sign/...")
        if (signed.startsWith("http://") || signed.startsWith("https://")) {
            return signed;
        }

        // Normaliza para sempre conter "/storage/v1" (algumas respostas vêm como "/object/sign/..." sem o prefixo)
        if (signed.startsWith("/object/")) {
            signed = "/storage/v1" + signed;
        } else if (signed.startsWith("object/")) {
            signed = "/storage/v1/" + signed;
        } else if (signed.startsWith("storage/v1/")) {
            signed = "/" + signed;
        }

        if (!signed.startsWith("/")) signed = "/" + signed;
        return supabaseUrl + signed;
    }

    /** Remove arquivo (DELETE /storage/v1/object/{bucket}/{path}) */
    public void delete(String storagePath) throws IOException, InterruptedException {
        String encodedPath = encodePath(storagePath);
        URI uri = URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodedPath);

        HttpRequest req = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("apikey", serviceRoleKey)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .DELETE()
                .build();

        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("Falha ao deletar (HTTP " + resp.statusCode() + "): " + resp.body());
        }
    }

    // ----------------- helpers -----------------

    private static Properties loadProps() {
        Properties p = new Properties();

        // 1) classpath
        try (InputStream in = SupabaseStorageService.class.getResourceAsStream("/db.properties")) {
            if (in != null) p.load(in);
        } catch (Exception ignored) {}

        // 2) arquivo local (na raiz do projeto)
        try (InputStream in = java.nio.file.Files.newInputStream(java.nio.file.Paths.get("db.properties"))) {
            p.load(in);
        } catch (Exception ignored) {}

        return p;
    }

    private static String req(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.trim().isBlank()) {
            throw new IllegalStateException("Faltando '" + key + "' no db.properties");
        }
        return v.trim();
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    // encode por segmentos (mantém "/") e evita espaço virar "+" no path
    private static String encodePath(String path) {
        String[] parts = path.split("/");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) out.append('/');
            String enc = URLEncoder.encode(parts[i], StandardCharsets.UTF_8)
                    .replace("+", "%20");
            out.append(enc);
        }
        return out.toString();
    }

    // extrator simples "chave":"valor" (sem libs)
    private static String extractJsonString(String json, String key) {
        if (json == null) return null;
        String needle = "\"" + key + "\":\"";
        int i = json.indexOf(needle);
        if (i < 0) return null;
        int start = i + needle.length();
        int end = start;
        boolean esc = false;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (esc) { esc = false; end++; continue; }
            if (c == '\\') { esc = true; end++; continue; }
            if (c == '"') break;
            end++;
        }
        if (end <= start) return null;
        String raw = json.substring(start, end);
        return raw.replace("\\/", "/").replace("\\\"", "\"");
    }
}