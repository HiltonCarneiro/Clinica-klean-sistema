package br.com.clinica.controller.caixa;

import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.enums.TipoMovimento;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

public class MovimentoCaixaSummaryManager {

    public void atualizarTotais(
            ObservableList<MovimentoCaixa> movimentos,
            Label lblEntradas,
            Label lblSaidas,
            Label lblSaldo
    ) {
        double entradas = 0.0;
        double saidas = 0.0;

        for (MovimentoCaixa mov : movimentos) {
            if (mov.getTipo() == TipoMovimento.ENTRADA) {
                entradas += mov.getValor();
            } else if (mov.getTipo() == TipoMovimento.SAIDA) {
                saidas += mov.getValor();
            }
        }

        double saldo = entradas - saidas;

        lblEntradas.setText(formatarMoeda(entradas));
        lblSaidas.setText(formatarMoeda(saidas));
        lblSaldo.setText(formatarMoeda(saldo));
    }

    private String formatarMoeda(double valor) {
        return String.format("R$ %.2f", valor);
    }
}