package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.Paciente;

import java.sql.*;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    private final AuditLogDAO audit = new AuditLogDAO();

    private static final DateTimeFormatter DATA_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void salvar(Paciente paciente) {
        if (paciente.getId() == null) inserir(paciente);
        else atualizar(paciente);
    }

    private void inserir(Paciente p) {
        String sql = "INSERT INTO paciente (" +
                "nome, cpf, data_nascimento, telefone, endereco, responsavel_legal, ativo, " +
                "rua, numero, bairro, cidade, cep, uf" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // valida duplicidade (quando CPF informado)
            String cpfNorm = normalizarDocumento(p.getCpf());
            if (cpfNorm != null && cpfJaExisteParaOutro(conn, null, cpfNorm)) {
                throw new RuntimeException("CPF já cadastrado para outro paciente.");
            }
            p.setCpf(cpfNorm); // garante armazenamento consistente (digits-only)

            preencherStatementBasico(stmt, p);

            stmt.setString(8, nvlTrim(p.getRua()));
            stmt.setString(9, nvlTrim(p.getNumero()));
            stmt.setString(10, nvlTrim(p.getBairro()));
            stmt.setString(11, nvlTrim(p.getCidade()));
            stmt.setString(12, normalizarDocumento(p.getCep()));
            stmt.setString(13, nvlTrim(p.getUf()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getLong(1));
            }

            audit.registrarAuto("CRIAR", "PACIENTE",
                    String.valueOf(p.getId()),
                    "nome=" + p.getNome() + ", cpf=" + (p.getCpf() == null ? "" : p.getCpf()));

        } catch (SQLException e) {
            // erro mais amigável quando a constraint estoura (caso o banco ainda tenha formato antigo)
            if (isUniqueCpfError(e)) {
                throw new RuntimeException("CPF já cadastrado para outro paciente.");
            }
            throw new RuntimeException("Erro ao inserir paciente", e);
        }
    }

    public void atualizar(Paciente p) {
        if (p.getId() == null) throw new IllegalArgumentException("Paciente sem ID para atualizar");

        String sql = "UPDATE paciente SET " +
                "nome = ?, cpf = ?, data_nascimento = ?, telefone = ?, endereco = ?, " +
                "responsavel_legal = ?, ativo = ?, " +
                "rua = ?, numero = ?, bairro = ?, cidade = ?, cep = ?, uf = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // valida duplicidade (quando CPF informado)
            String cpfNorm = normalizarDocumento(p.getCpf());
            if (cpfNorm != null && cpfJaExisteParaOutro(conn, p.getId(), cpfNorm)) {
                throw new RuntimeException("CPF já cadastrado para outro paciente.");
            }
            p.setCpf(cpfNorm);

            preencherStatementBasico(stmt, p);

            stmt.setString(8, nvlTrim(p.getRua()));
            stmt.setString(9, nvlTrim(p.getNumero()));
            stmt.setString(10, nvlTrim(p.getBairro()));
            stmt.setString(11, nvlTrim(p.getCidade()));
            stmt.setString(12, normalizarDocumento(p.getCep()));
            stmt.setString(13, nvlTrim(p.getUf()));
            stmt.setLong(14, p.getId());

            stmt.executeUpdate();

            audit.registrarAuto("EDITAR", "PACIENTE",
                    String.valueOf(p.getId()),
                    "nome=" + p.getNome() + ", cpf=" + (p.getCpf() == null ? "" : p.getCpf()));

        } catch (SQLException e) {
            if (isUniqueCpfError(e)) {
                throw new RuntimeException("CPF já cadastrado para outro paciente.");
            }
            throw new RuntimeException("Erro ao atualizar paciente", e);
        }
    }

    private void preencherStatementBasico(PreparedStatement stmt, Paciente p) throws SQLException {
        stmt.setString(1, nvlTrim(p.getNome()));

        // CPF: se vazio, salva NULL (não quebra UNIQUE)
        String cpf = normalizarDocumento(p.getCpf());
        if (cpf == null) {
            stmt.setNull(2, Types.VARCHAR);
        } else {
            stmt.setString(2, cpf);
        }

        if (p.getDataNascimento() != null) {
            stmt.setString(3, p.getDataNascimento().format(DATA_FORMATTER));
        } else {
            stmt.setNull(3, Types.VARCHAR);
        }

        // Telefone: se vazio, salva NULL
        String tel = normalizarDocumento(p.getTelefone());
        if (tel == null) stmt.setNull(4, Types.VARCHAR);
        else stmt.setString(4, tel);

        stmt.setString(5, nvlTrim(p.getEndereco()));

        String resp = nvlTrim(p.getResponsavelLegal());
        if (resp == null) stmt.setNull(6, Types.VARCHAR);
        else stmt.setString(6, resp);

        stmt.setInt(7, p.isAtivo() ? 1 : 0);
    }

    public List<Paciente> listarTodos() {
        String sql = "SELECT * FROM paciente ORDER BY nome";
        return executarConsultaLista(sql, false);
    }

    public List<Paciente> listarTodos(boolean incluirInativos) {
        if (incluirInativos) return listarTodos();
        return listarAtivos();
    }

    public List<Paciente> listarAtivos() {
        String sql = "SELECT * FROM paciente WHERE ativo = 1 ORDER BY nome";
        return executarConsultaLista(sql, false);
    }

    public Paciente buscarPorId(Long id) {
        String sql = "SELECT * FROM paciente WHERE id = ?";
        List<Paciente> lista = executarConsultaLista(sql, true, id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    private List<Paciente> executarConsultaLista(String sql, boolean temParametro, Long... idParametro) {
        List<Paciente> pacientes = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (temParametro && idParametro != null && idParametro.length > 0 && idParametro[0] != null) {
                stmt.setLong(1, idParametro[0]);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) pacientes.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pacientes", e);
        }

        return pacientes;
    }

    private Paciente mapRow(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();
        p.setId(rs.getLong("id"));
        p.setNome(rs.getString("nome"));
        p.setCpf(rs.getString("cpf"));

        String dataStr = rs.getString("data_nascimento");
        if (dataStr != null && !dataStr.isBlank()) p.setDataNascimento(LocalDate.parse(dataStr, DATA_FORMATTER));

        p.setTelefone(rs.getString("telefone"));
        p.setEndereco(rs.getString("endereco"));
        p.setResponsavelLegal(rs.getString("responsavel_legal"));
        p.setAtivo(rs.getInt("ativo") == 1);

        try {
            p.setRua(rs.getString("rua"));
            p.setNumero(rs.getString("numero"));
            p.setBairro(rs.getString("bairro"));
            p.setCidade(rs.getString("cidade"));
            p.setCep(rs.getString("cep"));
            p.setUf(rs.getString("uf"));
        } catch (SQLException ignored) {}

        return p;
    }

    public void inativar(Long id) { alterarStatus(id, false); }
    public void ativar(Long id) { alterarStatus(id, true); }

    private void alterarStatus(Long id, boolean ativo) {
        String sql = "UPDATE paciente SET ativo = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ativo ? 1 : 0);
            stmt.setLong(2, id);
            stmt.executeUpdate();

            audit.registrarAuto(ativo ? "ATIVAR" : "INATIVAR",
                    "PACIENTE",
                    String.valueOf(id),
                    "status=" + (ativo ? "ativo" : "inativo"));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar status do paciente", e);
        }
    }

    // =========================
    // Helpers
    // =========================

    private boolean cpfJaExisteParaOutro(Connection conn, Long idAtual, String cpfNorm) throws SQLException {
        if (cpfNorm == null || cpfNorm.isBlank()) return false;

        String sql = "SELECT 1 FROM paciente WHERE cpf = ? AND id <> ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpfNorm);
            ps.setLong(2, idAtual != null ? idAtual : -1);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String normalizarDocumento(String s) {
        if (s == null) return null;
        String digits = s.replaceAll("\\D+", "");
        return digits.isBlank() ? null : digits;
    }

    private String nvlTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    private boolean isUniqueCpfError(SQLException e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        return msg.toLowerCase().contains("unique") && msg.toLowerCase().contains("paciente.cpf");
    }
}
