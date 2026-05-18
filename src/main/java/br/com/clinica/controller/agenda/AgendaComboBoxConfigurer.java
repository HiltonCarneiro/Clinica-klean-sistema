package br.com.clinica.controller.agenda;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Function;

public class AgendaComboBoxConfigurer<T> {

    private ObservableList<T> originalItems = FXCollections.observableArrayList();

    public void configure(
            ComboBox<T> comboBox,
            List<T> items,
            Function<T, String> labelProvider
    ) {
        if (comboBox == null) {
            return;
        }

        comboBox.setEditable(true);

        originalItems = FXCollections.observableArrayList(items);
        FilteredList<T> filteredItems = new FilteredList<>(originalItems, item -> true);

        comboBox.setItems(filteredItems);

        StringConverter<T> converter = new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : labelProvider.apply(item);
            }

            @Override
            public T fromString(String value) {
                return comboBox.getValue();
            }
        };

        comboBox.setConverter(converter);

        comboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : labelProvider.apply(item));
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : labelProvider.apply(item));
            }
        });

        TextField editor = comboBox.getEditor();

        editor.textProperty().addListener((obs, oldText, newText) -> {
            T selected = comboBox.getSelectionModel().getSelectedItem();

            if (selected != null && labelProvider.apply(selected).equals(newText)) {
                return;
            }

            String filter = newText == null ? "" : newText.trim().toLowerCase();

            filteredItems.setPredicate(item ->
                    filter.isBlank()
                            || labelProvider.apply(item).toLowerCase().contains(filter)
            );

            if (!comboBox.isShowing()) {
                comboBox.show();
            }
        });

        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                Platform.runLater(() -> {
                    comboBox.getEditor().setText(labelProvider.apply(selected));
                    comboBox.hide();
                });
            }
        });

        editor.focusedProperty().addListener((obs, oldValue, focused) -> {
            if (focused) {
                return;
            }

            resolveSelectionFromEditor(comboBox, filteredItems, labelProvider);
            filteredItems.setPredicate(item -> true);
            comboBox.hide();
        });

        comboBox.setOnHidden(event -> filteredItems.setPredicate(item -> true));
    }

    public T resolveSelectedItem(
            ComboBox<T> comboBox,
            Function<T, String> labelProvider
    ) {
        if (comboBox == null) {
            return null;
        }

        T value = comboBox.getValue();

        if (value != null) {
            return value;
        }

        if (!comboBox.isEditable() || comboBox.getEditor() == null) {
            return null;
        }

        String typed = comboBox.getEditor().getText();

        if (typed == null || typed.trim().isEmpty()) {
            return null;
        }

        String target = typed.trim();

        for (T item : originalItems) {
            if (labelProvider.apply(item).equalsIgnoreCase(target)) {
                comboBox.setValue(item);
                return item;
            }
        }

        for (T item : originalItems) {
            if (labelProvider.apply(item).toLowerCase().contains(target.toLowerCase())) {
                comboBox.setValue(item);
                return item;
            }
        }

        return null;
    }

    private void resolveSelectionFromEditor(
            ComboBox<T> comboBox,
            FilteredList<T> filteredItems,
            Function<T, String> labelProvider
    ) {
        String typed = comboBox.getEditor().getText();

        if (typed == null || typed.trim().isEmpty()) {
            comboBox.getSelectionModel().clearSelection();
            comboBox.getEditor().clear();
            return;
        }

        T exact = null;

        for (T item : originalItems) {
            if (labelProvider.apply(item).equalsIgnoreCase(typed.trim())) {
                exact = item;
                break;
            }
        }

        if (exact == null && !filteredItems.isEmpty()) {
            exact = filteredItems.get(0);
        }

        if (exact != null) {
            comboBox.getSelectionModel().select(exact);
            comboBox.getEditor().setText(labelProvider.apply(exact));
        }
    }
}