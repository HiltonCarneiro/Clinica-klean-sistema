package br.com.clinica.controller.agenda;

import br.com.clinica.model.Agendamento;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class AgendaTableManager {

    private static final DateTimeFormatter HORA_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("pt-BR"));

    public void configure(
            TableView<Agendamento> table,
            TableColumn<Agendamento, String> colHora,
            TableColumn<Agendamento, String> colProfissional,
            TableColumn<Agendamento, String> colSala,
            TableColumn<Agendamento, String> colPaciente,
            TableColumn<Agendamento, String> colStatus,
            Consumer<Agendamento> onSelected
    ) {
        colHora.setCellValueFactory(cell ->
                new SimpleStringProperty(formatarHorario(cell.getValue()))
        );

        colProfissional.setCellValueFactory(cell ->
                new SimpleStringProperty(safe(cell.getValue().getProfissionalNome()))
        );

        colSala.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getSala() == null
                                ? ""
                                : cell.getValue().getSala().getDescricao()
                )
        );

        colPaciente.setCellValueFactory(cell ->
                new SimpleStringProperty(safe(cell.getValue().getPacienteNome()))
        );

        colStatus.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getStatus() == null
                                ? ""
                                : cell.getValue().getStatus().name()
                )
        );

        table.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, selected) -> {
                    if (selected != null) {
                        onSelected.accept(selected);
                    }
                });
    }

    public void setItems(TableView<Agendamento> table, List<Agendamento> items) {
        table.setItems(FXCollections.observableArrayList(items));
    }

    public void clear(TableView<Agendamento> table) {
        table.getItems().clear();
    }

    public Agendamento getSelected(TableView<Agendamento> table) {
        return table.getSelectionModel().getSelectedItem();
    }

    private String formatarHorario(Agendamento agendamento) {
        String inicio = agendamento.getHoraInicio() == null
                ? ""
                : agendamento.getHoraInicio().format(HORA_FORMATTER);

        String fim = agendamento.getHoraFim() == null
                ? ""
                : agendamento.getHoraFim().format(HORA_FORMATTER);

        return inicio + " - " + fim;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}