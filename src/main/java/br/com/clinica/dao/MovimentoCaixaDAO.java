package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.enums.TipoMovimento;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovimentoCaixaDAO {

    public void registrar(Connection conn, MovimentoCaixa mov) throws SQLException {
        String sql = "INSERT INTO movimento_caixa " +
                "(data, descricao, tipo, valor, forma_pagamento, paciente_nome, observacao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mov.getData().toString()); // LocalDate -> TEXT
            stmt.setString(2, mov.getDescricao());
            stmt.setString(3, mov.getTipo().name());
            stmt.setDouble(4, mov.getValor());
            stmt.setString(5, mov.getFormaPagamento());
            stmt.setString(6, mov.getPacienteNome());
            stmt.setString(7, mov.getObservacao());
            stmt.executeUpdate();
        }
    }

    public void registrar(MovimentoCaixa mov) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            registrar(conn, mov);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar movimento de caixa", e);
        }
    }

    public List<MovimentoCaixa> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        List<MovimentoCaixa> lista = new ArrayList<>();

        String sql = "SELECT id, data, descricao, tipo, valor, forma_pagamento, " +
                "paciente_nome, observacao " +
                "FROM movimento_caixa " +
                "WHERE date(data) BETWEEN ? AND ? " +
                "ORDER BY data, id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, inicio.toString());
            stmt.setString(2, fim.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MovimentoCaixa mov = new MovimentoCaixa();
                    mov.setId(rs.getLong("id"));

                    String dataStr = rs.getString("data");
                    if (dataStr != null) {
                        mov.setData(LocalDate.parse(dataStr));
                    }

                    mov.setDescricao(rs.getString("descricao"));

                    String tipoStr = rs.getString("tipo");
                    if (tipoStr != null) {
                        mov.setTipo(TipoMovimento.valueOf(tipoStr));
                    }

                    mov.setValor(rs.getDouble("valor"));
                    mov.setFormaPagamento(rs.getString("forma_pagamento"));
                    mov.setPacienteNome(rs.getString("paciente_nome"));
                    mov.setObservacao(rs.getString("observacao"));

                    lista.add(mov);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar movimentos de caixa", e);
        }

        return lista;
    }
}
