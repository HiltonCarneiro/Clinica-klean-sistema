package br.com.clinica.controller.avaliacaofisica;

import br.com.clinica.model.AvaliacaoFisica;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AvaliacaoFisicaChartManager {

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void limparGrafico(LineChart<String, Number> chartPeso) {
        if (chartPeso != null) {
            chartPeso.getData().clear();
        }
    }

    public void atualizarGraficoPeso(LineChart<String, Number> chartPeso, List<AvaliacaoFisica> historico) {
        if (chartPeso == null) {
            return;
        }

        chartPeso.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Peso");

        if (historico != null) {
            for (int i = historico.size() - 1; i >= 0; i--) {
                AvaliacaoFisica avaliacao = historico.get(i);

                if (avaliacao.getDataAvaliacao() != null && avaliacao.getPesoKg() != null) {
                    serie.getData().add(new XYChart.Data<>(
                            avaliacao.getDataAvaliacao().format(DATA_FORMATTER),
                            avaliacao.getPesoKg()
                    ));
                }
            }
        }

        chartPeso.getData().add(serie);
    }
}
