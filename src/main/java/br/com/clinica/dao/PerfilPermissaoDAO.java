package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class PerfilPermissaoDAO {

    public Set<String> listarPorPerfilId(int perfilId) {
        String sql = "SELECT permissao FROM perfil_permissao WHERE perfil_id = ?";
        Set<String> out = new HashSet<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, perfilId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString("permissao"));
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar permissões do perfil", e);
        }
    }

    public void salvarPermissoes(int perfilId, Set<String> permissoes) {
        String del = "DELETE FROM perfil_permissao WHERE perfil_id = ?";
        String ins = "INSERT INTO perfil_permissao (perfil_id, permissao) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psDel = conn.prepareStatement(del)) {
                psDel.setInt(1, perfilId);
                psDel.executeUpdate();
            }

            try (PreparedStatement psIns = conn.prepareStatement(ins)) {
                for (String p : permissoes) {
                    psIns.setInt(1, perfilId);
                    psIns.setString(2, p);
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            conn.commit();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar permissões do perfil", e);
        }
    }

    // ================== NOVO: EXCLUSÃO DE PERFIL ==================

    /** Retorna true se o perfil estiver vinculado a algum usuário OU tiver permissões cadastradas. */
    public boolean perfilEmUso(int perfilId) {
        String sqlUsuarios = "SELECT COUNT(*) FROM usuario WHERE perfil_id = ?";
        String sqlPermissoes = "SELECT COUNT(*) FROM perfil_permissao WHERE perfil_id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(sqlUsuarios)) {
                ps.setInt(1, perfilId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlPermissoes)) {
                ps.setInt(1, perfilId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar uso do perfil", e);
        }
    }

    /** Exclui o perfil (primeiro remove perfil_permissao). Deve ser chamado apenas se NÃO estiver em uso. */
    public void excluirPerfil(int perfilId) {
        String delPerm = "DELETE FROM perfil_permissao WHERE perfil_id = ?";
        String delPerfil = "DELETE FROM perfil WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(delPerm)) {
                ps1.setInt(1, perfilId);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(delPerfil)) {
                ps2.setInt(1, perfilId);
                ps2.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir perfil", e);
        }
    }
}