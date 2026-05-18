package br.com.clinica.controller.agenda;

import javafx.scene.control.TextFormatter;

public final class AgendaTextFormatters {

    private AgendaTextFormatters() {
    }

    public static TextFormatter<String> horaFormatter() {
        return new TextFormatter<>(change -> {
            if (!change.isContentChange()) {
                return change;
            }

            String text = change.getControlNewText();

            if (text == null || text.isEmpty()) {
                return change;
            }

            String digits = text.replaceAll("\\D", "");

            if (digits.length() > 4) {
                return null;
            }

            String formatted;

            if (digits.length() <= 2) {
                formatted = digits;
            } else {
                formatted = digits.substring(0, 2) + ":" + digits.substring(2);
            }

            int oldLength = change.getControlText() == null
                    ? 0
                    : change.getControlText().length();

            change.setText(formatted);
            change.setRange(0, oldLength);
            change.selectRange(formatted.length(), formatted.length());

            return change;
        });
    }
}