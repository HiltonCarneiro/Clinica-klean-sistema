package br.com.clinica.controller.avaliacaofisica;

import br.com.clinica.model.AvaliacaoFisica;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class AvaliacaoFisicaTableManager {

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void configurarTabela(
            TableView<AvaliacaoFisica> tableAvaliacoes,
            TableColumn<AvaliacaoFisica, String> colData,
            TableColumn<AvaliacaoFisica, String> colProfissional,
            TableColumn<AvaliacaoFisica, Double> colPeso,
            TableColumn<AvaliacaoFisica, Double> colImc,
            TableColumn<AvaliacaoFisica, Double> colPercentual,
            TableColumn<AvaliacaoFisica, Double> colMassaMagra,
            TableColumn<AvaliacaoFisica, Double> colMassaGorda,
            TableColumn<AvaliacaoFisica, Double> colRcq,
            Consumer<AvaliacaoFisica> onSelecionar
    ) {
        if (colData != null) {
            colData.setCellValueFactory(cell -> {
                LocalDate data = cell.getValue().getDataAvaliacao();
                return new SimpleStringProperty(data == null ? "" : data.format(DATA_FORMATTER));
            });
        }

        if (colProfissional != null) {
            colProfissional.setCellValueFactory(cell ->
                    new SimpleStringProperty(AvaliacaoFisicaFormatter.valorTexto(cell.getValue().getProfissionalResponsavel()))
            );
        }

        if (colPeso != null) {
            colPeso.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPesoKg()));
        }

        if (colImc != null) {
            colImc.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getImc()));
        }

        if (colPercentual != null) {
            colPercentual.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPercentualGordura()));
        }

        if (colMassaMagra != null) {
            colMassaMagra.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getMassaMagraKg()));
        }

        if (colMassaGorda != null) {
            colMassaGorda.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getMassaGordaKg()));
        }

        if (colRcq != null) {
            colRcq.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getRcq()));
        }

        if (tableAvaliacoes != null) {
            tableAvaliacoes.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, antigo, novo) -> {
                        if (novo != null && onSelecionar != null) {
                            onSelecionar.accept(novo);
                        }
                    });
        }
    }

    public void atualizarTabela(TableView<AvaliacaoFisica> tableAvaliacoes, List<AvaliacaoFisica> historico) {
        if (tableAvaliacoes != null) {
            tableAvaliacoes.setItems(FXCollections.observableArrayList(historico));
        }
    }

    public void limparTabela(TableView<AvaliacaoFisica> tableAvaliacoes) {
        if (tableAvaliacoes != null) {
            tableAvaliacoes.setItems(FXCollections.observableArrayList());
        }
    }

    public AvaliacaoFisica obterSelecionada(TableView<AvaliacaoFisica> tableAvaliacoes) {
        return tableAvaliacoes == null ? null : tableAvaliacoes.getSelectionModel().getSelectedItem();
    }

    public void limparSelecao(TableView<AvaliacaoFisica> tableAvaliacoes) {
        if (tableAvaliacoes != null) {
            tableAvaliacoes.getSelectionModel().clearSelection();
        }
    }
}
