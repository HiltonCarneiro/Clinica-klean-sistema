package br.com.clinica.controller.avaliacaofisica;

import br.com.clinica.model.AvaliacaoFisica;
import br.com.clinica.model.Paciente;
import br.com.clinica.service.AvaliacaoFisicaService;
import javafx.scene.chart.LineChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;

import java.util.List;

public class AvaliacaoFisicaHistoryManager {

    private final AvaliacaoFisicaService avaliacaoService;
    private final AvaliacaoFisicaTableManager tableManager;
    private final AvaliacaoFisicaChartManager chartManager;

    public AvaliacaoFisicaHistoryManager(
            AvaliacaoFisicaService avaliacaoService,
            AvaliacaoFisicaTableManager tableManager,
            AvaliacaoFisicaChartManager chartManager
    ) {
        this.avaliacaoService = avaliacaoService;
        this.tableManager = tableManager;
        this.chartManager = chartManager;
    }

    public void carregarHistoricoPaciente(
            ComboBox<Paciente> cbPacientes,
            TableView<AvaliacaoFisica> tableAvaliacoes,
            LineChart<String, Number> chartPeso
    ) {
        Paciente paciente = cbPacientes == null ? null : cbPacientes.getValue();

        if (paciente == null || paciente.getId() == null) {
            tableManager.limparTabela(tableAvaliacoes);
            chartManager.limparGrafico(chartPeso);
            return;
        }

        List<AvaliacaoFisica> historico = avaliacaoService.listarPorPaciente(paciente.getId());
        tableManager.atualizarTabela(tableAvaliacoes, historico);
        chartManager.atualizarGraficoPeso(chartPeso, historico);
    }
}
