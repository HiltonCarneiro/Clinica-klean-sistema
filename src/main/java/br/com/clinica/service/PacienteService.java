package br.com.clinica.service;

import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.exception.BusinessException;
import br.com.clinica.model.Paciente;
import br.com.clinica.validator.PacienteValidator;

import java.util.List;

public class PacienteService {

    private final PacienteDAO pacienteDAO;
    private final PacienteValidator validator;

    public PacienteService() {
        this.pacienteDAO = new PacienteDAO();
        this.validator = new PacienteValidator();
    }

    public void salvar(Paciente paciente) {
        validator.validar(paciente);

        try {
            pacienteDAO.salvar(paciente);
        } catch (Exception e) {
            throw new BusinessException("Erro ao salvar paciente: " + e.getMessage(), e);
        }
    }

    public void atualizar(Paciente paciente) {
        if (paciente == null || paciente.getId() == null) {
            throw new BusinessException("Paciente inválido para atualização.");
        }

        salvar(paciente);
    }

    public List<Paciente> listarTodos() {
        return listarTodos(false);
    }

    public List<Paciente> listarTodos(boolean incluirInativos) {
        try {
            return pacienteDAO.listarTodos(incluirInativos);
        } catch (Exception e) {
            throw new BusinessException("Erro ao listar pacientes.", e);
        }
    }

    public List<Paciente> buscar(String termo, boolean incluirInativos) {
        List<Paciente> pacientes = listarTodos(incluirInativos);

        if (termo == null || termo.trim().isEmpty()) {
            return pacientes;
        }

        String filtro = termo.trim().toLowerCase();

        return pacientes.stream()
                .filter(paciente ->
                        contains(paciente.getNome(), filtro)
                                || contains(paciente.getCpf(), filtro)
                                || contains(paciente.getRg(), filtro)
                                || contains(paciente.getTelefone(), filtro)
                )
                .toList();
    }

    public void inativar(Paciente paciente) {
        alterarStatus(paciente, false);
    }

    public void ativar(Paciente paciente) {
        alterarStatus(paciente, true);
    }

    private void alterarStatus(Paciente paciente, boolean ativo) {
        if (paciente == null || paciente.getId() == null) {
            throw new BusinessException("Selecione um paciente válido.");
        }

        try {
            if (ativo) {
                pacienteDAO.ativar(paciente.getId());
            } else {
                pacienteDAO.inativar(paciente.getId());
            }

            paciente.setAtivo(ativo);

        } catch (Exception e) {
            throw new BusinessException("Erro ao alterar status do paciente.", e);
        }
    }

    private boolean contains(String value, String filtro) {
        return value != null && value.toLowerCase().contains(filtro);
    }
}