package br.com.clinica.service;

import br.com.clinica.dao.AvaliacaoFisicaDAO;
import br.com.clinica.model.AvaliacaoFisica;

import java.time.LocalDate;
import java.util.List;

public class AvaliacaoFisicaService {

    private final AvaliacaoFisicaDAO dao = new AvaliacaoFisicaDAO();
    private final AvaliacaoFisicaCalculoService calculoService = new AvaliacaoFisicaCalculoService();

    public void salvar(AvaliacaoFisica avaliacao) {
        validar(avaliacao);
        calculoService.calcularResultados(avaliacao);
        dao.salvar(avaliacao);
    }

    public void excluir(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Avaliação física inválida para exclusão.");
        }

        dao.excluir(id);
    }

    public AvaliacaoFisica buscarPorId(Long id) {
        if (id == null) return null;
        return dao.buscarPorId(id);
    }

    public List<AvaliacaoFisica> listarPorPaciente(Long pacienteId) {
        if (pacienteId == null) {
            throw new IllegalArgumentException("Paciente obrigatório.");
        }

        return dao.listarPorPaciente(pacienteId);
    }

    public AvaliacaoFisica buscarUltimaPorPaciente(Long pacienteId) {
        if (pacienteId == null) return null;
        return dao.buscarUltimaPorPaciente(pacienteId);
    }

    private void validar(AvaliacaoFisica a) {
        if (a == null) {
            throw new IllegalArgumentException("Avaliação física inválida.");
        }

        if (a.getPacienteId() == null) {
            throw new IllegalArgumentException("Selecione um paciente para a avaliação física.");
        }

        if (a.getDataAvaliacao() == null) {
            a.setDataAvaliacao(LocalDate.now());
        }

        if (a.getPesoKg() == null || a.getPesoKg() <= 0) {
            throw new IllegalArgumentException("Informe um peso válido maior que zero.");
        }

        if (a.getAlturaM() == null || a.getAlturaM() <= 0) {
            throw new IllegalArgumentException("Informe uma altura válida maior que zero.");
        }

        if (a.getAlturaM() > 3) {
            throw new IllegalArgumentException("Informe a altura em metros. Exemplo: 1,75.");
        }

        validarNaoNegativo(a.getDobraTricipitalMm(), "Dobra tricipital");
        validarNaoNegativo(a.getDobraBicipitalMm(), "Dobra bicipital");
        validarNaoNegativo(a.getDobraAbdominalMm(), "Dobra abdominal");
        validarNaoNegativo(a.getDobraSubescapularMm(), "Dobra subescapular");
        validarNaoNegativo(a.getDobraAxilarMediaMm(), "Dobra axilar média");
        validarNaoNegativo(a.getDobraCoxaMm(), "Dobra da coxa");
        validarNaoNegativo(a.getDobraToracicaMm(), "Dobra torácica");
        validarNaoNegativo(a.getDobraSuprailiacaMm(), "Dobra suprailíaca");
        validarNaoNegativo(a.getDobraPanturrilhaMm(), "Dobra da panturrilha");

        validarNaoNegativo(a.getCircPescocoCm(), "Circunferência do pescoço");
        validarNaoNegativo(a.getCircToraxCm(), "Circunferência do tórax");
        validarNaoNegativo(a.getCircOmbroCm(), "Circunferência do ombro");
        validarNaoNegativo(a.getCircCinturaCm(), "Circunferência da cintura");
        validarNaoNegativo(a.getCircQuadrilCm(), "Circunferência do quadril");
        validarNaoNegativo(a.getCircAbdomenCm(), "Circunferência do abdômen");
    }

    private void validarNaoNegativo(Double valor, String campo) {
        if (valor != null && valor < 0) {
            throw new IllegalArgumentException(campo + " não pode ser negativo.");
        }
    }
}