package br.com.clinica.controller.caixa;

import br.com.clinica.model.NotaItem;
import br.com.clinica.model.enums.TipoItemNota;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class CaixaTableManager {

    public void configurarTabela(
            TableView<NotaItem> tblItens,
            TableColumn<NotaItem, String> colDescricao,
            TableColumn<NotaItem, String> colTipo,
            TableColumn<NotaItem, Double> colQuantidade,
            TableColumn<NotaItem, Double> colValorUnitario,
            TableColumn<NotaItem, Double> colValorTotal,
            ObservableList<NotaItem> itensNota
    ) {
        tblItens.setItems(itensNota);

        colDescricao.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDescricao())
        );

        colTipo.setCellValueFactory(cell ->
                new SimpleStringProperty(formatarTipoItem(cell.getValue().getTipoItem()))
        );

        colQuantidade.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getQuantidade()).asObject()
        );

        colValorUnitario.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getValorUnitario()).asObject()
        );

        colValorTotal.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getValorTotal()).asObject()
        );
    }

    private String formatarTipoItem(TipoItemNota tipo) {
        if (tipo == TipoItemNota.PRODUTO) {
            return "Produto";
        }

        if (tipo == TipoItemNota.PROCEDIMENTO) {
            return "Procedimento";
        }

        return "";
    }
}