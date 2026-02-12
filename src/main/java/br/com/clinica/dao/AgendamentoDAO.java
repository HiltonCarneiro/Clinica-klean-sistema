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

            if (ag.getPacienteId() != null) {
                ps.setInt(6, ag.getPacienteId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
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

    public void atualizar(Agendamento ag) {
        if (ag.getId() == null) {
            throw new IllegalArgumentException("Agendamento sem ID para atualizar.");
        }

        String sql = "UPDATE agendamento SET " +
                "data = ?, hora_inicio = ?, hora_fim = ?, " +
                "profissional_id = ?, profissional_nome = ?, " +
                "paciente_id = ?, paciente_nome = ?, " +
                "sala = ?, procedimento = ?, observacoes = ?, status = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ag.getData().toString());
            ps.setString(2, ag.getHoraInicio().format(HORA_FORMATTER));
            ps.setString(3, ag.getHoraFim().format(HORA_FORMATTER));
            ps.setInt(4, ag.getProfissionalId());
            ps.setString(5, ag.getProfissionalNome());

            if (ag.getPacienteId() != null) {
                ps.setInt(6, ag.getPacienteId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setString(7, ag.getPacienteNome());

            ps.setString(8, ag.getSala().name());
            ps.setString(9, ag.getProcedimento());
            ps.setString(10, ag.getObservacoes());
            ps.setString(11, ag.getStatus().name());

            ps.setInt(12, ag.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar agendamento", e);
        }
    }

    public void atualizarStatus(Integer id, StatusAgendamento status) {
        if (id == null) throw new IllegalArgumentException("ID do agendamento é obrigatório.");
        String sql = "UPDATE agendamento SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status do agendamento", e);
        }
    }

    public List<Agendamento> listarPorData(LocalDate data) {
        String sql = "SELECT * FROM agendamento WHERE data = ? ORDER BY hora_inicio";
        List<Agendamento> lista = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, data.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAgendamento(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar agendamentos", e);
        }

        return lista;
    }

    public List<Agendamento> listarPorDataEProfissional(LocalDate data, int profissionalId) {
        String sql = "SELECT * FROM agendamento WHERE data = ? AND profissional_id = ? ORDER BY hora_inicio";
        List<Agendamento> lista = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, data.toString());
            ps.setInt(2, profissionalId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAgendamento(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar agendamentos por profissional", e);
        }

        return lista;
    }

    public boolean existeConflito(Agendamento ag) {
        return existeConflito(ag, null);
    }

    /**
     * Verifica conflito de horário:
     * - mesmo profissional OU mesma sala OU mesmo paciente
     * - horários se sobrepõem
     * - ignora status CANCELADO
     * - opcionalmente ignora um ID (edição/reagendamento)
     */
    public boolean existeConflito(Agendamento ag, Integer ignorarId) {
        String base =
                "SELECT COUNT(*) FROM agendamento " +
                        "WHERE data = ? " +
                        "AND (profissional_id = ? OR sala = ? OR (paciente_id IS NOT NULL AND paciente_id = ?)) " +
                        "AND hora_inicio < ? " +
                        "AND hora_fim > ? " +
                        "AND status <> ? ";

        String sql = (ignorarId != null) ? (base + "AND id <> ?") : base;

        String dataStr = ag.getData().toString();
        String horaInicioStr = ag.getHoraInicio().format(HORA_FORMATTER);
        String horaFimStr = ag.getHoraFim().format(HORA_FORMATTER);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int i = 1;
            ps.setString(i++, dataStr);
            ps.setInt(i++, ag.getProfissionalId());
            ps.setString(i++, ag.getSala().name());

            if (ag.getPacienteId() != null) {
                ps.setInt(i++, ag.getPacienteId());
            } else {
                ps.setNull(i++, java.sql.Types.INTEGER);
            }

            ps.setString(i++, horaFimStr);      // hora_inicio < fimNovo
            ps.setString(i++, horaInicioStr);   // hora_fim > inicioNovo
            ps.setString(i++, StatusAgendamento.CANCELADO.name());

            if (ignorarId != null) {
                ps.setInt(i, ignorarId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar conflito de agenda", e);
        }

        return false;
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
        if (rs.wasNull()) {
            a.setPacienteId(null);
        } else {
            a.setPacienteId(pacienteId);
        }
        a.setPacienteNome(rs.getString("paciente_nome"));

        a.setSala(SalaAtendimento.valueOf(rs.getString("sala")));
        a.setProcedimento(rs.getString("procedimento"));
        a.setObservacoes(rs.getString("observacoes"));
        a.setStatus(StatusAgendamento.valueOf(rs.getString("status")));

        return a;
    }
}