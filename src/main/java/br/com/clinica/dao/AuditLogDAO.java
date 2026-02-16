package br.com.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AuditLogDAO {

    public void registrar(Connection conn,
                          Integer usuarioId,
                          String acao,
                          String entidade,
                          String entidadeId,
                          String detalhes) throws SQLException {

        String sql = "INSERT INTO audit_log (data_hora, usuario_id, acao, entidade, entidade_id, detalhes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().toString());

            if (usuarioId != null) ps.setInt(2, usuarioId);
            else ps.setNull(2, java.sql.Types.INTEGER);

            ps.setString(3, acao);
            ps.setString(4, entidade);
            ps.setString(5, entidadeId);
            ps.setString(6, detalhes);

            ps.executeUpdate();
        }
    }
}
