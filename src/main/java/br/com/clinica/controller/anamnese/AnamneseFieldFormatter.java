package br.com.clinica.controller.anamnese;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class AnamneseFieldFormatter {

    public void configurarMascarasSinaisVitais(
            TextField tfPA,
            TextField tfFC,
            TextField tfFR,
            TextField tfTemp,
            TextField tfPeso,
            TextField tfAltura,
            TextField tfSpO2
    ) {
        aplicarPAautomatico(tfPA);
        aplicarSomenteNumeros(tfFC, 3);
        aplicarSomenteNumeros(tfFR, 3);
        aplicarSomenteNumeros(tfSpO2, 3);

        aplicarDecimalTempoReal(tfTemp, 1, 2, false);
        aplicarDecimalTempoReal(tfPeso, 1, 3, false);
        aplicarDecimalTempoReal(tfAltura, 2, 1, true);
    }

    public void normalizarDecimaisAntesDeSalvar(
            TextField tfTemp,
            TextField tfPeso,
            TextField tfAltura
    ) {
        if (tfTemp != null) {
            tfTemp.setText(formatarDecimalSeSomenteDigitos(tfTemp.getText(), 1, 2));
        }

        if (tfPeso != null) {
            tfPeso.setText(formatarDecimalSeSomenteDigitos(tfPeso.getText(), 1, 3));
        }

        if (tfAltura != null) {
            tfAltura.setText(formatarDecimalSeSomenteDigitos(tfAltura.getText(), 2, 1));
        }
    }

    private void aplicarSomenteNumeros(TextField tf, int maxDigitos) {
        if (tf == null) {
            return;
        }

        tf.setTextFormatter(new TextFormatter<String>(change -> {
            String novo = change.getControlNewText();

            if (novo == null || novo.isEmpty()) {
                return change;
            }

            String digits = novo.replaceAll("\\D", "");

            if (digits.length() > maxDigitos) {
                digits = digits.substring(0, maxDigitos);
            }

            change.setText(digits);
            change.setRange(0, change.getControlText().length());
            change.selectRange(digits.length(), digits.length());

            return change;
        }));
    }

    private void aplicarPAautomatico(TextField tf) {
        if (tf == null) {
            return;
        }

        tf.setTextFormatter(new TextFormatter<String>(change -> {
            String novoTexto = change.getControlNewText();

            if (novoTexto == null || novoTexto.isEmpty()) {
                return change;
            }

            String digits = novoTexto.replaceAll("\\D", "");

            if (digits.isEmpty()) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                change.selectRange(0, 0);
                return change;
            }

            if (digits.length() > 6) {
                digits = digits.substring(0, 6);
            }

            String formatado;

            if (digits.length() <= 3) {
                formatado = digits;
            } else if (digits.length() <= 5) {
                String sis = digits.substring(0, digits.length() - 2);
                String dia = digits.substring(digits.length() - 2);
                formatado = sis + "/" + dia;
            } else {
                String sis = digits.substring(0, digits.length() - 3);
                String dia = digits.substring(digits.length() - 3);
                formatado = sis + "/" + dia;
            }

            change.setText(formatado);
            change.setRange(0, change.getControlText().length());
            change.selectRange(formatado.length(), formatado.length());

            return change;
        }));
    }

    private void aplicarDecimalTempoReal(
            TextField tf,
            int decimais,
            int maxInteiros,
            boolean modoAltura
    ) {
        if (tf == null) {
            return;
        }

        tf.setTextFormatter(new TextFormatter<String>(change -> {
            String novoTexto = change.getControlNewText();

            if (novoTexto == null || novoTexto.isEmpty()) {
                return change;
            }

            String digits = novoTexto.replaceAll("\\D", "");

            if (digits.isEmpty() || digits.matches("0+")) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                change.selectRange(0, 0);
                return change;
            }

            int maxLen = maxInteiros + decimais;

            if (digits.length() > maxLen) {
                digits = digits.substring(0, maxLen);
            }

            if (modoAltura) {
                if (digits.length() == 1) {
                    change.setText(digits);
                    change.setRange(0, change.getControlText().length());
                    change.selectRange(digits.length(), digits.length());
                    return change;
                }

                if (digits.length() == 2) {
                    String formatado = digits.charAt(0) + "," + digits.charAt(1);
                    change.setText(formatado);
                    change.setRange(0, change.getControlText().length());
                    change.selectRange(formatado.length(), formatado.length());
                    return change;
                }
            }

            String formatado = formatarDecimalPorDigitos(digits, decimais);

            change.setText(formatado);
            change.setRange(0, change.getControlText().length());
            change.selectRange(formatado.length(), formatado.length());

            return change;
        }));
    }

    private String formatarDecimalSeSomenteDigitos(
            String texto,
            int decimais,
            int maxInteiros
    ) {
        if (texto == null) {
            return "";
        }

        String t = texto.trim();

        if (t.isEmpty()) {
            return "";
        }

        if (t.contains(",")) {
            return t;
        }

        String digits = t.replaceAll("\\D", "");

        if (digits.isEmpty() || digits.matches("0+")) {
            return "";
        }

        int maxLen = maxInteiros + decimais;

        if (digits.length() > maxLen) {
            digits = digits.substring(0, maxLen);
        }

        return formatarDecimalPorDigitos(digits, decimais);
    }

    private String formatarDecimalPorDigitos(String digits, int decimais) {
        if (digits == null || digits.isEmpty()) {
            return "";
        }

        if (digits.length() <= decimais) {
            String zeros = "0".repeat(decimais - digits.length() + 1);
            digits = zeros + digits;
        }

        int split = digits.length() - decimais;

        String inteiro = digits.substring(0, split);
        String decimal = digits.substring(split);

        inteiro = inteiro.replaceFirst("^0+(?!$)", "");

        return inteiro + "," + decimal;
    }
}