package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.Produto;
import br.com.clinica.model.TipoProduto;
import br.com.clinica.session.Session;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    private final AuditLogDAO audit = new AuditLogDAO();

    public List<Produto> listar(boolean incluirInativos,
                                boolean apenasBaixoEstoque,
                                boolean apenasVencendo) {

        String sql = "SELECT * FROM produto";
        if (!incluirInativos) sql += " WHERE ativo = 1";

        List<Produto> produtos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produto p = mapRow(rs);

                if (apenasBaixoEstoque && p.getEstoqueAtual() > p.getEstoqueMinimo()) continue;
                if (apenasVencendo && !isVencendo(p)) continue;

                produtos.add(p);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos", e);
        }

        return produtos;
    }

    public void salvar(Produto p) {
        if (p.getId() == null) inserir(p);
        else atualizar(p);
    }

    private void inserir(Produto p) {
        String sql = "INSERT INTO produto (" +
                "nome, tipo, estoque_atual, estoque_minimo, lote, validade, preco_custo, preco_venda, ativo" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherCamposSemId(stmt, p);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getLong(1));
            }

            audit.registrarAuto("CRIAR", "PRODUTO",
                    String.valueOf(p.getId()),
                    "nome=" + p.getNome() + ", tipo=" + (p.getTipo() != null ? p.getTipo().name() : "null"));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir produto", e);
        }
    }

    private void atualizar(Produto p) {
        String sql = "UPDATE produto SET " +
                "nome = ?, tipo = ?, estoque_atual = ?, estoque_minimo = ?, " +
                "lote = ?, validade = ?, preco_custo = ?, preco_venda = ?, ativo = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preencherCamposSemId(stmt, p);
            stmt.setLong(10, p.getId());
            stmt.executeUpdate();

            audit.registrarAuto("EDITAR", "PRODUTO",
                    String.valueOf(p.getId()),
                    "nome=" + p.getNome() + ", estoque=" + p.getEstoqueAtual() + ", ativo=" + p.isAtivo());

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar produto", e);
        }
    }

    public void ativarDesativar(Produto p) {
        if (p.getId() == null) return;

        boolean novoStatus = !p.isAtivo();
        String sql = "UPDATE produto SET ativo = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, novoStatus ? 1 : 0);
            stmt.setLong(2, p.getId());
            stmt.executeUpdate();

            p.setAtivo(novoStatus);

            audit.registrarAuto(novoStatus ? "ATIVAR" : "INATIVAR",
                    "PRODUTO",
                    String.valueOf(p.getId()),
                    "nome=" + p.getNome());

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar status do produto", e);
        }
    }

    private void preencherCamposSemId(PreparedStatement stmt, Produto p) throws SQLException {
        stmt.setString(1, p.getNome());
        stmt.setString(2, p.getTipo() != null ? p.getTipo().toDatabase() : null);

        stmt.setDouble(3, p.getEstoqueAtual());
        stmt.setDouble(4, p.getEstoqueMinimo());
        stmt.setString(5, p.getLote());

        if (p.getValidade() != null) stmt.setString(6, p.getValidade().toString());
        else stmt.setString(6, null);

        if (p.getPrecoCusto() != null) stmt.setDouble(7, p.getPrecoCusto());
        else stmt.setNull(7, Types.REAL);

        if (p.getPrecoVenda() != null) stmt.setDouble(8, p.getPrecoVenda());
        else stmt.setNull(8, Types.REAL);

        stmt.setInt(9, p.isAtivo() ? 1 : 0);
    }

    private Produto mapRow(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getLong("id"));
        p.setNome(rs.getString("nome"));

        String tipoDb = rs.getString("tipo");
        p.setTipo(tipoDb != null ? TipoProduto.fromDatabase(tipoDb) : null);

        p.setEstoqueAtual(rs.getDouble("estoque_atual"));
        p.setEstoqueMinimo(rs.getDouble("estoque_minimo"));
        p.setLote(rs.getString("lote"));

        String validadeStr = rs.getString("validade");
        if (validadeStr != null && !validadeStr.isBlank()) p.setValidade(LocalDate.parse(validadeStr));

        Object precoCustoObj = rs.getObject("preco_custo");
        if (precoCustoObj != null) p.setPrecoCusto(rs.getDouble("preco_custo"));

        Object precoVendaObj = rs.getObject("preco_venda");
        if (precoVendaObj != null) p.setPrecoVenda(rs.getDouble("preco_venda"));

        p.setAtivo(rs.getInt("ativo") == 1);
        return p;
    }

    private boolean isVencendo(Produto p) {
        LocalDate validade = p.getValidade();
        if (validade == null) return false;
        return !validade.isAfter(LocalDate.now().plusDays(30));
    }

    // baixa segura (não deixa estoque negativo)
    public void baixarEstoque(Connection conn, Long idProduto, double quantidade) throws SQLException {
        if (idProduto == null) throw new SQLException("Produto inválido.");
        if (quantidade <= 0) throw new SQLException("Quantidade inválida para baixa de estoque.");

        String sql = "UPDATE produto " +
                "SET estoque_atual = estoque_atual - ? " +
                "WHERE id = ? AND estoque_atual >= ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, quantidade);
            stmt.setLong(2, idProduto);
            stmt.setDouble(3, quantidade);

            int updated = stmt.executeUpdate();
            if (updated == 0) throw new SQLException("Estoque insuficiente para o produto selecionado.");
        }
    }
}