package br.com.clinica.controller.agenda;

import br.com.clinica.dto.AgendaFormData;
import br.com.clinica.exception.BusinessException;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Usuario;
import br.com.clinica.service.AgendaService;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class AgendaActionManager {

    private final AgendaService agendaService;
    private final Consumer<String> mensagemConsumer;

    public AgendaActionManager(AgendaService agendaService, Consumer<String> mensagemConsumer) {
        this.agendaService = agendaService;
        this.mensagemConsumer = mensagemConsumer;
    }

    public List<Agendamento> carregarAgendaDoDia(
            LocalDate data,
            Usuario profissionalSelecionado,
            boolean podeVerTodos,
            Usuario usuarioLogado
    ) {
        try {
            return agendaService.listarAgenda(
                    data,
                    profissionalSelecionado,
                    podeVerTodos,
                    usuarioLogado
            );

        } catch (BusinessException e) {
            mensagemConsumer.accept(e.getMessage());
            return List.of();

        } catch (Exception e) {
            e.printStackTrace();
            mensagemConsumer.accept("Erro ao carregar agenda: " + e.getMessage());
            return List.of();
        }
    }

    public boolean salvar(AgendaFormData formData) {
        try {
            agendaService.salvar(formData);
            mensagemConsumer.accept("Agendamento salvo com sucesso.");
            return true;

        } catch (BusinessException e) {
            mensagemConsumer.accept(e.getMessage());
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            mensagemConsumer.accept("Erro ao salvar agendamento: " + e.getMessage());
            return false;
        }
    }

    public void finalizarConsulta(Agendamento agendamento) {
        try {
            agendaService.finalizarConsulta(agendamento);
            mensagemConsumer.accept("Consulta finalizada.");

        } catch (BusinessException e) {
            mensagemConsumer.accept(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            mensagemConsumer.accept("Erro ao finalizar consulta: " + e.getMessage());
        }
    }
}