package br.com.clinica.controller.avaliacaofisica;

import javafx.scene.control.TextInputControl;

public final class AvaliacaoFisicaFormatter {

    private AvaliacaoFisicaFormatter() {
    }

    public static Double parseDouble(TextInputControl campo) {
        if (campo == null) {
            return null;
        }

        return parseDouble(campo.getText(), nomeCampo(campo));
    }

    public static Double parseDouble(String texto, String nomeCampo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        try {
            String normalizado = texto.trim().replace(".", "").replace(",", ".");
            return Double.parseDouble(normalizado);
        } catch (NumberFormatException e) {
            String nome = nomeCampo == null || nomeCampo.isBlank() ? "numérico" : nomeCampo;
            throw new IllegalArgumentException("Informe um número válido no campo: " + nome);
        }
    }

    public static Integer parseInteger(TextInputControl campo) {
        if (campo == null) {
            return null;
        }

        String texto = campo.getText();

        if (texto == null || texto.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe uma idade válida.");
        }
    }

    public static String valorNumero(Double valor) {
        return valor == null ? "-" : String.format("%.2f", valor);
    }

    public static String valorNumeroParaCampo(Double valor) {
        return valor == null ? "" : String.format("%.2f", valor).replace(".", ",");
    }

    public static String valorTexto(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    public static String valorTextoParaCampo(String valor) {
        return valor == null || valor.isBlank() || "-".equals(valor) ? "" : valor;
    }

    private static String nomeCampo(TextInputControl campo) {
        String nomeCampo = campo.getPromptText();
        return nomeCampo == null || nomeCampo.isBlank() ? "numérico" : nomeCampo;
    }
}
