package br.com.clinica.controller.avaliacaofisica;

import br.com.clinica.model.AvaliacaoFisica;
import javafx.scene.control.TableView;

public class AvaliacaoFisicaSelectionManager {

    public AvaliacaoFisica obterSelecionada(TableView<AvaliacaoFisica> tableAvaliacoes) {
        return tableAvaliacoes == null ? null : tableAvaliacoes.getSelectionModel().getSelectedItem();
    }

    public void limparSelecao(TableView<AvaliacaoFisica> tableAvaliacoes) {
        if (tableAvaliacoes != null) {
            tableAvaliacoes.getSelectionModel().clearSelection();
        }
    }
}
