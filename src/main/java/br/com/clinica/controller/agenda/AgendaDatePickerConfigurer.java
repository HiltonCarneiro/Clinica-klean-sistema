package br.com.clinica.controller.agenda;

import javafx.application.Platform;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

public class AgendaDatePickerConfigurer {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_PT_BR);

    public void configure(
            DatePicker datePicker,
            Consumer<String> messageHandler,
            Runnable onValidDate
    ) {
        if (datePicker == null) {
            return;
        }

        datePicker.setValue(null);
        datePicker.setEditable(true);
        datePicker.getEditor().clear();
        datePicker.getEditor().setPromptText("dd/MM/aaaa");

        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date.format(DATA_BR);
            }

            @Override
            public LocalDate fromString(String value) {
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }

                try {
                    return LocalDate.parse(value.trim(), DATA_BR);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty && item != null) {
                    setDisable(item.isBefore(LocalDate.now()));
                }
            }
        });

        datePicker.getEditor().setTextFormatter(createDateMask());

        datePicker.getEditor().focusedProperty().addListener((obs, oldValue, focused) -> {
            if (focused) {
                return;
            }

            String text = datePicker.getEditor().getText() == null
                    ? ""
                    : datePicker.getEditor().getText().trim();

            if (text.isEmpty()) {
                datePicker.setValue(null);
                return;
            }

            if (text.replaceAll("\\D", "").length() < 8) {
                messageHandler.accept("Digite a data completa no formato dd/MM/aaaa.");
                return;
            }

            LocalDate parsed = datePicker.getConverter().fromString(text);

            if (parsed == null) {
                messageHandler.accept("Data inválida. Use o formato dd/MM/aaaa.");
                datePicker.setValue(null);
                return;
            }

            if (parsed.isBefore(LocalDate.now())) {
                messageHandler.accept("Não é permitido selecionar data no passado.");
                datePicker.setValue(null);
                Platform.runLater(() -> datePicker.getEditor().requestFocus());
                return;
            }

            datePicker.setValue(parsed);
            datePicker.getEditor().setText(datePicker.getConverter().toString(parsed));
            onValidDate.run();
        });
    }

    private TextFormatter<String> createDateMask() {
        return new TextFormatter<>(change -> {
            if (!change.isContentChange()) {
                return change;
            }

            String newText = change.getControlNewText();

            if (newText == null) {
                return change;
            }

            String digits = newText.replaceAll("\\D", "");

            if (digits.length() > 8) {
                return null;
            }

            if (digits.isEmpty()) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                change.selectRange(0, 0);
                return change;
            }

            StringBuilder formatted = new StringBuilder();

            for (int i = 0; i < digits.length(); i++) {
                formatted.append(digits.charAt(i));

                if (i == 1 && digits.length() > 2) {
                    formatted.append("/");
                }

                if (i == 3 && digits.length() > 4) {
                    formatted.append("/");
                }
            }

            int oldLength = change.getControlText() == null
                    ? 0
                    : change.getControlText().length();

            change.setText(formatted.toString());
            change.setRange(0, oldLength);
            change.selectRange(formatted.length(), formatted.length());

            return change;
        });
    }
}