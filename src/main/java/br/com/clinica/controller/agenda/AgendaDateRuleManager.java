package br.com.clinica.controller.agenda;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.function.Consumer;

public class AgendaDateRuleManager {

    public boolean validarDataSelecionada(
            DatePicker datePicker,
            TableView<?> tableView,
            Consumer<String> mensagemConsumer
    ) {
        if (datePicker == null) {
            return false;
        }

        LocalDate data = datePicker.getValue();

        if (data == null) {
            return false;
        }

        if (data.isBefore(LocalDate.now())) {
            mensagemConsumer.accept("Não é permitido selecionar data no passado.");

            datePicker.setValue(null);

            if (datePicker.getEditor() != null) {
                datePicker.getEditor().clear();
            }

            if (tableView != null) {
                tableView.getItems().clear();
            }

            return false;
        }

        return true;
    }
}