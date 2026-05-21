package br.com.clinica.controller.anamnese;

import br.com.clinica.dao.AnamneseDAO;
import br.com.clinica.dao.AnexoPacienteDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Anamnese;
import br.com.clinica.model.Paciente;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.List;
import java.util.function.Consumer;

public class AnamneseLoadManager {

    public void carregarTudoAsync(
            Agendamento agendamento,
            PacienteDAO pacienteDAO,
            AnamneseDAO anamneseDAO,
            AnexoPacienteDAO anexoDAO,
            Consumer<LoadResult> onSuccess,
            Consumer<String> onError
    ) {
        Task<LoadResult> task = new Task<>() {
            @Override
            protected LoadResult call() {
                Paciente paciente = null;
                Anamnese anamneseInicial = null;
                List<Anamnese> historico = List.of();
                List<AnexoPacienteDAO.AnexoPacienteItem> anexos = List.of();

                Integer pacienteId = agendamento != null ? agendamento.getPacienteId() : null;

                if (pacienteId != null) {
                    paciente = pacienteDAO.buscarPorId(Long.valueOf(pacienteId));
                }

                if (paciente != null && paciente.getId() != null) {
                    anamneseInicial = anamneseDAO.buscarInicialPorPaciente(paciente.getId());
                    historico = anamneseDAO.listarPorPaciente(paciente.getId());
                    anexos = anexoDAO.listarPorPaciente(paciente.getId());
                }

                return new LoadResult(paciente, anamneseInicial, historico, anexos);
            }

            @Override
            protected void succeeded() {
                LoadResult resultado = getValue();

                if (onSuccess != null) {
                    Platform.runLater(() -> onSuccess.accept(resultado));
                }
            }

            @Override
            protected void failed() {
                Throwable ex = getException();

                if (ex != null) {
                    ex.printStackTrace();
                }

                if (onError != null) {
                    Platform.runLater(() -> onError.accept(ex != null ? ex.getMessage() : ""));
                }
            }
        };

        Thread thread = new Thread(task, "anamnese-load");
        thread.setDaemon(true);
        thread.start();
    }

    public record LoadResult(
            Paciente paciente,
            Anamnese anamneseInicial,
            List<Anamnese> historico,
            List<AnexoPacienteDAO.AnexoPacienteItem> anexos
    ) {
    }
}
