package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAO {

    public static class LinhaAuditoria {
        public long id;
        public String dataHora;
        public String usuario;     // "Pessoa Nome (login)" ou "(desconhecido)"
        public String acao;
        public String entidade;
        public String entidadeId;
        public String detalhes;
    }

    public List<LinhaAuditoria> listarUltimos(int limite) {
        String sql = """
            SELECT a.id,
                   a.data_hora,
                   COALESCE(u.pessoa_nome, '') AS pessoa_nome,
                   COALESCE(u.login, '') AS login,
                   a.acao,
                   a.entidade,
                   a.entidade_id,
                   a.detalhes
              FROM audit_log a
              LEFT JOIN usuario u ON u.id = a.usuario_id
             ORDER BY a.data_hora DESC
             LIMIT ?
        """;

        List<LinhaAuditoria> out = new ArrayList<>();

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            int lim = Math.max(10, Math.min(limite, 2000));
            ps.setInt(1, lim);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LinhaAuditoria r = new LinhaAuditoria();
                    r.id = rs.getLong("id");
                    r.dataHora = rs.getString("data_hora");

                    String pessoaNome = rs.getString("pessoa_nome");
                    String login = rs.getString("login");

                    if ((pessoaNome == null || pessoaNome.isBlank()) && (login == null || login.isBlank())) {
                        r.usuario = "(desconhecido)";
                    } else if (pessoaNome == null || pessoaNome.isBlank()) {
                        r.usuario = "(" + login + ")";
                    } else if (login == null || login.isBlank()) {
                        r.usuario = pessoaNome;
                    } else {
                        r.usuario = pessoaNome + " (" + login + ")";
                    }

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
    public void registrar(String acao, String entidade, String entidadeId, String detalhes) {
        String sql = """
        INSERT INTO audit_log (data_hora, usuario_id, acao, entidade, entidade_id, detalhes)
        VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, ?)
    """;

        Integer usuarioId = null;
        try {
            if (br.com.clinica.session.Session.getUsuario() != null) {
                usuarioId = br.com.clinica.session.Session.getUsuario().getId();
            }
        } catch (Exception ignored) {}

        try (java.sql.Connection c = br.com.clinica.database.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {

            if (usuarioId != null) {
                ps.setInt(1, usuarioId);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }

            ps.setString(2, acao);
            ps.setString(3, entidade);
            ps.setString(4, entidadeId);
            ps.setString(5, detalhes);

            ps.executeUpdate();

        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao registrar auditoria", e);
        }
    }
}