package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.Produto;
import br.com.clinica.model.TipoProduto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de acesso à tabela 'produto'.
 */
public class ProdutoDAO {

    /**
     * Lista produtos com filtros opcionais.
     *
     * @param incluirInativos   se false, retorna apenas produtos com ativo = 1
     * @param apenasBaixoEstoque se true, retorna apenas produtos com estoqueAtual <= estoqueMinimo
     * @param apenasVencendo    se true, retorna apenas produtos com validade próxima (30 dias) ou já vencidos
     */
    public List<Produto> listar(boolean incluirInativos,
                                boolean apenasBaixoEstoque,
                                boolean apenasVencendo) {

        String sql = "SELECT * FROM produto";
        if (!incluirInativos) {
            sql += " WHERE ativo = 1";
        }

        List<Produto> produtos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produto p = mapRow(rs);

                if (apenasBaixoEstoque && p.getEstoqueAtual() > p.getEstoqueMinimo()) {
                    continue;
                }

                if (apenasVencendo && !isVencendo(p)) {
                    continue;
                }

                produtos.add(p);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos", e);
        }

        return produtos;
    }

    /**
     * Insere ou atualiza um produto.
     */
    public void salvar(Produto p) {
        if (p.getId() == null) {
            inserir(p);
        } else {
            atualizar(p);
        }
    }

    private void inserir(Produto p) {
        String sql = "INSERT INTO produto (" +
                "nome, tipo, unidade, estoque_atual, estoque_minimo, lote, validade, preco_custo, preco_venda, ativo" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preencherCampos(stmt, p);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir produto", e);
        }
    }

    private void atualizar(Produto p) {
        String sql = "UPDATE produto SET " +
                "nome = ?, tipo = ?, unidade = ?, estoque_atual = ?, estoque_minimo = ?, " +
                "lote = ?, validade = ?, preco_custo = ?, preco_venda = ?, ativo = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preencherCampos(stmt, p);
            stmt.setLong(11, p.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar produto", e);
        }
    }

    /**
     * Ativa ou inativa (toggle) um produto.
     */
    public void ativarDesativar(Produto p) {
        if (p.getId() == null) {
            return;
        }
        boolean novoStatus = !p.isAtivo();

        String sql = "UPDATE produto SET ativo = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, novoStatus ? 1 : 0);
            stmt.setLong(2, p.getId());
            stmt.executeUpdate();

            p.setAtivo(novoStatus);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar status do produto", e);
        }
    }

    private void preencherCampos(PreparedStatement stmt, Produto p) throws SQLException {
        stmt.setString(1, p.getNome());
        stmt.setString(2, p.getTipo() != null ? p.getTipo().toDatabase() : null);
        stmt.setString(3, p.getUnidade());
        stmt.setDouble(4, p.getEstoqueAtual());
        stmt.setDouble(5, p.getEstoqueMinimo());
        stmt.setString(6, p.getLote());

        if (p.getValidade() != null) {
            stmt.setString(7, p.getValidade().toString()); // ISO yyyy-MM-dd
        } else {
            stmt.setString(7, null);
        }

        if (p.getPrecoCusto() != null) {
            stmt.setDouble(8, p.getPrecoCusto());
        } else {
            stmt.setNull(8, java.sql.Types.REAL);
        }

        if (p.getPrecoVenda() != null) {
            stmt.setDouble(9, p.getPrecoVenda());
        } else {
            stmt.setNull(9, java.sql.Types.REAL);
        }

        stmt.setInt(10, p.isAtivo() ? 1 : 0);
    }

    private Produto mapRow(ResultSet rs) throws SQLException {
        Produto p = new Produto();

        p.setId(rs.getLong("id"));
        p.setNome(rs.getString("nome"));

        String tipoDb = rs.getString("tipo");
        p.setTipo(tipoDb != null ? TipoProduto.fromDatabase(tipoDb) : null);

        p.setUnidade(rs.getString("unidade"));
        p.setEstoqueAtual(rs.getDouble("estoque_atual"));
        p.setEstoqueMinimo(rs.getDouble("estoque_minimo"));
        p.setLote(rs.getString("lote"));

        String validadeStr = rs.getString("validade");
        if (validadeStr != null && !validadeStr.isBlank()) {
            p.setValidade(LocalDate.parse(validadeStr));
        }

        Object precoCustoObj = rs.getObject("preco_custo");
        if (precoCustoObj != null) {
            p.setPrecoCusto(rs.getDouble("preco_custo"));
        }

        Object precoVendaObj = rs.getObject("preco_venda");
        if (precoVendaObj != null) {
            p.setPrecoVenda(rs.getDouble("preco_venda"));
        }

        p.setAtivo(rs.getInt("ativo") == 1);

        return p;
    }

    private boolean isVencendo(Produto p) {
        LocalDate validade = p.getValidade();
        if (validade == null) {
            return false;
        }
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(30);
        return !validade.isAfter(limite);
    }

    /**
     * Baixa a quantidade informada do estoque do produto.
     * Usa a mesma Connection da transação da Nota.
     */
    public void baixarEstoque(Connection conn, Long idProduto, double quantidade) throws SQLException {
        String sql = "UPDATE produto SET estoque_atual = estoque_atual - ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, quantidade);
            stmt.setLong(2, idProduto);
            stmt.executeUpdate();
        }
    }
}
