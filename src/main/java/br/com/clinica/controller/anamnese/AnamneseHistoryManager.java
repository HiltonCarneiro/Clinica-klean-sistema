package br.com.clinica.controller.anamnese;

import br.com.clinica.dao.AnamneseDAO;
import br.com.clinica.model.Anamnese;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.function.Consumer;

public class AnamneseHistoryManager {

    public void configurarTabela(
            TableView<Anamnese> tabela,
            TableColumn<Anamnese, String> colData,
            TableColumn<Anamnese, String> colTipo,
            Consumer<Anamnese> onSelecionar
    ) {
        if (tabela == null) {
            return;
        }

        if (colData != null) {
            colData.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getDataHora()))
            );
        }

        if (colTipo != null) {
            colTipo.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getTipo()))
            );
        }

        tabela.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, selected) -> {
                    if (selected != null && onSelecionar != null) {
                        onSelecionar.accept(selected);
                    }
                });
    }

    public void atualizarItens(
            TableView<Anamnese> tabela,
            List<Anamnese> historico
    ) {
        if (tabela == null) {
            return;
        }

        tabela.setItems(
                FXCollections.observableArrayList(
                        historico == null ? List.of() : historico
                )
        );
    }

    public void carregarHistorico(
            TableView<Anamnese> tabela,
            AnamneseDAO anamneseDAO,
            Long pacienteId
    ) {
        if (tabela == null || anamneseDAO == null || pacienteId == null) {
            limpar(tabela);
            return;
        }

        atualizarItens(
                tabela,
                anamneseDAO.listarPorPaciente(pacienteId)
        );
    }

    public void limpar(TableView<Anamnese> tabela) {
        if (tabela != null) {
            tabela.setItems(FXCollections.observableArrayList());
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
