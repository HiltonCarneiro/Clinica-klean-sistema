package br.com.clinica.service;

import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.Usuario;

public class LoginService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario autenticar(String login, String senha) {
        // validação básica
        if (login == null || login.isBlank() ||
                senha == null || senha.isBlank()) {
            return null;
        }

        // remove espaços extras
        login = login.trim();
        senha = senha.trim();

        // chama o método certo do DAO
        return usuarioDAO.buscarPorLoginESenha(login, senha);

    }
}