package br.com.clinica.controller.estoque;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EstoqueMaskManager {

    private final DateTimeFormatter dataFormatter;
    private final DecimalFormat moedaFormatter;

    public EstoqueMaskManager(
            DateTimeFormatter dataFormatter,
            DecimalFormat moedaFormatter
    ) {
        this.dataFormatter = dataFormatter;
        this.moedaFormatter = moedaFormatter;
    }

    public void configurarDatePickerBR(DatePicker dp) {
        dp.setPromptText("dd/MM/aaaa");

        dp.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date.format(dataFormatter);
            }

            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.trim().isBlank()) {
                    return null;
                }

                try {
                    return LocalDate.parse(string.trim(), dataFormatter);
                } catch (Exception e) {
                    return null;
                }
            }
        });
    }

    public void aplicarMascaraDataNoEditor(DatePicker dp) {
        TextField editor = dp.getEditor();

        editor.textProperty().addListener((obs, old, neu) -> {
            if (neu == null) {
                return;
            }

            String digits = neu.replaceAll("\\D", "");

            if (digits.length() > 8) {
                digits = digits.substring(0, 8);
            }

            String formatted = formatarDataDigits(digits);

            if (!formatted.equals(neu)) {
                int caret = formatted.length();

                editor.setText(formatted);
                editor.positionCaret(caret);
            }
        });
    }

    private String formatarDataDigits(String digits) {
        if (digits.isEmpty()) {
            return "";
        }

        if (digits.length() <= 2) {
            return digits;
        }

        if (digits.length() <= 4) {
            return digits.substring(0, 2)
                    + "/"
                    + digits.substring(2);
        }

        return digits.substring(0, 2)
                + "/"
                + digits.substring(2, 4)
                + "/"
                + digits.substring(4);
    }

    public void aplicarFiltroQuantidade(TextField tf) {
        tf.setTextFormatter(new TextFormatter<>(change -> {
            String novo = change.getControlNewText();

            if (novo == null || novo.isBlank()) {
                return change;
            }

            return novo.matches("[0-9.,\\- ]*")
                    ? change
                    : null;
        }));
    }

    public void aplicarMascaraMoeda(TextField tf) {
        tf.setTextFormatter(new TextFormatter<>(change -> {
            String novo = change.getControlNewText();

            if (novo == null) {
                return change;
            }

            if (novo.isBlank()) {
                return change;
            }

            String digits = novo.replaceAll("\\D", "");

            boolean temLetraOuSimbolo =
                    !novo.equals(digits) && novo.matches(".*[A-Za-z].*");

            if (temLetraOuSimbolo) {
                return null;
            }

            digits = digits.replaceFirst("^0+(?!$)", "");

            if (digits.length() > 14) {
                digits = digits.substring(0, 14);
            }

            if (digits.isEmpty()) {
                return null;
            }

            BigDecimal value = new BigDecimal(digits).movePointLeft(2);
            value = value.setScale(2, RoundingMode.DOWN);

            String formatted = moedaFormatter.format(value);

            change.setText(formatted);
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(formatted.length());
            change.setAnchor(formatted.length());

            return change;
        }));
    }

    public double parseNumero(String texto) {
        Double v = parseNumeroNullable(texto);
        return v == null ? 0.0 : v;
    }

    public Double parseNumeroNullable(String texto) {
        if (texto == null) {
            return null;
        }

        String s = texto.trim();

        if (s.isBlank()) {
            return null;
        }

        s = s.replaceAll("[^0-9,\\.\\-]", "");

        if (s.contains(",") && s.contains(".")) {
            s = s.replace(".", "").replace(",", ".");
        } else if (s.contains(",")) {
            s = s.replace(",", ".");
        }

        if (s.isBlank() || s.equals("-")) {
            return null;
        }

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double parseMoedaNullable(String texto) {
        return parseNumeroNullable(texto);
    }

    public String formatarMoedaInput(Double valor) {
        if (valor == null) {
            return "";
        }

        BigDecimal v = BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP);

        return moedaFormatter.format(v);
    }

    public LocalDate obterDataDigitada(DatePicker dp) {
        LocalDate validade = dp.getValue();

        if (validade != null) {
            return validade;
        }

        String txt = dp.getEditor().getText();

        if (txt != null && txt.trim().length() == 10) {
            try {
                return LocalDate.parse(txt.trim(), dataFormatter);
            } catch (Exception ignored) {
                return null;
            }
        }

        return null;
    }
}