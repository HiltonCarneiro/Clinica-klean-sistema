package br.com.clinica.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class AnexoStorageService {

    public static Path getBaseDir() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "Documents", "ClinicaIntegracao", "anexos");
    }

    public static Path salvarPdfParaPaciente(Long pacienteId, File arquivoOriginal) throws Exception {
        Path pastaPaciente = getBaseDir().resolve(String.valueOf(pacienteId));
        Files.createDirectories(pastaPaciente);

        String safeName = arquivoOriginal.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
        String finalName = System.currentTimeMillis() + "_" + safeName;

        Path destino = pastaPaciente.resolve(finalName);
        Files.copy(arquivoOriginal.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        return destino;
    }

    // =========================
    // NOVO: evolução em arquivo TXT (editável)
    // =========================

    public static Path salvarTextoParaPaciente(Long pacienteId, String nomeArquivo, String conteudo) throws Exception {
        if (pacienteId == null) throw new IllegalArgumentException("pacienteId é obrigatório");
        if (nomeArquivo == null || nomeArquivo.isBlank()) throw new IllegalArgumentException("nomeArquivo é obrigatório");

        Path pastaPaciente = getBaseDir().resolve(String.valueOf(pacienteId));
        Files.createDirectories(pastaPaciente);

        String safeName = nomeArquivo.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path destino = pastaPaciente.resolve(safeName);

        Files.writeString(destino, conteudo == null ? "" : conteudo, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return destino;
    }

    public static void atualizarTexto(Path arquivoExistente, String conteudo) throws Exception {
        if (arquivoExistente == null) throw new IllegalArgumentException("arquivoExistente é obrigatório");

        Files.createDirectories(arquivoExistente.getParent());

        Files.writeString(arquivoExistente, conteudo == null ? "" : conteudo, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}