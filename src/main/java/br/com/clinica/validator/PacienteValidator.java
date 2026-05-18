package br.com.clinica.validator;

import br.com.clinica.exception.BusinessException;
import br.com.clinica.model.Paciente;

public class PacienteValidator {

    public void validar(Paciente paciente) {

        if (paciente == null) {
            throw new BusinessException("Paciente inválido.");
        }

        if (isBlank(paciente.getNome())) {
            throw new BusinessException("Nome do paciente é obrigatório.");
        }

        validarDocumentoObrigatorio(paciente);

        validarCpfSeInformado(paciente);

        if (isBlank(paciente.getTelefone())) {
            throw new BusinessException("Telefone é obrigatório.");
        }
    }

    private void validarDocumentoObrigatorio(Paciente paciente) {

        boolean cpfVazio = isBlank(paciente.getCpf());
        boolean rgVazio = isBlank(paciente.getRg());

        if (cpfVazio && rgVazio) {
            throw new BusinessException("Informe CPF ou RG.");
        }
    }

    private void validarCpfSeInformado(Paciente paciente) {

        String cpf = paciente.getCpf();

        if (isBlank(cpf)) {
            return;
        }

        cpf = cpf.replaceAll("\\D", "");

        if (!cpfValido(cpf)) {
            throw new BusinessException("CPF inválido.");
        }
    }

    private boolean cpfValido(String cpf) {

        if (cpf == null || cpf.length() != 11) {
            return false;
        }

        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {

            int soma = 0;
            int peso = 10;

            for (int i = 0; i < 9; i++) {
                soma += (cpf.charAt(i) - '0') * peso--;
            }

            int resto = 11 - (soma % 11);
            int digito1 = (resto == 10 || resto == 11) ? 0 : resto;

            if (digito1 != (cpf.charAt(9) - '0')) {
                return false;
            }

            soma = 0;
            peso = 11;

            for (int i = 0; i < 10; i++) {
                soma += (cpf.charAt(i) - '0') * peso--;
            }

            resto = 11 - (soma % 11);
            int digito2 = (resto == 10 || resto == 11) ? 0 : resto;

            return digito2 == (cpf.charAt(10) - '0');

        } catch (Exception e) {
            return false;
        }
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}