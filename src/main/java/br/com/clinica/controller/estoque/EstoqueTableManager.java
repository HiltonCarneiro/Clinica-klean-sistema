package br.com.clinica.controller.estoque;

import br.com.clinica.model.Produto;
import br.com.clinica.model.enums.TipoProduto;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EstoqueTableManager {

    private final DecimalFormat decimalFormat;
    private final DateTimeFormatter dataFormatter;

    public EstoqueTableManager(
            DecimalFormat decimalFormat,
            DateTimeFormatter dataFormatter
    ) {
        this.decimalFormat = decimalFormat;
        this.dataFormatter = dataFormatter;
    }

    public void configurarTabela(
            TableColumn<Produto, String> colNome,
            TableColumn<Produto, String> colTipo,
            TableColumn<Produto, Number> colEstoque,
            TableColumn<Produto, String> colValidade,
            TableColumn<Produto, Boolean> colAtivo
    ) {

        colNome.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getNome())
        );

        colTipo.setCellValueFactory(cell -> {
            TipoProduto tipo = cell.getValue().getTipo();

            return new SimpleStringProperty(
                    tipo != null ? tipo.getDescricao() : ""
            );
        });

        colEstoque.setCellValueFactory(cell ->
                new SimpleDoubleProperty(
                        cell.getValue().getEstoqueAtual()
                )
        );

        colEstoque.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number valor, boolean empty) {
                super.updateItem(valor, empty);

                setText(
                        empty || valor == null
                                ? null
                                : decimalFormat.format(valor.doubleValue())
                );
            }
        });

        colValidade.setCellValueFactory(cell -> {
            LocalDate validade = cell.getValue().getValidade();

            return new SimpleStringProperty(
                    validade != null
                            ? validade.format(dataFormatter)
                            : ""
            );
        });

        colAtivo.setCellValueFactory(cell ->
                new SimpleBooleanProperty(
                        cell.getValue().isAtivo()
                )
        );

        colAtivo.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean ativo, boolean empty) {
                super.updateItem(ativo, empty);

                if (empty || ativo == null) {
                    setText(null);
                    return;
                }

                setText(ativo ? "Ativo" : "Inativo");
            }
        });
    }
}