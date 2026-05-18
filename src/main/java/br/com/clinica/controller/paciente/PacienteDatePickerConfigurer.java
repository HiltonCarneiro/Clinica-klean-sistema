package br.com.clinica.controller.paciente;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

public class PacienteDatePickerConfigurer {

    private static final DateTimeFormatter DATA_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));

    public void configure(
            DatePicker datePicker,
            Consumer<String> messageHandler,
            Runnable onValidDate
    ) {
        if (datePicker == null) {
            return;
        }

        datePicker.setEditable(true);
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

        datePicker.getEditor().setTextFormatter(dateFormatter());

        datePicker.getEditor().focusedProperty().addListener((obs, oldValue, focused) -> {
            if (focused) {
                return;
            }

            String text = datePicker.getEditor().getText();

            if (text == null || text.trim().isEmpty()) {
                datePicker.setValue(null);
                onValidDate.run();
                return;
            }

            LocalDate parsed = datePicker.getConverter().fromString(text);

            if (parsed == null) {
                datePicker.setValue(null);
                messageHandler.accept("Data de nascimento inválida.");
                onValidDate.run();
                return;
            }

            datePicker.setValue(parsed);
            datePicker.getEditor().setText(datePicker.getConverter().toString(parsed));
            messageHandler.accept("");
            onValidDate.run();
        });
    }

    private TextFormatter<String> dateFormatter() {
        return new TextFormatter<>(change -> {
            if (!change.isContentChange()) {
                return change;
            }

            String digits = change.getControlNewText().replaceAll("\\D", "");

            if (digits.length() > 8) {
                return null;
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

            change.setText(formatted.toString());
            change.setRange(0, change.getControlText().length());
            change.selectRange(formatted.length(), formatted.length());

            return change;
        });
    }
}