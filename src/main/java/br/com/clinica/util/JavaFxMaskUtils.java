package br.com.clinica.util;

import javafx.scene.control.TextField;

public class JavaFxMaskUtils {

    private JavaFxMaskUtils() {
    }

    public static void aplicarMascaraCpf(TextField field) {

        field.textProperty().addListener((obs, oldValue, newValue) -> {

            String texto = newValue.replaceAll("\\D", "");

            if (texto.length() > 11) {
                texto = texto.substring(0, 11);
            }

            StringBuilder formatado = new StringBuilder();

            int i = 0;

            for (char c : texto.toCharArray()) {

                if (i == 3 || i == 6) {
                    formatado.append(".");
                }

                if (i == 9) {
                    formatado.append("-");
                }

                formatado.append(c);

                i++;
            }

            if (!newValue.equals(formatado.toString())) {
                field.setText(formatado.toString());
            }
        });
    }

    public static void aplicarMascaraTelefone(TextField field) {

        field.textProperty().addListener((obs, oldValue, newValue) -> {

            String texto = newValue.replaceAll("\\D", "");

            if (texto.length() > 11) {
                texto = texto.substring(0, 11);
            }

            StringBuilder formatado = new StringBuilder();

            if (texto.length() >= 1) {
                formatado.append("(");
            }

            for (int i = 0; i < texto.length(); i++) {

                if (i == 2) {
                    formatado.append(") ");
                }

                if (i == 7) {
                    formatado.append("-");
                }

                formatado.append(texto.charAt(i));
            }

            if (!newValue.equals(formatado.toString())) {
                field.setText(formatado.toString());
            }
        });
    }
}