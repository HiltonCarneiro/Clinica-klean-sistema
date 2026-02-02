package br.com.clinica.service;

import java.io.File;
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
}
