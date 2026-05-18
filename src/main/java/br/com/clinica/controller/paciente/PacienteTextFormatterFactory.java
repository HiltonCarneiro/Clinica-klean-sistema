package br.com.clinica.controller.paciente;

import javafx.scene.control.TextFormatter;

import java.util.function.Function;

public final class PacienteTextFormatterFactory {

    private PacienteTextFormatterFactory() {
    }

    public static TextFormatter<String> digitsFormatter(int maxLength, Function<String, String> formatter) {
        return new TextFormatter<>(change -> {
            if (!change.isContentChange()) {
                return change;
            }

            String digits = change.getControlNewText().replaceAll("\\D", "");

            if (digits.length() > maxLength) {
                return null;
            }

            String formatted = formatter.apply(digits);

            change.setText(formatted);
            change.setRange(0, change.getControlText().length());
            change.selectRange(formatted.length(), formatted.length());

            return change;
        });
    }

    public static TextFormatter<String> ufFormatter() {
        return new TextFormatter<>(change -> {
            if (!change.isContentChange()) {
                return change;
            }

            String value = change.getControlNewText()
                    .toUpperCase()
                    .replaceAll("[^A-Z]", "");

            if (value.length() > 2) {
                value = value.substring(0, 2);
            }

            change.setText(value);
            change.setRange(0, change.getControlText().length());
            change.selectRange(value.length(), value.length());

            return change;
        });
    }

    public static TextFormatter<String> nameFormatter() {
        return new TextFormatter<>(change -> {
            if (!change.isContentChange()) {
                return change;
            }

            String value = change.getControlNewText()
                    .replaceAll("[^\\p{L}\\s']", "");

            change.setText(value);
            change.setRange(0, change.getControlText().length());
            change.selectRange(value.length(), value.length());

            return change;
        });
    }
}