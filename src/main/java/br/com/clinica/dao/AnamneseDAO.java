package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.Anamnese;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnamneseDAO {

    // =========================
    // INSERT
    // =========================

    public int inserir(Anamnese a) {
        String sql = """
            INSERT INTO anamnese (paciente_id, agendamento_id, profissional_id, data_hora, tipo, dados_json, observacoes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, a.getPacienteId());

            if (a.getAgendamentoId() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, a.getAgendamentoId());

            ps.setInt(3, a.getProfissionalId());
            ps.setString(4, a.getDataHora());
            ps.setString(5, a.getTipo());
            ps.setString(6, a.getDadosJson());
            ps.setString(7, a.getObservacoes());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir anamnese", e);
        }
    }

    // =========================
    // UPDATE
    // =========================

    public void atualizar(Anamnese a) {
        if (a.getId() == null) throw new IllegalArgumentException("Anamnese sem ID para atualizar.");

        String sql = """
            UPDATE anamnese
               SET profissional_id = ?,
                   data_hora = ?,
                   dados_json = ?,
                   observacoes = ?
             WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, a.getProfissionalId());
            ps.setString(2, a.getDataHora());
            ps.setString(3, a.getDadosJson());
            ps.setString(4, a.getObservacoes());
            ps.setInt(5, a.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar anamnese", e);
        }
    }

    // ======================================================
    // REGRA DE NEGÓCIO: ANAMNESE INICIAL ÚNICA (UPSERT)
    // ======================================================

    /**
     * Regra:
     * - Deve existir apenas 1 ANAMNESE_INICIAL por agendamento (se agendamentoId != null).
     * - Se não houver agendamentoId, considera 1 ANAMNESE_INICIAL por paciente (fallback).
     * - Salvar anamnese inicial deve fazer UPDATE se já existir, e INSERT se não existir.
     *
     * @return id da anamnese inicial persistida
     */
    public int salvarOuAtualizarInicial(Anamnese a) {
        if (a == null) throw new IllegalArgumentException("Anamnese nula.");
        a.setTipo("ANAMNESE_INICIAL");

        Anamnese existente = (a.getAgendamentoId() != null)
                ? buscarInicialPorAgendamento(a.getAgendamentoId())
                : buscarInicialPorPaciente(a.getPacienteId());

        if (existente == null) {
            return inserir(a);
        }

        // atualiza o registro existente
        a.setId(existente.getId());
        atualizar(a);
        return existente.getId();
    }

    public Anamnese buscarInicialPorAgendamento(Integer agendamentoId) {
        String sql = """
            SELECT * FROM anamnese
             WHERE agendamento_id = ?
               AND tipo = 'ANAMNESE_INICIAL'
             ORDER BY data_hora DESC
             LIMIT 1
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, agendamentoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar anamnese inicial por agendamento", e);
        }
    }

    public Anamnese buscarInicialPorPaciente(Long pacienteId) {
        String sql = """
            SELECT * FROM anamnese
             WHERE paciente_id = ?
               AND tipo = 'ANAMNESE_INICIAL'
             ORDER BY data_hora DESC
             LIMIT 1
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, pacienteId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar anamnese inicial por paciente", e);
        }
    }

    // =========================
    // LISTAGENS
    // =========================

    public List<Anamnese> listarPorPaciente(Long pacienteId) {
        String sql = "SELECT * FROM anamnese WHERE paciente_id = ? ORDER BY data_hora DESC";
        List<Anamnese> list = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, pacienteId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar anamneses do paciente", e);
        }
    }

    public List<Anamnese> listarPorAgendamento(Integer agendamentoId) {
        String sql = "SELECT * FROM anamnese WHERE agendamento_id = ? ORDER BY data_hora DESC";
        List<Anamnese> list = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, agendamentoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar anamneses do agendamento", e);
        }
    }

    public Anamnese buscarPorId(int id) {
        String sql = "SELECT * FROM anamnese WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar anamnese por id", e);
        }
    }

    private Anamnese map(ResultSet rs) throws SQLException {
        Anamnese a = new Anamnese();
        a.setId(rs.getInt("id"));
        a.setPacienteId(rs.getLong("paciente_id"));

        int agId = rs.getInt("agendamento_id");
        a.setAgendamentoId(rs.wasNull() ? null : agId);

        a.setProfissionalId(rs.getInt("profissional_id"));
        a.setDataHora(rs.getString("data_hora"));
        a.setTipo(rs.getString("tipo"));
        a.setDadosJson(rs.getString("dados_json"));
        a.setObservacoes(rs.getString("observacoes"));
        return a;
    }
}