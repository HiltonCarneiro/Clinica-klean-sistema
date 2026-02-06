package br.com.clinica.auth;

import br.com.clinica.auth.exceptions.AcessoNegadoException;
import br.com.clinica.auth.exceptions.NaoAutenticadoException;
import br.com.clinica.model.Perfil;
import br.com.clinica.model.Usuario;
import br.com.clinica.session.Session;

public final class AuthGuard {
    private AuthGuard() {}

    public static Usuario exigirLogin() {
        Usuario u = Session.getUsuario();
        if (u == null) throw new NaoAutenticadoException();
        return u;
    }

    public static Usuario exigirPermissao(Permissao permissao) {
        Usuario u = exigirLogin();

        Perfil perfil = u.getPerfil();
        Integer perfilId = null;
        String perfilNome = null;

        if (perfil != null) {
            perfilNome = perfil.getNome();
            if (perfil.getId() != null) {
                // Perfil.id é Long no seu model
                perfilId = Math.toIntExact(perfil.getId());
            }
        }

        if (!Policy.temPermissao(perfilId, perfilNome, permissao)) {
            throw new AcessoNegadoException();
        }

        return u;
    }
}