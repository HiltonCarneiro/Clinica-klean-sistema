package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.Nota;
import br.com.clinica.model.NotaItem;
import br.com.clinica.model.TipoItemNota;
import br.com.clinica.model.TipoMovimento;

import java.sql.*;
import java.time.LocalDate;

public class NotaDAO {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final MovimentoCaixaDAO movimentoCaixaDAO = new MovimentoCaixaDAO();

    /**
     * Salva a nota, itens, baixa estoque de produtos e registra movimento de caixa,
     * tudo numa única transação.
     */
    public void salvarNota(Nota nota) {
        String sqlNota = "INSERT INTO nota " +
                "(data_hora, id_paciente, id_profissional, forma_pagamento, total_bruto, desconto, total_liquido, observacao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlItem = "INSERT INTO nota_item " +
                "(id_nota, tipo_item, id_produto, descricao, quantidade, valor_unitario, valor_total) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Garante que totais estão coerentes com os itens
            nota.recalcularTotais();

            // 1) Inserir nota
            long idNota;
            try (PreparedStatement stmtNota = conn.prepareStatement(sqlNota, Statement.RETURN_GENERATED_KEYS)) {

                // data_hora como TEXT (ISO-8601)
                stmtNota.setString(1, nota.getDataHora().toString());
                stmtNota.setLong(2, nota.getPaciente().getId());
                stmtNota.setInt(3, nota.getProfissional().getId());
                stmtNota.setString(4, nota.getFormaPagamento());
                stmtNota.setDouble(5, nota.getTotalBruto());
                stmtNota.setDouble(6, nota.getDesconto());     // hoje sempre 0
                stmtNota.setDouble(7, nota.getTotalLiquido()); // igual ao totalBruto no seu cenário
                stmtNota.setString(8, nota.getObservacao());

                stmtNota.executeUpdate();

                try (ResultSet rs = stmtNota.getGeneratedKeys()) {
                    if (rs.next()) {
                        idNota = rs.getLong(1);
                    } else {
                        conn.rollback();
                        throw new RuntimeException("Não foi possível obter o ID da nota.");
                    }
                }
            }

            // 2) Inserir itens + baixar estoque (se for produto)
            try (PreparedStatement stmtItem = conn.prepareStatement(sqlItem)) {
                for (NotaItem item : nota.getItens()) {

                    stmtItem.setLong(1, idNota);
                    stmtItem.setString(2, item.getTipoItem().name());

                    if (item.getProduto() != null && item.getProduto().getId() != null) {
                        stmtItem.setLong(3, item.getProduto().getId());
                    } else {
                        stmtItem.setNull(3, Types.INTEGER);
                    }

                    stmtItem.setString(4, item.getDescricao());
                    stmtItem.setDouble(5, item.getQuantidade());
                    stmtItem.setDouble(6, item.getValorUnitario());
                    stmtItem.setDouble(7, item.getValorTotal());

                    stmtItem.addBatch();

                    // Se for produto, baixa estoque
                    if (item.getTipoItem() == TipoItemNota.PRODUTO && item.getProduto() != null) {
                        produtoDAO.baixarEstoque(conn, item.getProduto().getId(), item.getQuantidade());
                    }
                }

                stmtItem.executeBatch();
            }

            // 3) Registrar movimento de caixa (entrada)
            MovimentoCaixa mov = new MovimentoCaixa();
            mov.setData(LocalDate.now());
            mov.setDescricao("Atendimento / venda para " + nota.getPaciente().getNome());
            mov.setTipo(TipoMovimento.ENTRADA);
            mov.setValor(nota.getTotalLiquido());
            mov.setFormaPagamento(nota.getFormaPagamento());
            mov.setPacienteNome(nota.getPaciente().getNome());
            mov.setObservacao(nota.getObservacao());

            movimentoCaixaDAO.registrar(conn, mov);

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ignora erro de rollback, já vamos lançar a de cima
                }
            }
            throw new RuntimeException("Erro ao salvar nota e itens", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Ignora
                }
            }
        }
    }
}

