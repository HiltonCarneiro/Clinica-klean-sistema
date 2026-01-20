package br.com.clinica.session;

import br.com.clinica.model.Usuario;

public class Session {

    private static Usuario usuario;

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void setUsuario(Usuario u) {
        usuario = u;
    }

    public static void limpar() {
        usuario = null;
    }
}
