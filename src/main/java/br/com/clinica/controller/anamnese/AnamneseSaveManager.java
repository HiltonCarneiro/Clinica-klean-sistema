package br.com.clinica.controller.anamnese;

import br.com.clinica.dao.AnamneseDAO;
import br.com.clinica.dao.AnexoPacienteDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Anamnese;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AnamneseSaveManager {

    private static final DateTimeFormatter DB_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.forLanguageTag("pt-BR"));

    public SaveResult salvar(
            String tipo,
            Paciente paciente,
            Agendamento agendamento,
            Usuario usuario,
            Anamnese selecionada,
            String dadosJson,
            String observacoes,
            AnamneseDAO anamneseDAO,
            AnexoPacienteDAO anexoDAO,
            AnamneseEvolutionFileManager evolutionFileManager,
            AnamneseJsonMapper jsonMapper
    ) {
        Anamnese anamnese = montarAnamnese(
                tipo,
                paciente,
                agendamento,
                usuario,
                dadosJson,
                observacoes
        );

        if ("ANAMNESE_INICIAL".equals(tipo)) {
            int id = anamneseDAO.salvarOuAtualizarInicial(anamnese);
            anamnese.setId(id);

            return new SaveResult(
                    anamnese,
                    true,
                    false,
                    "Anamnese inicial salva/atualizada. Evoluções liberadas.",
                    "EVOLUCAO"
            );
        }

        boolean editandoEvolucao = selecionada != null
                && "EVOLUCAO".equalsIgnoreCase(selecionada.getTipo())
                && selecionada.getId() != null;

        if (editandoEvolucao) {
            anamnese.setId(selecionada.getId());
            anamneseDAO.atualizar(anamnese);
            atualizarArquivoEvolucao(paciente, anamnese, anexoDAO, evolutionFileManager, jsonMapper);

            return new SaveResult(
                    null,
                    true,
                    false,
                    "Evolução atualizada.",
                    null
            );
        }

        int id = anamneseDAO.inserir(anamnese);
        anamnese.setId(id);
        atualizarArquivoEvolucao(paciente, anamnese, anexoDAO, evolutionFileManager, jsonMapper);

        return new SaveResult(
                null,
                true,
                true,
                "Evolução salva com sucesso.",
                null
        );
    }

    private Anamnese montarAnamnese(
            String tipo,
            Paciente paciente,
            Agendamento agendamento,
            Usuario usuario,
            String dadosJson,
            String observacoes
    ) {
        Anamnese anamnese = new Anamnese();
        anamnese.setPacienteId(paciente.getId());
        anamnese.setAgendamentoId(agendamento != null ? agendamento.getId() : null);
        anamnese.setProfissionalId(usuario.getId());
        anamnese.setDataHora(LocalDateTime.now().format(DB_FMT));
        anamnese.setTipo(tipo);
        anamnese.setDadosJson(dadosJson);
        anamnese.setObservacoes(observacoes);
        return anamnese;
    }

    private void atualizarArquivoEvolucao(
            Paciente paciente,
            Anamnese anamnese,
            AnexoPacienteDAO anexoDAO,
            AnamneseEvolutionFileManager evolutionFileManager,
            AnamneseJsonMapper jsonMapper
    ) {
        anexoDAO.criarOuAtualizarArquivoEvolucao(
                paciente.getId(),
                anamnese.getId(),
                evolutionFileManager.gerarTextoEvolucaoParaArquivo(anamnese, jsonMapper)
        );
    }

    public record SaveResult(
            Anamnese anamneseInicialAtual,
            boolean recarregarHistorico,
            boolean limparFormulario,
            String mensagem,
            String tipoParaSelecionar
    ) {
    }
}
