package br.com.clinica.controller.relatorios;

import br.com.clinica.dao.AgendamentoDAO;
import br.com.clinica.dao.MovimentoCaixaDAO;
import br.com.clinica.dao.NotaDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.Usuario;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;

public class RelatoriosSearchManager {

    public void buscarCaixa(
            LocalDate inicio,
            LocalDate fim,
            MovimentoCaixaDAO movimentoCaixaDAO,
            ObservableList<MovimentoCaixa> caixaObs,
            RelatoriosValidationManager validationManager,
            RelatoriosCaixaSummaryManager caixaSummaryManager,
            Label lblCxEntradas,
            Label lblCxSaidas,
            Label lblCxSaldo,
            BiConsumer<String, String> erroConsumer
    ) {
        try {
            RelatoriosValidationManager.ValidationResult validacao =
                    validationManager.validarPeriodo(inicio, fim);

            if (!validacao.isValido()) {
                erroConsumer.accept(validacao.getTitulo(), validacao.getMensagem());
                return;
            }

            List<MovimentoCaixa> lista =
                    movimentoCaixaDAO.listarPorPeriodo(inicio, fim);

            caixaObs.setAll(lista);

            caixaSummaryManager.atualizarResumo(
                    lista,
                    lblCxEntradas,
                    lblCxSaidas,
                    lblCxSaldo
            );

        } catch (Exception e) {
            e.printStackTrace();
            erroConsumer.accept("Erro ao buscar caixa", e.getMessage());
        }
    }

    public void buscarAgendamentos(
            LocalDate inicio,
            LocalDate fim,
            Usuario profissional,
            AgendamentoDAO agendamentoDAO,
            ObservableList<Agendamento> agObs,
            RelatoriosValidationManager validationManager,
            BiConsumer<String, String> erroConsumer
    ) {
        try {
            RelatoriosValidationManager.ValidationResult validacao =
                    validationManager.validarPeriodo(inicio, fim);

            if (!validacao.isValido()) {
                erroConsumer.accept(validacao.getTitulo(), validacao.getMensagem());
                return;
            }

            List<Agendamento> lista =
                    profissional == null
                            ? agendamentoDAO.listarPorPeriodo(inicio, fim)
                            : agendamentoDAO.listarPorPeriodoEProfissional(
                            inicio,
                            fim,
                            profissional.getId()
                    );

            agObs.setAll(lista);

        } catch (Exception e) {
            e.printStackTrace();
            erroConsumer.accept("Erro ao buscar agendamentos", e.getMessage());
        }
    }

    public void buscarNotas(
            LocalDate inicio,
            LocalDate fim,
            String pacienteLike,
            Usuario profissional,
            String formaPagamento,
            String formaTodas,
            NotaDAO notaDAO,
            ObservableList<NotaDAO.NotaResumo> notasObs,
            RelatoriosValidationManager validationManager,
            BiConsumer<String, String> erroConsumer
    ) {
        try {
            RelatoriosValidationManager.ValidationResult validacao =
                    validationManager.validarPeriodo(inicio, fim);

            if (!validacao.isValido()) {
                erroConsumer.accept(validacao.getTitulo(), validacao.getMensagem());
                return;
            }

            Integer profissionalId =
                    profissional == null ? null : profissional.getId();

            String forma =
                    formaTodas.equals(formaPagamento) ? null : formaPagamento;

            List<NotaDAO.NotaResumo> lista =
                    notaDAO.listarNotasResumo(
                            inicio,
                            fim,
                            pacienteLike,
                            profissionalId,
                            forma
                    );

            notasObs.setAll(lista);

        } catch (Exception e) {
            e.printStackTrace();
            erroConsumer.accept("Erro ao buscar notas", e.getMessage());
        }
    }
}