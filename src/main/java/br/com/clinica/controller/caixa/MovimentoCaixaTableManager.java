package br.com.clinica.controller.caixa;

import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.enums.TipoMovimento;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;

public class MovimentoCaixaTableManager {

    public void configurarTabela(
            TableView<MovimentoCaixa> tblMovimentos,
            TableColumn<MovimentoCaixa, LocalDate> colData,
            TableColumn<MovimentoCaixa, String> colDescricao,
            TableColumn<MovimentoCaixa, String> colTipo,
            TableColumn<MovimentoCaixa, Double> colValor,
            TableColumn<MovimentoCaixa, String> colFormaPagamento,
            TableColumn<MovimentoCaixa, String> colPaciente,
            ObservableList<MovimentoCaixa> movimentos
    ) {
        tblMovimentos.setItems(movimentos);

        colData.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getData())
        );

        colDescricao.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDescricao())
        );

        colTipo.setCellValueFactory(cell ->
                new SimpleStringProperty(formatarTipo(cell.getValue().getTipo()))
        );

        colValor.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getValor()).asObject()
        );

        colFormaPagamento.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFormaPagamento())
        );

        colPaciente.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPacienteNome())
        );
    }

    private String formatarTipo(TipoMovimento tipo) {
        if (tipo == TipoMovimento.ENTRADA) {
            return "Entrada";
        }

        if (tipo == TipoMovimento.SAIDA) {
            return "Saída";
        }

        return "";
    }
}