package br.com.clinica.controller.avaliacaofisica;

import br.com.clinica.model.AvaliacaoFisica;
import br.com.clinica.service.AvaliacaoFisicaCalculoService;
import javafx.scene.control.Label;

public class AvaliacaoFisicaResultManager {

    private final AvaliacaoFisicaCalculoService calculoService;

    public AvaliacaoFisicaResultManager(AvaliacaoFisicaCalculoService calculoService) {
        this.calculoService = calculoService;
    }

    public void atualizarPreview(AvaliacaoFisica preview, Label lblImc, Label lblClassificacaoImc,
                                 Label lblPercentualGordura, Label lblProtocolo, Label lblMassaMagra,
                                 Label lblMassaGorda, Label lblRcq, Label lblClassificacaoRcq) {
        try {
            calculoService.calcularResultados(preview);
            atualizarLabelsResultado(preview, lblImc, lblClassificacaoImc, lblPercentualGordura, lblProtocolo,
                    lblMassaMagra, lblMassaGorda, lblRcq, lblClassificacaoRcq);
        } catch (Exception ignored) {
            limparResultados(lblImc, lblClassificacaoImc, lblPercentualGordura, lblProtocolo,
                    lblMassaMagra, lblMassaGorda, lblRcq, lblClassificacaoRcq);
        }
    }

    public void atualizarLabelsResultado(AvaliacaoFisica avaliacao, Label lblImc, Label lblClassificacaoImc,
                                         Label lblPercentualGordura, Label lblProtocolo, Label lblMassaMagra,
                                         Label lblMassaGorda, Label lblRcq, Label lblClassificacaoRcq) {
        if (avaliacao == null) {
            limparResultados(lblImc, lblClassificacaoImc, lblPercentualGordura, lblProtocolo,
                    lblMassaMagra, lblMassaGorda, lblRcq, lblClassificacaoRcq);
            return;
        }

        AvaliacaoFisicaFieldUtils.setLabel(lblImc, AvaliacaoFisicaFormatter.valorNumero(avaliacao.getImc()));
        AvaliacaoFisicaFieldUtils.setLabel(lblClassificacaoImc, AvaliacaoFisicaFormatter.valorTexto(avaliacao.getClassificacaoImc()));
        AvaliacaoFisicaFieldUtils.setLabel(lblPercentualGordura, AvaliacaoFisicaFormatter.valorNumero(avaliacao.getPercentualGordura()));
        AvaliacaoFisicaFieldUtils.setLabel(lblProtocolo, AvaliacaoFisicaFormatter.valorTexto(avaliacao.getProtocoloGordura()));
        AvaliacaoFisicaFieldUtils.setLabel(lblMassaMagra, AvaliacaoFisicaFormatter.valorNumero(avaliacao.getMassaMagraKg()));
        AvaliacaoFisicaFieldUtils.setLabel(lblMassaGorda, AvaliacaoFisicaFormatter.valorNumero(avaliacao.getMassaGordaKg()));
        AvaliacaoFisicaFieldUtils.setLabel(lblRcq, AvaliacaoFisicaFormatter.valorNumero(avaliacao.getRcq()));
        AvaliacaoFisicaFieldUtils.setLabel(lblClassificacaoRcq, AvaliacaoFisicaFormatter.valorTexto(avaliacao.getClassificacaoRcq()));
    }

    public void limparResultados(Label lblImc, Label lblClassificacaoImc, Label lblPercentualGordura,
                                 Label lblProtocolo, Label lblMassaMagra, Label lblMassaGorda,
                                 Label lblRcq, Label lblClassificacaoRcq) {
        AvaliacaoFisicaFieldUtils.setLabel(lblImc, "-");
        AvaliacaoFisicaFieldUtils.setLabel(lblClassificacaoImc, "-");
        AvaliacaoFisicaFieldUtils.setLabel(lblPercentualGordura, "-");
        AvaliacaoFisicaFieldUtils.setLabel(lblProtocolo, "-");
        AvaliacaoFisicaFieldUtils.setLabel(lblMassaMagra, "-");
        AvaliacaoFisicaFieldUtils.setLabel(lblMassaGorda, "-");
        AvaliacaoFisicaFieldUtils.setLabel(lblRcq, "-");
        AvaliacaoFisicaFieldUtils.setLabel(lblClassificacaoRcq, "-");
    }
}
