package br.com.clinica.controller.anamnese;

import br.com.clinica.dao.AnamneseDAO;
import br.com.clinica.model.Anamnese;
import javafx.scene.control.TableView;

public class AnamneseRefreshManager {

    public RefreshResult carregarInicialEvolucoes(
            TableView<Anamnese> tvHistorico,
            AnamneseDAO anamneseDAO,
            AnamneseHistoryManager historyManager,
            Long pacienteId
    ) {
        if (pacienteId == null) {
            return new RefreshResult(null, false);
        }

        Anamnese anamneseInicialAtual = anamneseDAO.buscarInicialPorPaciente(pacienteId);
        boolean inicialJaSalva = anamneseInicialAtual != null && anamneseInicialAtual.getId() != null;

        historyManager.carregarHistorico(
                tvHistorico,
                anamneseDAO,
                pacienteId
        );

        return new RefreshResult(anamneseInicialAtual, inicialJaSalva);
    }

    public record RefreshResult(
            Anamnese anamneseInicialAtual,
            boolean inicialJaSalva
    ) {
    }
}
