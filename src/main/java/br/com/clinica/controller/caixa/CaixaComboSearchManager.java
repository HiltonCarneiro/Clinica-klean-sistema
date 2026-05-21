package br.com.clinica.controller.caixa;

import br.com.clinica.model.Paciente;
import br.com.clinica.model.Produto;
import br.com.clinica.model.Usuario;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;

import java.util.function.Function;

public class CaixaComboSearchManager {

    public void configurarPesquisaCombos(
            ComboBox<Paciente> cbPaciente,
            ObservableList<Paciente> pacientes,
            ComboBox<Produto> cbProduto,
            ObservableList<Produto> produtos,
            ComboBox<Usuario> cbProfissional,
            ObservableList<Usuario> profissionais
    ) {
        configurarComboPesquisa(cbPaciente, pacientes, p -> safe(p.getNome()));
        configurarComboPesquisa(cbProduto, produtos, p -> safe(p.getNome()));
        configurarComboPesquisa(cbProfissional, profissionais, this::textoProfissional);
    }

    private String textoProfissional(Usuario u) {
        if (u == null) {
            return "";
        }

        String pessoa = safe(u.getPessoaNome());
        String cargo = safe(u.getNome());

        if (pessoa.isBlank() && !safe(u.getLogin()).isBlank()) {
            pessoa = u.getLogin();
        }

        if (cargo.isBlank()) {
            return pessoa;
        }

        return pessoa + " (" + cargo + ")";
    }

    private <T> void configurarComboPesquisa(
            ComboBox<T> comboBox,
            ObservableList<T> itensOriginais,
            Function<T, String> textoFunc
    ) {
        comboBox.setEditable(true);

        FilteredList<T> filtrados = new FilteredList<>(itensOriginais, p -> true);
        comboBox.setItems(filtrados);

        StringConverter<T> conv = new StringConverter<>() {
            @Override
            public String toString(T obj) {
                return obj == null ? "" : safe(textoFunc.apply(obj));
            }

            @Override
            public T fromString(String s) {
                return comboBox.getValue();
            }
        };

        comboBox.setConverter(conv);

        comboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : conv.toString(item));
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : conv.toString(item));
            }
        });

        TextField editor = comboBox.getEditor();

        editor.textProperty().addListener((obs, old, texto) -> {
            T sel = comboBox.getSelectionModel().getSelectedItem();

            if (sel != null && safe(textoFunc.apply(sel)).equals(texto)) {
                return;
            }

            String t = texto == null ? "" : texto.trim().toLowerCase();

            filtrados.setPredicate(item -> {
                if (t.isBlank()) {
                    return true;
                }

                return safe(textoFunc.apply(item)).toLowerCase().contains(t);
            });

            if (!comboBox.isShowing()) {
                comboBox.show();
            }
        });

        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                Platform.runLater(() -> {
                    comboBox.getEditor().setText(conv.toString(novo));
                    comboBox.hide();
                });
            }
        });

        editor.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                confirmarSelecaoOuManter(comboBox, filtrados, textoFunc);
                comboBox.hide();
                e.consume();

            } else if (e.getCode() == KeyCode.ESCAPE) {
                comboBox.hide();
                e.consume();
            }
        });

        editor.focusedProperty().addListener((obs, old, focado) -> {
            if (Boolean.TRUE.equals(focado)) {
                return;
            }

            confirmarSelecaoOuManter(comboBox, filtrados, textoFunc);
            comboBox.hide();
        });

        comboBox.setOnHidden(e -> {
            T sel = comboBox.getSelectionModel().getSelectedItem();

            if (sel != null) {
                editor.setText(conv.toString(sel));
            }

            filtrados.setPredicate(x -> true);
        });
    }

    private <T> void confirmarSelecaoOuManter(
            ComboBox<T> comboBox,
            FilteredList<T> filtrados,
            Function<T, String> textoFunc
    ) {
        String digitado = safe(comboBox.getEditor().getText()).trim();
        T atual = comboBox.getSelectionModel().getSelectedItem();

        if (digitado.isBlank()) {
            comboBox.getSelectionModel().clearSelection();
            comboBox.getEditor().clear();
            return;
        }

        T exato = null;

        for (T item : filtrados.getSource()) {
            if (safe(textoFunc.apply(item)).equalsIgnoreCase(digitado)) {
                exato = item;
                break;
            }
        }

        if (exato != null) {
            comboBox.getSelectionModel().select(exato);
            comboBox.getEditor().setText(safe(textoFunc.apply(exato)));
            return;
        }

        if (!filtrados.isEmpty()) {
            T first = filtrados.get(0);
            comboBox.getSelectionModel().select(first);
            comboBox.getEditor().setText(safe(textoFunc.apply(first)));
            return;
        }

        if (atual != null) {
            comboBox.getEditor().setText(safe(textoFunc.apply(atual)));
        } else {
            comboBox.getEditor().clear();
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}