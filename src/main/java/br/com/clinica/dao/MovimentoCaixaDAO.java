package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.MovimentoCaixa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MovimentoCaixaDAO {

    /**
     * Usado quando já existe uma Connection aberta (por exemplo dentro de uma transação da Nota).
     */
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

    /**
     * Versão conveniente caso você queira registrar um movimento isolado,
     * fora do fluxo da Nota.
     */
    public void registrar(MovimentoCaixa mov) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            registrar(conn, mov);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar movimento de caixa", e);
        }
    }
}

