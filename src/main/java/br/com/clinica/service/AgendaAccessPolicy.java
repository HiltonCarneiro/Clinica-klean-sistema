package br.com.clinica.service;

import br.com.clinica.auth.Perfis;
import br.com.clinica.model.Usuario;

public class AgendaAccessPolicy {

    public boolean podeVerTodos(Usuario usuario) {

        if (usuario == null || usuario.getPerfil() == null || usuario.getPerfil().getNome() == null) {
            return true;
        }

        String perfil = usuario.getPerfil().getNome();

        return Perfis.ADMIN.equals(perfil)
                || Perfis.RECEPCIONISTA.equals(perfil);
    }

    public boolean podeFinalizarConsulta(Usuario usuario) {

        if (usuario == null || usuario.getPerfil() == null || usuario.getPerfil().getNome() == null) {
            return false;
        }

        String perfil = usuario.getPerfil().getNome();

        return Perfis.ADMIN.equals(perfil)
                || !Perfis.RECEPCIONISTA.equals(perfil);
    }
}