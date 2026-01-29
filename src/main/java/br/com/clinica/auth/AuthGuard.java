package br.com.clinica.auth;

import br.com.clinica.auth.exceptions.AcessoNegadoException;
import br.com.clinica.auth.exceptions.NaoAutenticadoException;
import br.com.clinica.model.Usuario;
import br.com.clinica.session.Session;

//aqui lança as exceções
public final class AuthGuard {
    private AuthGuard() {}

    public static Usuario exigirLogin() {
        Usuario u = Session.getUsuario();
        if (u == null) throw new NaoAutenticadoException();
        return u;
    }

    public static Usuario exigirPermissao(Permissao permissao) {
        Usuario u = exigirLogin();
        String perfil = (u.getPerfil() != null) ? u.getPerfil().getNome() : null;

        if (!Policy.temPermissao(perfil, permissao)) {
            throw new AcessoNegadoException();
        }
        return u;
    }
}
