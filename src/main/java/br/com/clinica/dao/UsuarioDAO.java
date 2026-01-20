package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.Perfil;
import br.com.clinica.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public Usuario buscarPorLoginESenha(String login, String senha) {
        String sql = """
            SELECT u.id, u.nome, u.login, u.senha, u.ativo,
                   p.id AS perfil_id, p.nome AS perfil_nome
            FROM usuario u
            JOIN perfil p ON p.id = u.perfil_id
            WHERE u.login = ? AND u.senha = ? AND u.ativo = 1
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, login);
            ps.setString(2, senha);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getLong("id"));
                u.setNome(rs.getString("nome"));
                u.setLogin(rs.getString("login"));
                u.setSenha(rs.getString("senha"));
                u.setAtivo(rs.getInt("ativo") == 1);

                Perfil perfil = new Perfil();
                perfil.setId(rs.getLong("perfil_id"));
                perfil.setNome(rs.getString("perfil_nome"));

                u.setPerfil(perfil);

                return u;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao autenticar usuário: " + e.getMessage());
        }

        return null;
    }
}

