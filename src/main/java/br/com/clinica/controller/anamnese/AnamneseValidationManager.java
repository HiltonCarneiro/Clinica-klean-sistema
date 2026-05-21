package br.com.clinica.controller.anamnese;

import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;

public class AnamneseValidationManager {

    public ValidationResult validarAntesDeSalvar(
            String tipo,
            Paciente paciente,
            Usuario usuario,
            String queixa,
            String evolucao
    ) {
        if (paciente == null) {
            return ValidationResult.invalido("Paciente não encontrado.");
        }

        if (usuario == null) {
            return ValidationResult.invalido("Usuário não autenticado.");
        }

        String queixaTratada = safe(queixa);
        String evolucaoTratada = safe(evolucao);

        if ("ANAMNESE_INICIAL".equals(tipo) && queixaTratada.isBlank()) {
            return ValidationResult.invalido("Na anamnese inicial, preencha pelo menos a queixa principal.");
        }

        if ("EVOLUCAO".equals(tipo) && queixaTratada.isBlank() && evolucaoTratada.isBlank()) {
            return ValidationResult.invalido("Na evolução, preencha pelo menos Queixa ou Evolução.");
        }

        return ValidationResult.valido();
    }

    private String safe(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public static final class ValidationResult {

        private final boolean valido;
        private final String mensagem;

        private ValidationResult(boolean valido, String mensagem) {
            this.valido = valido;
            this.mensagem = mensagem == null ? "" : mensagem;
        }

        public static ValidationResult valido() {
            return new ValidationResult(true, "");
        }

        public static ValidationResult invalido(String mensagem) {
            return new ValidationResult(false, mensagem);
        }

        public boolean isValido() {
            return valido;
        }

        public String getMensagem() {
            return mensagem;
        }
    }
}
