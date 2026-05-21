package br.com.clinica.controller.relatorios;

import java.time.LocalDate;

public class RelatoriosValidationManager {

    public ValidationResult validarPeriodo(LocalDate inicio, LocalDate fim) {

        if (inicio == null || fim == null) {
            return ValidationResult.erro(
                    "Período inválido",
                    "Informe data inicial e final."
            );
        }

        if (fim.isBefore(inicio)) {
            return ValidationResult.erro(
                    "Período inválido",
                    "A data final não pode ser menor que a inicial."
            );
        }

        return ValidationResult.ok();
    }

    public static class ValidationResult {

        private final boolean valido;
        private final String titulo;
        private final String mensagem;

        private ValidationResult(boolean valido, String titulo, String mensagem) {
            this.valido = valido;
            this.titulo = titulo;
            this.mensagem = mensagem;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, "", "");
        }

        public static ValidationResult erro(String titulo, String mensagem) {
            return new ValidationResult(false, titulo, mensagem);
        }

        public boolean isValido() {
            return valido;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getMensagem() {
            return mensagem;
        }
    }
}