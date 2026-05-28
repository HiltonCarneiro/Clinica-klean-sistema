package br.com.clinica.controller.avaliacaofisica;

import br.com.clinica.model.AvaliacaoFisica;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;

import java.time.LocalDate;

public class AvaliacaoFisicaFormManager {

    private final AvaliacaoFisicaFormFields fields;
    private final AvaliacaoFisicaComboManager comboManager;
    private final AvaliacaoFisicaResultManager resultManager;
    private AvaliacaoFisica selecionada;

    public AvaliacaoFisicaFormManager(
            AvaliacaoFisicaFormFields fields,
            AvaliacaoFisicaComboManager comboManager,
            AvaliacaoFisicaResultManager resultManager
    ) {
        this.fields = fields;
        this.comboManager = comboManager;
        this.resultManager = resultManager;
    }

    public AvaliacaoFisica getSelecionada() {
        return selecionada;
    }

    public void setSelecionada(AvaliacaoFisica selecionada) {
        this.selecionada = selecionada;
    }

    public void novaAvaliacao() {
        selecionada = null;

        if (fields.dpDataAvaliacao != null) {
            fields.dpDataAvaliacao.setValue(LocalDate.now());
        }

        AvaliacaoFisicaFieldUtils.limparCombo(fields.cbProfissionais);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtObjetivo);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtPeso);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtAltura);
        AvaliacaoFisicaFieldUtils.limparCombo(fields.cbSexo);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtIdade);
        AvaliacaoFisicaFieldUtils.limparCombo(fields.cbNivelAtividade);
        limparDobras();
        limparCircunferencias();
        AvaliacaoFisicaFieldUtils.limparAreaTexto(fields.txtObservacoesGerais);
        AvaliacaoFisicaFieldUtils.limparAreaTexto(fields.txtObservacoesNutricionais);
        AvaliacaoFisicaFieldUtils.limparAreaTexto(fields.txtAnotacoesProfissional);

        Paciente paciente = fields.cbPacientes == null ? null : fields.cbPacientes.getValue();
        if (paciente != null) {
            comboManager.preencherIdadePorPaciente(paciente, fields.txtIdade);
        }
    }

    public AvaliacaoFisica montarAvaliacaoDoFormulario() {
        AvaliacaoFisica avaliacao = selecionada == null ? new AvaliacaoFisica() : selecionada;

        Paciente paciente = fields.cbPacientes == null ? null : fields.cbPacientes.getValue();
        if (paciente != null) {
            avaliacao.setPaciente(paciente);
            avaliacao.setPacienteId(paciente.getId());
        }

        Usuario profissional = fields.cbProfissionais == null ? null : fields.cbProfissionais.getValue();

        avaliacao.setDataAvaliacao(fields.dpDataAvaliacao == null ? null : fields.dpDataAvaliacao.getValue());
        avaliacao.setProfissionalResponsavel(profissional == null ? null : profissional.toString());
        avaliacao.setObjetivoPaciente(AvaliacaoFisicaFieldUtils.getTexto(fields.txtObjetivo));
        avaliacao.setPesoKg(AvaliacaoFisicaFormatter.parseDouble(fields.txtPeso));
        avaliacao.setAlturaM(AvaliacaoFisicaFormatter.parseDouble(fields.txtAltura));
        avaliacao.setSexoBiologico(fields.cbSexo == null ? null : fields.cbSexo.getValue());
        avaliacao.setIdadeNoMomento(AvaliacaoFisicaFormatter.parseInteger(fields.txtIdade));
        avaliacao.setNivelAtividadeFisica(fields.cbNivelAtividade == null ? null : fields.cbNivelAtividade.getValue());

        avaliacao.setDobraTricipitalMm(AvaliacaoFisicaFormatter.parseDouble(fields.txtDobraTricipital));
        avaliacao.setDobraBicipitalMm(AvaliacaoFisicaFormatter.parseDouble(fields.txtDobraBicipital));
        avaliacao.setDobraAbdominalMm(AvaliacaoFisicaFormatter.parseDouble(fields.txtDobraAbdominal));
        avaliacao.setDobraSubescapularMm(AvaliacaoFisicaFormatter.parseDouble(fields.txtDobraSubescapular));
        avaliacao.setDobraAxilarMediaMm(AvaliacaoFisicaFormatter.parseDouble(fields.txtDobraAxilarMedia));
        avaliacao.setDobraCoxaMm(AvaliacaoFisicaFormatter.parseDouble(fields.txtDobraCoxa));
        avaliacao.setDobraToracicaMm(AvaliacaoFisicaFormatter.parseDouble(fields.txtDobraToracica));
        avaliacao.setDobraSuprailiacaMm(AvaliacaoFisicaFormatter.parseDouble(fields.txtDobraSuprailiaca));
        avaliacao.setDobraPanturrilhaMm(AvaliacaoFisicaFormatter.parseDouble(fields.txtDobraPanturrilha));

        avaliacao.setCircPescocoCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircPescoco));
        avaliacao.setCircToraxCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircTorax));
        avaliacao.setCircOmbroCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircOmbro));
        avaliacao.setCircCinturaCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircCintura));
        avaliacao.setCircQuadrilCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircQuadril));
        avaliacao.setCircAbdomenCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircAbdomen));
        avaliacao.setCircBracoEsqRelaxadoCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircBracoEsqRelaxado));
        avaliacao.setCircBracoDirRelaxadoCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircBracoDirRelaxado));
        avaliacao.setCircBracoEsqContraidoCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircBracoEsqContraido));
        avaliacao.setCircBracoDirContraidoCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircBracoDirContraido));
        avaliacao.setCircAntebracoEsqCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircAntebracoEsq));
        avaliacao.setCircAntebracoDirCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircAntebracoDir));
        avaliacao.setCircCoxaEsqProximalCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircCoxaEsqProximal));
        avaliacao.setCircCoxaDirProximalCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircCoxaDirProximal));
        avaliacao.setCircCoxaEsqMedialCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircCoxaEsqMedial));
        avaliacao.setCircCoxaDirMedialCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircCoxaDirMedial));
        avaliacao.setCircCoxaEsqDistalCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircCoxaEsqDistal));
        avaliacao.setCircCoxaDirDistalCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircCoxaDirDistal));
        avaliacao.setCircPanturrilhaEsqCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircPanturrilhaEsq));
        avaliacao.setCircPanturrilhaDirCm(AvaliacaoFisicaFormatter.parseDouble(fields.txtCircPanturrilhaDir));

        avaliacao.setObservacoesGerais(AvaliacaoFisicaFieldUtils.getTextoArea(fields.txtObservacoesGerais));
        avaliacao.setObservacoesNutricionais(AvaliacaoFisicaFieldUtils.getTextoArea(fields.txtObservacoesNutricionais));
        avaliacao.setAnotacoesProfissional(AvaliacaoFisicaFieldUtils.getTextoArea(fields.txtAnotacoesProfissional));

        return avaliacao;
    }

    public void preencherFormulario(AvaliacaoFisica avaliacao) {
        if (avaliacao == null) {
            return;
        }

        selecionada = avaliacao;
        comboManager.selecionarPacientePorId(fields.cbPacientes, avaliacao.getPacienteId());

        if (fields.dpDataAvaliacao != null) {
            fields.dpDataAvaliacao.setValue(avaliacao.getDataAvaliacao());
        }

        AvaliacaoFisicaFieldUtils.setCampo(fields.txtObjetivo, AvaliacaoFisicaFormatter.valorTextoParaCampo(avaliacao.getObjetivoPaciente()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtPeso, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getPesoKg()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtAltura, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getAlturaM()));

        if (fields.cbSexo != null) fields.cbSexo.setValue(avaliacao.getSexoBiologico());
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtIdade, avaliacao.getIdadeNoMomento() == null ? "" : String.valueOf(avaliacao.getIdadeNoMomento()));
        if (fields.cbNivelAtividade != null) fields.cbNivelAtividade.setValue(avaliacao.getNivelAtividadeFisica());

        AvaliacaoFisicaFieldUtils.setCampo(fields.txtDobraTricipital, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getDobraTricipitalMm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtDobraBicipital, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getDobraBicipitalMm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtDobraAbdominal, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getDobraAbdominalMm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtDobraSubescapular, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getDobraSubescapularMm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtDobraAxilarMedia, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getDobraAxilarMediaMm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtDobraCoxa, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getDobraCoxaMm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtDobraToracica, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getDobraToracicaMm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtDobraSuprailiaca, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getDobraSuprailiacaMm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtDobraPanturrilha, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getDobraPanturrilhaMm()));

        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircPescoco, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircPescocoCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircTorax, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircToraxCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircOmbro, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircOmbroCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircCintura, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircCinturaCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircQuadril, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircQuadrilCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircAbdomen, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircAbdomenCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircBracoEsqRelaxado, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircBracoEsqRelaxadoCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircBracoDirRelaxado, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircBracoDirRelaxadoCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircBracoEsqContraido, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircBracoEsqContraidoCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircBracoDirContraido, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircBracoDirContraidoCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircAntebracoEsq, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircAntebracoEsqCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircAntebracoDir, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircAntebracoDirCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircCoxaEsqProximal, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircCoxaEsqProximalCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircCoxaDirProximal, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircCoxaDirProximalCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircCoxaEsqMedial, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircCoxaEsqMedialCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircCoxaDirMedial, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircCoxaDirMedialCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircCoxaEsqDistal, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircCoxaEsqDistalCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircCoxaDirDistal, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircCoxaDirDistalCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircPanturrilhaEsq, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircPanturrilhaEsqCm()));
        AvaliacaoFisicaFieldUtils.setCampo(fields.txtCircPanturrilhaDir, AvaliacaoFisicaFormatter.valorNumeroParaCampo(avaliacao.getCircPanturrilhaDirCm()));

        AvaliacaoFisicaFieldUtils.setAreaTexto(fields.txtObservacoesGerais, AvaliacaoFisicaFormatter.valorTextoParaCampo(avaliacao.getObservacoesGerais()));
        AvaliacaoFisicaFieldUtils.setAreaTexto(fields.txtObservacoesNutricionais, AvaliacaoFisicaFormatter.valorTextoParaCampo(avaliacao.getObservacoesNutricionais()));
        AvaliacaoFisicaFieldUtils.setAreaTexto(fields.txtAnotacoesProfissional, AvaliacaoFisicaFormatter.valorTextoParaCampo(avaliacao.getAnotacoesProfissional()));
    }

    public void limparResultados(javafx.scene.control.Label lblImc, javafx.scene.control.Label lblClassificacaoImc,
                                 javafx.scene.control.Label lblPercentualGordura, javafx.scene.control.Label lblProtocolo,
                                 javafx.scene.control.Label lblMassaMagra, javafx.scene.control.Label lblMassaGorda,
                                 javafx.scene.control.Label lblRcq, javafx.scene.control.Label lblClassificacaoRcq) {
        resultManager.limparResultados(lblImc, lblClassificacaoImc, lblPercentualGordura, lblProtocolo,
                lblMassaMagra, lblMassaGorda, lblRcq, lblClassificacaoRcq);
    }

    private void limparDobras() {
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtDobraTricipital);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtDobraBicipital);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtDobraAbdominal);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtDobraSubescapular);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtDobraAxilarMedia);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtDobraCoxa);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtDobraToracica);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtDobraSuprailiaca);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtDobraPanturrilha);
    }

    private void limparCircunferencias() {
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircPescoco);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircTorax);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircOmbro);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircCintura);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircQuadril);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircAbdomen);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircBracoEsqRelaxado);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircBracoDirRelaxado);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircBracoEsqContraido);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircBracoDirContraido);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircAntebracoEsq);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircAntebracoDir);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircCoxaEsqProximal);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircCoxaDirProximal);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircCoxaEsqMedial);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircCoxaDirMedial);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircCoxaEsqDistal);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircCoxaDirDistal);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircPanturrilhaEsq);
        AvaliacaoFisicaFieldUtils.limparCampo(fields.txtCircPanturrilhaDir);
    }
}
