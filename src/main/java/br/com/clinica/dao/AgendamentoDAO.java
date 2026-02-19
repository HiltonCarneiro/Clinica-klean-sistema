package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.enums.SalaAtendimento;
import br.com.clinica.model.enums.StatusAgendamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AgendamentoDAO {

    private static final DateTimeFormatter HORA_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    public void salvar(Agendamento ag) {
        String sql = "INSERT INTO agendamento (" +
                "data, hora_inicio, hora_fim, " +
                "profissional_id, profissional_nome, " +
                "paciente_id, paciente_nome, " +
                "sala, procedimento, observacoes, status" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ag.getData().toString());
            ps.setString(2, ag.getHoraInicio().format(HORA_FORMATTER));
            ps.setString(3, ag.getHoraFim().format(HORA_FORMATTER));
            ps.setInt(4, ag.getProfissionalId());
            ps.setString(5, ag.getProfissionalNome());

            if (ag.getPacienteId() != null) ps.setInt(6, ag.getPacienteId());
            else ps.setNull(6, java.sql.Types.INTEGER);

            ps.setString(7, ag.getPacienteNome());
            ps.setString(8, ag.getSala().name());
            ps.setString(9, ag.getProcedimento());
            ps.setString(10, ag.getObservacoes());
            ps.setString(11, ag.getStatus().name());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar agendamento", e);
        }
    }

    public List<Agendamento> listarPorData(LocalDate data) {
        // ✅ agora NÃO traz CONCLUIDO (assim finalizado some da agenda)
        String sql = "SELECT * FROM agendamento WHERE data = ? AND status <> ? ORDER BY hora_inicio";
        List<Agendamento> lista = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, data.toString());
            ps.setString(2, StatusAgendamento.CONCLUIDO.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearAgendamento(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar agendamentos", e);
        }

        return lista;
    }

    public List<Agendamento> listarPorDataEProfissional(LocalDate data, int profissionalId) {
        // ✅ agora NÃO traz CONCLUIDO
        String sql = "SELECT * FROM agendamento WHERE data = ? AND profissional_id = ? AND status <> ? ORDER BY hora_inicio";
        List<Agendamento> lista = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, data.toString());
            ps.setInt(2, profissionalId);
            ps.setString(3, StatusAgendamento.CONCLUIDO.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearAgendamento(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar agendamentos por profissional", e);
        }

        return lista;
    }

    public void atualizarStatus(int agendamentoId, StatusAgendamento status) {
        String sql = "UPDATE agendamento SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, agendamentoId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status do agendamento", e);
        }
    }

    public void finalizarConsulta(int agendamentoId) {
        atualizarStatus(agendamentoId, StatusAgendamento.CONCLUIDO);
    }

    public boolean existeConflito(Agendamento ag) {
        String sql = "SELECT COUNT(1) " +
                "FROM agendamento " +
                "WHERE data = ? AND status <> ? " +
                "AND ( (profissional_id = ?) OR (sala = ?) OR (paciente_id = ?) ) " +
                "AND (hora_inicio < ? AND hora_fim > ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ag.getData().toString());
            ps.setString(2, StatusAgendamento.CANCELADO.name());
            ps.setInt(3, ag.getProfissionalId());
            ps.setString(4, ag.getSala().name());

            if (ag.getPacienteId() != null) ps.setInt(5, ag.getPacienteId());
            else ps.setNull(5, java.sql.Types.INTEGER);

            ps.setString(6, ag.getHoraFim().format(HORA_FORMATTER));
            ps.setString(7, ag.getHoraInicio().format(HORA_FORMATTER));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar conflito de agenda", e);
        }

        return false;
    }

    public List<Agendamento> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        String sql = "SELECT * FROM agendamento " +
                "WHERE date(data) BETWEEN ? AND ? " +
                "AND status <> ? " +
                "ORDER BY data, hora_inicio";

        List<Agendamento> lista = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, inicio.toString());
            ps.setString(2, fim.toString());
            ps.setString(3, StatusAgendamento.CONCLUIDO.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAgendamento(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar agendamentos por período", e);
        }

        return lista;
    }

    public List<Agendamento> listarPorPeriodoEProfissional(LocalDate inicio, LocalDate fim, Integer profissionalId) {
        String sql = "SELECT * FROM agendamento " +
                "WHERE date(data) BETWEEN ? AND ? " +
                "AND profissional_id = ? " +
                "AND status <> ? " +
                "ORDER BY data, hora_inicio";

        List<Agendamento> lista = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, inicio.toString());
            ps.setString(2, fim.toString());
            ps.setInt(3, profissionalId);
            ps.setString(4, StatusAgendamento.CONCLUIDO.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAgendamento(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar agendamentos por período e profissional", e);
        }

        return lista;
    }

    private Agendamento mapearAgendamento(ResultSet rs) throws SQLException {
        Agendamento a = new Agendamento();
        a.setId(rs.getInt("id"));
        a.setData(LocalDate.parse(rs.getString("data")));
        a.setHoraInicio(LocalTime.parse(rs.getString("hora_inicio"), HORA_FORMATTER));
        a.setHoraFim(LocalTime.parse(rs.getString("hora_fim"), HORA_FORMATTER));
        a.setProfissionalId(rs.getInt("profissional_id"));
        a.setProfissionalNome(rs.getString("profissional_nome"));

        int pacienteId = rs.getInt("paciente_id");
        if (rs.wasNull()) a.setPacienteId(null);
        else a.setPacienteId(pacienteId);

        a.setPacienteNome(rs.getString("paciente_nome"));
        a.setSala(SalaAtendimento.valueOf(rs.getString("sala")));
        a.setProcedimento(rs.getString("procedimento"));
        a.setObservacoes(rs.getString("observacoes"));
        a.setStatus(StatusAgendamento.valueOf(rs.getString("status")));
        return a;
    }
}