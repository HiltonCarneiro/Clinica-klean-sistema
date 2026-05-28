package br.com.clinica.service;

import br.com.clinica.model.Usuario;
import br.com.clinica.session.Session;
import javafx.scene.control.Label;

public class SessionViewService {

    public void atualizarUsuarioLogado(Label lblUsuarioLogado, Label lblPerfilUsuario) {
        Usuario usuario = Session.getUsuario();

        if (usuario == null) {
            setText(lblUsuarioLogado, "-");
            setText(lblPerfilUsuario, "Perfil: -");
            return;
        }

        setText(lblUsuarioLogado, obterNomeUsuario(usuario));
        setText(lblPerfilUsuario, "Perfil: " + obterNomePerfil(usuario));
    }

    public String obterNomeUsuarioLogado() {
        Usuario usuario = Session.getUsuario();

        if (usuario == null) {
            return "-";
        }

        return obterNomeUsuario(usuario);
    }

    private String obterNomeUsuario(Usuario usuario) {
        if (usuario.getPessoaNome() == null || usuario.getPessoaNome().isBlank()) {
            return usuario.getLogin() == null ? "-" : usuario.getLogin();
        }

        return usuario.getPessoaNome();
    }

    private String obterNomePerfil(Usuario usuario) {
        if (usuario.getPerfil() == null || usuario.getPerfil().getNome() == null || usuario.getPerfil().getNome().isBlank()) {
            return "-";
        }

        return usuario.getPerfil().getNome();
    }

    private void setText(Label label, String texto) {
        if (label != null) {
            label.setText(texto == null ? "" : texto);
        }
    }
}