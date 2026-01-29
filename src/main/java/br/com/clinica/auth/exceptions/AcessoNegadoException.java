package br.com.clinica.auth.exceptions;

public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException() {
        super("Acesso negado para este perfil.");
    }
}
