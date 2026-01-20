package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.Produto;
import br.com.clinica.model.TipoProduto;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    public void salvar(Produto p) {
        if (p.getId() == null) {
            inserir(p);
        } else {
            atualizar(p);
        }
    }

    private void inserir(Produto p) {
        String sql = """
                INSERT INTO produto
                (nome, tipo, unidade, estoque_atual, estoque_minimo,
                 lote, validade, preco_custo, preco_venda, ativo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatement(p, stmt);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void atualizar(Produto p) {
        String sql = """
                UPDATE produto SET
                    nome           = ?,
                    tipo           = ?,
                    unidade        = ?,
                    estoque_atual  = ?,
                    estoque_minimo = ?,
                    lote           = ?,
                    validade       = ?,
                    preco_custo    = ?,
                    preco_venda    = ?,
                    ativo          = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preencherStatement(p, stmt);
            stmt.setInt(11, p.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void preencherStatement(Produto p, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, p.getNome());
        stmt.setString(2, p.getTipo() != null ? p.getTipo().toDatabase() : null); // SUPLEMENTO/ORAL/INJETAVEL/TOPICO
        stmt.setString(3, p.getUnidade());
        stmt.setDouble(4, p.getEstoqueAtual());
        stmt.setDouble(5, p.getEstoqueMinimo());
        stmt.setString(6, p.getLote());
        stmt.setString(7, p.getValidade() != null ? p.getValidade().toString() : null); // yyyy-MM-dd

        if (p.getPrecoCusto() != null) {
            stmt.setDouble(8, p.getPrecoCusto());
        } else {
            stmt.setNull(8, Types.REAL);
        }

        if (p.getPrecoVenda() != null) {
            stmt.setDouble(9, p.getPrecoVenda());
        } else {
            stmt.setNull(9, Types.REAL);
        }

        stmt.setInt(10, p.isAtivo() ? 1 : 0);
    }

    public List<Produto> listar(boolean incluirInativos, boolean apenasBaixoEstoque, boolean vencendo30Dias) {
        List<Produto> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT * FROM produto WHERE 1=1
                """);

        if (!incluirInativos) {
            sql.append(" AND ativo = 1 ");
        }

        if (apenasBaixoEstoque) {
            sql.append(" AND estoque_atual <= estoque_minimo ");
        }

        if (vencendo30Dias) {
            sql.append(" AND validade IS NOT NULL ");
            sql.append(" AND validade <= date('now', '+30 day') ");
        }

        sql.append(" ORDER BY nome ");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        Produto p = new Produto();

        p.setId(rs.getInt("id"));
        p.setNome(rs.getString("nome"));

        String tipoDb = rs.getString("tipo"); // SUPLEMENTO/ORAL/INJETAVEL/TOPICO
        p.setTipo(TipoProduto.fromDatabase(tipoDb));

        p.setUnidade(rs.getString("unidade"));
        p.setEstoqueAtual(rs.getDouble("estoque_atual"));
        p.setEstoqueMinimo(rs.getDouble("estoque_minimo"));
        p.setLote(rs.getString("lote"));

        String validadeStr = rs.getString("validade");
        if (validadeStr != null) {
            p.setValidade(LocalDate.parse(validadeStr)); // yyyy-MM-dd
        }

        double custo = rs.getDouble("preco_custo");
        if (!rs.wasNull()) {
            p.setPrecoCusto(custo);
        }

        double venda = rs.getDouble("preco_venda");
        if (!rs.wasNull()) {
            p.setPrecoVenda(venda);
        }

        p.setAtivo(rs.getInt("ativo") == 1);

        return p;
    }

    public void ativarDesativar(Produto p) {
        String sql = "UPDATE produto SET ativo = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, p.isAtivo() ? 1 : 0);
            stmt.setInt(2, p.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
