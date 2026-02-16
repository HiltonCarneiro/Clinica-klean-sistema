package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.session.Session;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public static class AuditRow {
        public long id;
        public String dataHora;
        public Integer usuarioId;
        public String acao;
        public String entidade;
        public String entidadeId;
        public String detalhes;
    }

    // Registra usando a Connection da transação (quando existir)
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
            else ps.setNull(2, Types.INTEGER);

            ps.setString(3, acao);
            ps.setString(4, entidade);
            ps.setString(5, entidadeId);
            ps.setString(6, detalhes);
            ps.executeUpdate();
        }
    }

    // Atalho: abre conexão e usa Session.getUsuario()
    public void registrarAuto(String acao, String entidade, String entidadeId, String detalhes) {
        Integer uid = (Session.getUsuario() != null) ? Session.getUsuario().getId() : null;

        try (Connection c = DatabaseConfig.getConnection()) {
            registrar(c, uid, acao, entidade, entidadeId, detalhes);
        } catch (SQLException e) {
            // auditoria nunca deve quebrar a operação principal
            // (se quiser, depois a gente loga isso em arquivo)
        }
    }

    // Para usar depois na tela “Auditoria”
    public List<AuditRow> listar(String entidade, String acao, String termoLivre, int limite) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, data_hora, usuario_id, acao, entidade, entidade_id, detalhes
              FROM audit_log
             WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (entidade != null && !entidade.isBlank()) {
            sql.append(" AND entidade = ? ");
            params.add(entidade.trim());
        }
        if (acao != null && !acao.isBlank()) {
            sql.append(" AND acao = ? ");
            params.add(acao.trim());
        }
        if (termoLivre != null && !termoLivre.isBlank()) {
            sql.append(" AND (LOWER(detalhes) LIKE ? OR LOWER(entidade_id) LIKE ?) ");
            String t = "%" + termoLivre.trim().toLowerCase() + "%";
            params.add(t);
            params.add(t);
        }

        sql.append(" ORDER BY data_hora DESC ");
        sql.append(" LIMIT ").append(Math.max(1, Math.min(limite, 500)));

        List<AuditRow> out = new ArrayList<>();

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditRow r = new AuditRow();
                    r.id = rs.getLong("id");
                    r.dataHora = rs.getString("data_hora");
                    int u = rs.getInt("usuario_id");
                    r.usuarioId = rs.wasNull() ? null : u;
                    r.acao = rs.getString("acao");
                    r.entidade = rs.getString("entidade");
                    r.entidadeId = rs.getString("entidade_id");
                    r.detalhes = rs.getString("detalhes");
                    out.add(r);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar audit_log", e);
        }

        return out;
    }
}