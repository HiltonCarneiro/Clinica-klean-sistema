package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.*;
import br.com.clinica.model.enums.TipoItemNota;
import br.com.clinica.model.enums.TipoMovimento;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotaDAO {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final MovimentoCaixaDAO movimentoCaixaDAO = new MovimentoCaixaDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private static final DateTimeFormatter DH_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

            nota.recalcularTotais();

            long idNota;

            // 1) inserir cabeçalho
            try (PreparedStatement stmtNota = conn.prepareStatement(sqlNota, Statement.RETURN_GENERATED_KEYS)) {
                stmtNota.setString(1, nota.getDataHora().toString());
                stmtNota.setLong(2, nota.getPaciente().getId());
                stmtNota.setInt(3, nota.getProfissional().getId());
                stmtNota.setString(4, nota.getFormaPagamento());
                stmtNota.setDouble(5, nota.getTotalBruto());
                stmtNota.setDouble(6, nota.getDesconto());
                stmtNota.setDouble(7, nota.getTotalLiquido());
                stmtNota.setString(8, nota.getObservacao());

                stmtNota.executeUpdate();

                try (ResultSet rs = stmtNota.getGeneratedKeys()) {
                    if (!rs.next()) {
                        conn.rollback();
                        throw new RuntimeException("Não foi possível obter o ID da nota.");
                    }
                    idNota = rs.getLong(1);
                }
            }

            // 2) inserir itens + baixar estoque
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

                    if (item.getTipoItem() == TipoItemNota.PRODUTO
                            && item.getProduto() != null
                            && item.getProduto().getId() != null) {
                        produtoDAO.baixarEstoque(conn, item.getProduto().getId(), item.getQuantidade());
                    }
                }
                stmtItem.executeBatch();
            }

            // 3) registrar movimento caixa (entrada)
            MovimentoCaixa mov = new MovimentoCaixa();
            LocalDate dataMov = (nota.getDataHora() != null)
                    ? nota.getDataHora().toLocalDate()
                    : LocalDate.now();

            mov.setData(dataMov);
            mov.setDescricao("Recebimento - Nota " + idNota);
            mov.setTipo(TipoMovimento.ENTRADA);
            mov.setValor(nota.getTotalLiquido());
            mov.setFormaPagamento(nota.getFormaPagamento());
            mov.setPacienteNome(nota.getPaciente().getNome());
            mov.setObservacao(nota.getObservacao());

            movimentoCaixaDAO.registrar(conn, mov);

            // 4) audit log (quem criou a nota)
            String detalhes = "paciente=" + nota.getPaciente().getNome()
                    + ", total=" + nota.getTotalLiquido()
                    + ", forma=" + nota.getFormaPagamento();

            auditLogDAO.registrar(conn,
                    nota.getProfissional().getId(),
                    "CRIAR",
                    "NOTA",
                    String.valueOf(idNota),
                    detalhes
            );

            conn.commit();

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }

            String msg = (e.getMessage() != null) ? e.getMessage() : "Erro desconhecido.";
            if (msg.toLowerCase().contains("estoque insuficiente")) {
                throw new RuntimeException("Não foi possível finalizar: estoque insuficiente para um ou mais produtos.", e);
            }

            throw new RuntimeException("Erro ao salvar nota: " + msg, e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    // CONSULTA / REIMPRESSÃO

    public static class NotaResumo {
        private final long id;
        private final String dataHoraFmt;
        private final String pacienteNome;
        private final String profissionalNome;
        private final String formaPagamento;
        private final double totalLiquido;

        public NotaResumo(long id, String dataHoraFmt, String pacienteNome, String profissionalNome, String formaPagamento, double totalLiquido) {
            this.id = id;
            this.dataHoraFmt = dataHoraFmt;
            this.pacienteNome = pacienteNome;
            this.profissionalNome = profissionalNome;
            this.formaPagamento = formaPagamento;
            this.totalLiquido = totalLiquido;
        }

        public long getId() { return id; }
        public String getDataHoraFmt() { return dataHoraFmt; }
        public String getPacienteNome() { return pacienteNome; }
        public String getProfissionalNome() { return profissionalNome; }
        public String getFormaPagamento() { return formaPagamento; }
        public double getTotalLiquido() { return totalLiquido; }
    }

    public List<NotaResumo> listarNotasResumo(LocalDate inicio, LocalDate fim,
                                              String pacienteLike,
                                              Integer profissionalId,
                                              String formaPagamento) {

        StringBuilder sql = new StringBuilder("""
            SELECT n.id,
                   n.data_hora,
                   p.nome AS paciente_nome,
                   u.nome AS profissional_cargo,
                   u.pessoa_nome AS profissional_pessoa,
                   n.forma_pagamento,
                   n.total_liquido
              FROM nota n
              JOIN paciente p ON p.id = n.id_paciente
              JOIN usuario  u ON u.id = n.id_profissional
             WHERE substr(n.data_hora, 1, 10) BETWEEN ? AND ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(inicio.toString());
        params.add(fim.toString());

        if (pacienteLike != null && !pacienteLike.isBlank()) {
            sql.append(" AND LOWER(p.nome) LIKE ? ");
            params.add("%" + pacienteLike.trim().toLowerCase() + "%");
        }

        if (profissionalId != null) {
            sql.append(" AND n.id_profissional = ? ");
            params.add(profissionalId);
        }

        if (formaPagamento != null && !formaPagamento.isBlank()) {
            sql.append(" AND n.forma_pagamento = ? ");
            params.add(formaPagamento.trim());
        }

        sql.append(" ORDER BY n.data_hora DESC, n.id DESC ");

        List<NotaResumo> out = new ArrayList<>();

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    LocalDateTime dh = LocalDateTime.parse(rs.getString("data_hora"));

                    String paciente = rs.getString("paciente_nome");
                    String cargo = rs.getString("profissional_cargo");
                    String pessoa = rs.getString("profissional_pessoa");
                    String profissional = (pessoa != null && !pessoa.isBlank()) ? pessoa : cargo;

                    String forma = rs.getString("forma_pagamento");
                    double total = rs.getDouble("total_liquido");

                    out.add(new NotaResumo(
                            id,
                            dh.format(DH_FMT),
                            paciente,
                            profissional,
                            forma,
                            total
                    ));
                }
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar notas", e);
        }
    }

    public Nota buscarNotaCompleta(long idNota) {
        String sqlNota = """
            SELECT n.id, n.data_hora, n.forma_pagamento, n.total_bruto, n.desconto, n.total_liquido, n.observacao,
                   p.id AS p_id, p.nome AS p_nome,
                   u.id AS u_id, u.nome AS u_cargo, u.pessoa_nome AS u_pessoa
              FROM nota n
              JOIN paciente p ON p.id = n.id_paciente
              JOIN usuario  u ON u.id = n.id_profissional
             WHERE n.id = ?
        """;

        String sqlItens = """
            SELECT i.id,
                   i.tipo_item,
                   i.id_produto,
                   pr.nome AS produto_nome,
                   i.descricao,
                   i.quantidade,
                   i.valor_unitario,
                   i.valor_total
              FROM nota_item i
              LEFT JOIN produto pr ON pr.id = i.id_produto
             WHERE i.id_nota = ?
             ORDER BY i.id
        """;

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement psNota = c.prepareStatement(sqlNota)) {

            psNota.setLong(1, idNota);

            try (ResultSet rs = psNota.executeQuery()) {
                if (!rs.next()) return null;

                Nota nota = new Nota();
                nota.setId(rs.getLong("id"));
                nota.setDataHora(LocalDateTime.parse(rs.getString("data_hora")));
                nota.setFormaPagamento(rs.getString("forma_pagamento"));
                nota.setTotalBruto(rs.getDouble("total_bruto"));
                nota.setDesconto(rs.getDouble("desconto"));
                nota.setTotalLiquido(rs.getDouble("total_liquido"));
                nota.setObservacao(rs.getString("observacao"));

                Paciente p = new Paciente();
                p.setId(rs.getLong("p_id"));
                p.setNome(rs.getString("p_nome"));
                nota.setPaciente(p);

                Usuario u = new Usuario();
                u.setId(rs.getInt("u_id"));
                u.setNome(rs.getString("u_cargo"));
                u.setPessoaNome(rs.getString("u_pessoa"));
                nota.setProfissional(u);

                try (PreparedStatement psItens = c.prepareStatement(sqlItens)) {
                    psItens.setLong(1, idNota);

                    List<NotaItem> itens = new ArrayList<>();
                    try (ResultSet ri = psItens.executeQuery()) {
                        while (ri.next()) {
                            NotaItem item = new NotaItem();
                            item.setId(ri.getLong("id"));
                            item.setTipoItem(TipoItemNota.valueOf(ri.getString("tipo_item")));

                            long idProd = ri.getLong("id_produto");
                            if (!ri.wasNull()) {
                                Produto pr = new Produto();
                                pr.setId(idProd);
                                pr.setNome(ri.getString("produto_nome"));
                                item.setProduto(pr);
                            }

                            item.setDescricao(ri.getString("descricao"));
                            item.setQuantidade(ri.getDouble("quantidade"));
                            item.setValorUnitario(ri.getDouble("valor_unitario"));
                            item.setValorTotal(ri.getDouble("valor_total"));

                            itens.add(item);
                        }
                    }
                    nota.setItens(itens);
                }

                return nota;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar nota", e);
        }
    }
}