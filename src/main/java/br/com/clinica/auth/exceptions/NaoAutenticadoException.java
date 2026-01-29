package br.com.clinica.auth.exceptions;

public class NaoAutenticadoException extends RuntimeException {
    public NaoAutenticadoException() {
        super("Usuário não autenticado!");
    }
}
