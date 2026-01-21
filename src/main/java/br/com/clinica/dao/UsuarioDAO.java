package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.Perfil;
import br.com.clinica.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // === Autenticar login ===
    public Usuario autenticar(String login, String senha) {
        String sql = """
                SELECT u.id,
                       u.nome,
                       u.login,
                       u.senha,
                       u.ativo,
                       p.id AS perfil_id,
                       p.nome AS perfil_nome
                FROM usuario u
                LEFT JOIN perfil p ON p.id = u.perfil_id
                WHERE u.login = ? AND u.senha = ? AND u.ativo = 1
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, login);
            ps.setString(2, senha);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapUsuario(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // === Listar todos ativos ===
    public List<Usuario> listarProfissionaisAtivos() {
        List<Usuario> lista = new ArrayList<>();

        String sql = """
                SELECT u.id,
                       u.nome,
                       u.login,
                       u.senha,
                       u.ativo,
                       p.id AS perfil_id,
                       p.nome AS perfil_nome
                FROM usuario u
                LEFT JOIN perfil p ON p.id = u.perfil_id
                WHERE u.ativo = 1
                ORDER BY u.nome ASC
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapUsuario(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    // === Mapear resultado para objeto Usuario ===
    private Usuario mapUsuario(ResultSet rs) throws Exception {
        Usuario u = new Usuario();

        u.setId(rs.getInt("id"));
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
}
