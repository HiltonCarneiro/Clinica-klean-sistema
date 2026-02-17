package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminUsuariosPerfisDAO {

    private final AuditoriaDAO auditoria = new AuditoriaDAO();

    public static class PerfilRow {
        public int id;
        public String nome;
        @Override public String toString() { return nome; }
    }

    public static class UsuarioRow {
        public int id;
        public String nome;        // cargo
        public String pessoaNome;
        public String login;
        public String senha;
        public boolean ativo;
        public int perfilId;
        public String perfilNome;
    }

    // ===================== PERFIS =====================

    public List<PerfilRow> listarPerfis() {
        String sql = "SELECT id, nome FROM perfil ORDER BY nome";
        List<PerfilRow> out = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PerfilRow p = new PerfilRow();
                p.id = rs.getInt("id");
                p.nome = rs.getString("nome");
                out.add(p);
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar perfis", e);
        }
    }

    public void criarPerfil(String nome) {
        String sql = "INSERT INTO perfil (nome) VALUES (?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nome);
            ps.executeUpdate();

            int id = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) id = rs.getInt(1);
            }

            auditoria.registrar("CRIAR", "PERFIL", String.valueOf(id), "nome=" + nome);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar perfil", e);
        }
    }

    public void renomearPerfil(int perfilId, String novoNome) {
        String sql = "UPDATE perfil SET nome=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, novoNome);
            ps.setInt(2, perfilId);
            ps.executeUpdate();

            auditoria.registrar("EDITAR", "PERFIL", String.valueOf(perfilId), "novoNome=" + novoNome);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao renomear perfil", e);
        }
    }

    // ===================== USUÁRIOS =====================

    public List<UsuarioRow> listarUsuarios(String termo, boolean incluirInativos) {
        String base = """
            SELECT u.id, u.nome, u.pessoa_nome, u.login, u.senha, u.ativo, u.perfil_id, p.nome AS perfil_nome
              FROM usuario u
              LEFT JOIN perfil p ON p.id = u.perfil_id
             WHERE 1=1
            """;

        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder();

        if (!incluirInativos) where.append(" AND u.ativo = 1 ");

        if (termo != null && !termo.trim().isBlank()) {
            where.append(" AND (LOWER(u.login) LIKE ? OR LOWER(u.pessoa_nome) LIKE ? OR LOWER(u.nome) LIKE ?)");
            String t = "%" + termo.trim().toLowerCase() + "%";
            params.add(t); params.add(t); params.add(t);
        }

        String sql = base + where + " ORDER BY u.ativo DESC, u.login";

        List<UsuarioRow> out = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UsuarioRow u = new UsuarioRow();
                    u.id = rs.getInt("id");
                    u.nome = rs.getString("nome");
                    u.pessoaNome = rs.getString("pessoa_nome");
                    u.login = rs.getString("login");
                    u.senha = rs.getString("senha");
                    u.ativo = rs.getInt("ativo") == 1;
                    u.perfilId = rs.getInt("perfil_id");
                    u.perfilNome = rs.getString("perfil_nome");
                    out.add(u);
                }
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar usuários", e);
        }
    }

    public void inserirUsuario(UsuarioRow u) {
        String sql = """
            INSERT INTO usuario (nome, pessoa_nome, login, senha, ativo, perfil_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.nome);
            ps.setString(2, u.pessoaNome);
            ps.setString(3, u.login);
            ps.setString(4, u.senha);
            ps.setInt(5, u.ativo ? 1 : 0);
            ps.setInt(6, u.perfilId);
            ps.executeUpdate();

            int id = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) id = rs.getInt(1);
            }

            auditoria.registrar("CRIAR", "USUARIO", String.valueOf(id),
                    "login=" + u.login + ", pessoa=" + u.pessoaNome + ", perfilId=" + u.perfilId);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir usuário", e);
        }
    }

    public void atualizarUsuario(UsuarioRow u) {
        String sql = """
            UPDATE usuario
               SET nome=?, pessoa_nome=?, login=?, senha=?, ativo=?, perfil_id=?
             WHERE id=?
            """;
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u.nome);
            ps.setString(2, u.pessoaNome);
            ps.setString(3, u.login);
            ps.setString(4, u.senha);
            ps.setInt(5, u.ativo ? 1 : 0);
            ps.setInt(6, u.perfilId);
            ps.setInt(7, u.id);
            ps.executeUpdate();

            auditoria.registrar("EDITAR", "USUARIO", String.valueOf(u.id),
                    "login=" + u.login + ", pessoa=" + u.pessoaNome + ", ativo=" + u.ativo + ", perfilId=" + u.perfilId);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário", e);
        }
    }

    public void ativarInativarUsuario(int usuarioId, boolean novoAtivo) {
        String sql = "UPDATE usuario SET ativo=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, novoAtivo ? 1 : 0);
            ps.setInt(2, usuarioId);
            ps.executeUpdate();

            auditoria.registrar(novoAtivo ? "ATIVAR" : "INATIVAR",
                    "USUARIO",
                    String.valueOf(usuarioId),
                    "status=" + (novoAtivo ? "ativo" : "inativo"));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao ativar/inativar usuário", e);
        }
    }

    public void resetSenha(int usuarioId, String novaSenha) {
        String sql = "UPDATE usuario SET senha=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, novaSenha);
            ps.setInt(2, usuarioId);
            ps.executeUpdate();

            auditoria.registrar("RESET_SENHA", "USUARIO", String.valueOf(usuarioId), "senha resetada");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao resetar senha", e);
        }
    }
}