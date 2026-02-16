package br.com.clinica.service;

import br.com.clinica.database.DatabaseConfig;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.prefs.Preferences;

public class BackupService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Preferences PREFS = Preferences.userRoot().node("br.com.clinica.backup");

    // Pasta padrão (pode trocar depois por uma configuração)
    private static Path pastaBackupsPadrao() {
        return Paths.get(System.getProperty("user.home"), "ClinicaIntegracao", "backups");
    }

    /** Faz backup se ainda não foi feito hoje (recomendado chamar no start da aplicação). */
    public static BackupResult backupDiarioSeNecessario() {
        String last = PREFS.get("last_backup_date", "");
        String hoje = LocalDate.now().toString();
        if (hoje.equals(last)) {
            return new BackupResult(false, null, "Backup diário já realizado hoje (" + hoje + ").");
        }

        BackupResult r = fazerBackupAgora(pastaBackupsPadrao());
        if (r.ok) {
            PREFS.put("last_backup_date", hoje);
        }
        return r;
    }

    /** Backup manual (ex: botão "Fazer backup agora"). */
    public static BackupResult fazerBackupAgora(Path pastaDestino) {
        try {
            Files.createDirectories(pastaDestino);

            String nome = "backup_" + LocalDateTime.now().format(TS) + ".db";
            Path arquivo = pastaDestino.resolve(nome);

            // ✅ Snapshot consistente usando SQLite VACUUM INTO
            // (melhor do que copiar arquivo na mão)
            try (var conn = DatabaseConfig.getConnection();
                 var stmt = conn.createStatement()) {

                // Escapa aspas simples no path
                String pathSql = arquivo.toAbsolutePath().toString().replace("'", "''");

                stmt.execute("VACUUM INTO '" + pathSql + "'");
            }

            // (Opcional) manter apenas os últimos N backups
            manterUltimos(pastaDestino, 30);

            return new BackupResult(true, arquivo, "Backup gerado com sucesso.");

        } catch (Exception e) {
            return new BackupResult(false, null, "Falha ao gerar backup: " + e.getMessage());
        }
    }

    /** Mantém apenas os N backups mais recentes na pasta (evita encher o disco). */
    private static void manterUltimos(Path pasta, int maxArquivos) throws IOException {
        try (var stream = Files.list(pasta)) {
            var arquivos = stream
                    .filter(p -> p.getFileName().toString().startsWith("backup_") && p.getFileName().toString().endsWith(".db"))
                    .sorted((a, b) -> {
                        try {
                            return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .toList();

            for (int i = maxArquivos; i < arquivos.size(); i++) {
                try { Files.deleteIfExists(arquivos.get(i)); } catch (IOException ignored) {}
            }
        }
    }

    public static class BackupResult {
        public final boolean ok;
        public final Path arquivo;
        public final String mensagem;

        public BackupResult(boolean ok, Path arquivo, String mensagem) {
            this.ok = ok;
            this.arquivo = arquivo;
            this.mensagem = mensagem;
        }
    }
}
