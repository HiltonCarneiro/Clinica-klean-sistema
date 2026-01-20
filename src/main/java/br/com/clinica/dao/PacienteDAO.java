package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.Paciente;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    public void salvar(Paciente paciente) {
        String sql = "INSERT INTO paciente (" +
                "nome, cpf, data_nascimento, telefone, " +
                "rua, numero, bairro, cidade, cep, uf, " +
                "responsavel_legal, ativo" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paciente.getNome());
            stmt.setString(2, paciente.getCpf());
            stmt.setString(3, paciente.getDataNascimento() != null ? paciente.getDataNascimento().toString() : null);
            stmt.setString(4, paciente.getTelefone());
            stmt.setString(5, paciente.getRua());
            stmt.setString(6, paciente.getNumero());
            stmt.setString(7, paciente.getBairro());
            stmt.setString(8, paciente.getCidade());
            stmt.setString(9, paciente.getCep());
            stmt.setString(10, paciente.getUf());
            stmt.setString(11, paciente.getResponsavelLegal());
            stmt.setInt(12, paciente.isAtivo() ? 1 : 0);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao salvar paciente", e);
        }
    }

    /**
     * Lista pacientes.
     * @param incluirInativos se true, traz TODOS; se false, só ativos.
     */
    public List<Paciente> listarTodos(boolean incluirInativos) {
        String sql;
        if (incluirInativos) {
            sql = "SELECT * FROM paciente ORDER BY nome";
        } else {
            sql = "SELECT * FROM paciente WHERE ativo = 1 ORDER BY nome";
        }

        List<Paciente> lista = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Paciente p = new Paciente();
                p.setId(rs.getLong("id"));
                p.setNome(rs.getString("nome"));
                p.setCpf(rs.getString("cpf"));

                String dataStr = rs.getString("data_nascimento");
                if (dataStr != null && !dataStr.isBlank()) {
                    p.setDataNascimento(LocalDate.parse(dataStr)); // yyyy-MM-dd
                }

                p.setTelefone(rs.getString("telefone"));
                p.setRua(rs.getString("rua"));
                p.setNumero(rs.getString("numero"));
                p.setBairro(rs.getString("bairro"));
                p.setCidade(rs.getString("cidade"));
                p.setCep(rs.getString("cep"));
                p.setUf(rs.getString("uf"));
                p.setResponsavelLegal(rs.getString("responsavel_legal"));
                p.setAtivo(rs.getInt("ativo") == 1);

                lista.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao listar pacientes", e);
        }

        return lista;
    }

    public void atualizar(Paciente paciente) {
        String sql = "UPDATE paciente SET " +
                "nome = ?, cpf = ?, data_nascimento = ?, telefone = ?, " +
                "rua = ?, numero = ?, bairro = ?, cidade = ?, cep = ?, uf = ?, " +
                "responsavel_legal = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paciente.getNome());
            stmt.setString(2, paciente.getCpf());
            stmt.setString(3, paciente.getDataNascimento() != null ? paciente.getDataNascimento().toString() : null);
            stmt.setString(4, paciente.getTelefone());
            stmt.setString(5, paciente.getRua());
            stmt.setString(6, paciente.getNumero());
            stmt.setString(7, paciente.getBairro());
            stmt.setString(8, paciente.getCidade());
            stmt.setString(9, paciente.getCep());
            stmt.setString(10, paciente.getUf());
            stmt.setString(11, paciente.getResponsavelLegal());
            stmt.setLong(12, paciente.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao atualizar paciente", e);
        }
    }

    public void inativar(Long id) {
        alterarAtivo(id, false);
    }

    public void ativar(Long id) {
        alterarAtivo(id, true);
    }

    private void alterarAtivo(Long id, boolean ativo) {
        String sql = "UPDATE paciente SET ativo = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ativo ? 1 : 0);
            stmt.setLong(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao alterar status do paciente", e);
        }
    }
}
