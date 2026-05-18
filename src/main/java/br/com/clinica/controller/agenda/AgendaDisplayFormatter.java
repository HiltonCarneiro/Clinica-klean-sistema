package br.com.clinica.controller.agenda;

import br.com.clinica.model.Usuario;

public class AgendaDisplayFormatter {

    public String profissional(Usuario usuario) {

        if (usuario == null) {
            return "";
        }

        String pessoa = safe(usuario.getPessoaNome());
        String cargo = safe(usuario.getNome());

        if (pessoa.isBlank() && !safe(usuario.getLogin()).isBlank()) {
            pessoa = usuario.getLogin();
        }

        if (cargo.isBlank()) {
            return pessoa;
        }

        return pessoa + " (" + cargo + ")";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}