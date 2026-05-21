package br.com.clinica.controller.relatorios;

import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.enums.TipoMovimento;
import javafx.scene.control.Label;

import java.util.List;

public class RelatoriosCaixaSummaryManager {

    public void atualizarResumo(
            List<MovimentoCaixa> movimentos,
            Label lblEntradas,
            Label lblSaidas,
            Label lblSaldo
    ) {
        double entradas = movimentos.stream()
                .filter(m -> m.getTipo() == TipoMovimento.ENTRADA)
                .mapToDouble(MovimentoCaixa::getValor)
                .sum();

        double saidas = movimentos.stream()
                .filter(m -> m.getTipo() == TipoMovimento.SAIDA)
                .mapToDouble(MovimentoCaixa::getValor)
                .sum();

        double saldo = entradas - saidas;

        lblEntradas.setText(String.format("R$ %.2f", entradas));
        lblSaidas.setText(String.format("R$ %.2f", saidas));
        lblSaldo.setText(String.format("R$ %.2f", saldo));
    }
}