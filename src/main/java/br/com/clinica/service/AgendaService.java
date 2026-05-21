package br.com.clinica.service;

import br.com.clinica.dao.AgendamentoDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.ProcedimentoFrequenteDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.dto.AgendaFormData;
import br.com.clinica.exception.BusinessException;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.model.enums.SalaAtendimento;
import br.com.clinica.model.enums.StatusAgendamento;
import br.com.clinica.validator.AgendaValidator;

import java.time.LocalDate;
import java.util.List;

public class AgendaService {

    private static final int DIAS_PARA_CANCELAMENTO_AUTOMATICO = 3;

    private final AgendamentoDAO agendamentoDAO;
    private final UsuarioDAO usuarioDAO;
    private final PacienteDAO pacienteDAO;
    private final ProcedimentoFrequenteDAO procedimentoDAO;
    private final AgendaValidator validator;

    public AgendaService() {
        this.agendamentoDAO = new AgendamentoDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.pacienteDAO = new PacienteDAO();
        this.procedimentoDAO = new ProcedimentoFrequenteDAO();
        this.validator = new AgendaValidator();
    }

    public List<Usuario> listarProfissionaisAtivos() {
        return usuarioDAO.listarProfissionaisAtivos();
    }

    public List<Paciente> listarPacientes() {
        return pacienteDAO.listarTodos();
    }

    public List<String> sugerirProcedimentos(
            String termo,
            SalaAtendimento sala,
            int limite
    ) {
        return procedimentoDAO.sugerir(termo, sala, limite);
    }

    public String normalizarProcedimento(String procedimento) {
        return procedimentoDAO.normalizar(procedimento);
    }

    public void salvar(AgendaFormData data) {
        cancelarAgendamentosAntigosPendentes();

        String procedimentoNormalizado = normalizarProcedimento(data.getProcedimento());
        data.setProcedimento(procedimentoNormalizado);

        validator.validarParaSalvar(data);

        Agendamento agendamento = montarAgendamento(data);

        if (agendamentoDAO.existeConflito(agendamento)) {
            throw new BusinessException(
                    "Conflito de agenda: já existe agendamento nesse horário para profissional, sala ou paciente."
            );
        }

        agendamentoDAO.salvar(agendamento);
        procedimentoDAO.registrarUso(data.getProcedimento(), data.getSala());
    }

    public List<Agendamento> listarAgenda(
            LocalDate data,
            Usuario profissionalSelecionado,
            boolean podeVerTodos,
            Usuario usuarioLogado
    ) {
        cancelarAgendamentosAntigosPendentes();

        if (data == null) {
            throw new BusinessException("Selecione uma data para visualizar a agenda.");
        }

        if (data.isBefore(LocalDate.now())) {
            throw new BusinessException("Não é permitido selecionar data no passado.");
        }

        if (podeVerTodos) {

            if (profissionalSelecionado != null) {
                return agendamentoDAO.listarPorDataEProfissional(
                        data,
                        Math.toIntExact(profissionalSelecionado.getId())
                );
            }

            return agendamentoDAO.listarPorData(data);
        }

        if (usuarioLogado != null) {
            return agendamentoDAO.listarPorDataEProfissional(
                    data,
                    Math.toIntExact(usuarioLogado.getId())
            );
        }

        return agendamentoDAO.listarPorData(data);
    }

    public void finalizarConsulta(Agendamento agendamento) {
        if (agendamento == null) {
            throw new BusinessException("Selecione um agendamento para finalizar.");
        }

        agendamentoDAO.finalizarConsulta(agendamento.getId());
    }

    public void cancelarAgendamentosAntigosPendentes() {
        LocalDate dataLimite = LocalDate.now().minusDays(DIAS_PARA_CANCELAMENTO_AUTOMATICO);
        agendamentoDAO.cancelarAgendamentosAntigosPendentes(dataLimite);
    }

    private Agendamento montarAgendamento(AgendaFormData data) {
        Agendamento agendamento = new Agendamento();

        agendamento.setData(data.getData());
        agendamento.setHoraInicio(data.getHoraInicio());
        agendamento.setHoraFim(data.getHoraFim());

        agendamento.setProfissionalId(
                Math.toIntExact(data.getProfissional().getId())
        );

        agendamento.setProfissionalNome(
                resolverNomeProfissional(data.getProfissional())
        );

        if (data.getPaciente() != null) {
            agendamento.setPacienteId(
                    Math.toIntExact(data.getPaciente().getId())
            );
            agendamento.setPacienteNome(data.getPaciente().getNome());
        } else {
            agendamento.setPacienteId(null);
            agendamento.setPacienteNome(null);
        }

        agendamento.setSala(data.getSala());
        agendamento.setProcedimento(data.getProcedimento());
        agendamento.setObservacoes(
                data.getObservacoes() == null ? "" : data.getObservacoes().trim()
        );
        agendamento.setStatus(StatusAgendamento.AGENDADO);

        return agendamento;
    }

    private String resolverNomeProfissional(Usuario usuario) {
        if (usuario == null) {
            return "";
        }

        if (usuario.getPessoaNome() != null && !usuario.getPessoaNome().isBlank()) {
            return usuario.getPessoaNome();
        }

        return usuario.getNome();
    }
}