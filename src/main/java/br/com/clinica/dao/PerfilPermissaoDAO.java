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
}