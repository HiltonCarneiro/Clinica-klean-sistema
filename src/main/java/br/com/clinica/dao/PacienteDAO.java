package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.Paciente;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    public void salvar(Paciente paciente) {
        String sql = """
                INSERT INTO paciente
                (nome, cpf, data_nascimento, telefone, endereco, responsavel_legal, ativo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paciente.getNome());
            ps.setString(2, paciente.getCpf());
            ps.setString(3, paciente.getDataNascimento() != null ? paciente.getDataNascimento().toString() : null);
            ps.setString(4, paciente.getTelefone());
            ps.setString(5, paciente.getEndereco());
            ps.setString(6, paciente.getResponsavelLegal());
            ps.setInt(7, paciente.isAtivo() ? 1 : 0);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar paciente: " + e.getMessage());
        }
    }

    public List<Paciente> listarTodos() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM paciente WHERE ativo = 1 ORDER BY nome";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPaciente(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar pacientes: " + e.getMessage());
        }

        return lista;
    }

    public Paciente buscarPorId(Long id) {
        String sql = "SELECT * FROM paciente WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapearPaciente(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar paciente: " + e.getMessage());
        }

        return null;
    }

    public void atualizar(Paciente paciente) {
        String sql = """
                UPDATE paciente SET
                    nome = ?,
                    cpf = ?,
                    data_nascimento = ?,
                    telefone = ?,
                    endereco = ?,
                    responsavel_legal = ?,
                    ativo = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paciente.getNome());
            ps.setString(2, paciente.getCpf());
            ps.setString(3, paciente.getDataNascimento() != null ? paciente.getDataNascimento().toString() : null);
            ps.setString(4, paciente.getTelefone());
            ps.setString(5, paciente.getEndereco());
            ps.setString(6, paciente.getResponsavelLegal());
            ps.setInt(7, paciente.isAtivo() ? 1 : 0);
            ps.setLong(8, paciente.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar paciente: " + e.getMessage());
        }
    }

    public void inativar(Long id) {
        String sql = "UPDATE paciente SET ativo = 0 WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao inativar paciente: " + e.getMessage());
        }
    }

    private Paciente mapearPaciente(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();
        p.setId(rs.getLong("id"));
        p.setNome(rs.getString("nome"));
        p.setCpf(rs.getString("cpf"));

        String dataNasc = rs.getString("data_nascimento");
        if (dataNasc != null) {
            p.setDataNascimento(LocalDate.parse(dataNasc));
        }

        p.setTelefone(rs.getString("telefone"));
        p.setEndereco(rs.getString("endereco"));
        p.setResponsavelLegal(rs.getString("responsavel_legal"));
        p.setAtivo(rs.getInt("ativo") == 1);

        return p;
    }
}
