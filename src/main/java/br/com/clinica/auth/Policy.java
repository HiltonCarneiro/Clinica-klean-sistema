package br.com.clinica.auth;

import br.com.clinica.dao.PerfilPermissaoDAO;

import java.util.EnumSet;
import java.util.Set;

public final class Policy {
    private Policy() {}

    private static final PerfilPermissaoDAO dao = new PerfilPermissaoDAO();

    public static boolean temPermissao(Integer perfilId, String nomePerfil, Permissao permissao) {
        if (permissao == null) return false;

        // Admin sempre tudo
        if (nomePerfil != null && nomePerfil.trim().equalsIgnoreCase(Perfis.ADMIN)) {
            return true;
        }

        if (perfilId == null) return false;

        Set<String> permissoes = dao.listarPorPerfilId(perfilId);
        return permissoes.contains(permissao.name());
    }
}