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
                "nome, cpf, rg, data_nascimento, telefone, endereco, responsavel_legal, ativo, " +
                "rua, numero, complemento, bairro, cidade, cep, uf" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String cpfNorm = normalizarDocumento(p.getCpf());
            String rgNorm = normalizarDocumento(p.getRg());

            if (cpfNorm != null && cpfJaExisteParaOutro(conn, null, cpfNorm)) {
                throw new RuntimeException("CPF já cadastrado para outro paciente.");
            }
            if (rgNorm != null && rgJaExisteParaOutro(conn, null, rgNorm)) {
                throw new RuntimeException("RG já cadastrado para outro paciente.");
            }

            p.setCpf(cpfNorm);
            p.setRg(rgNorm);

            preencherStatementBasico(stmt, p);

            stmt.setString(9, nvlTrim(p.getRua()));
            stmt.setString(10, nvlTrim(p.getNumero()));
            stmt.setString(11, nvlTrim(p.getComplemento()));
            stmt.setString(12, nvlTrim(p.getBairro()));
            stmt.setString(13, nvlTrim(p.getCidade()));
            stmt.setString(14, normalizarDocumento(p.getCep()));
            stmt.setString(15, nvlTrim(p.getUf()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getLong(1));
            }

            audit.registrarAuto("CRIAR", "PACIENTE",
                    String.valueOf(p.getId()),
                    "nome=" + p.getNome()
                            + ", cpf=" + (p.getCpf() == null ? "" : p.getCpf())
                            + ", rg=" + (p.getRg() == null ? "" : p.getRg()));

        } catch (SQLException e) {
            tratarErroSql(e);
        }
    }

    public void atualizar(Paciente p) {
        if (p.getId() == null) throw new IllegalArgumentException("Paciente sem ID para atualizar");

        String sql = "UPDATE paciente SET " +
                "nome = ?, cpf = ?, rg = ?, data_nascimento = ?, telefone = ?, endereco = ?, " +
                "responsavel_legal = ?, ativo = ?, " +
                "rua = ?, numero = ?, complemento = ?, bairro = ?, cidade = ?, cep = ?, uf = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String cpfNorm = normalizarDocumento(p.getCpf());
            String rgNorm = normalizarDocumento(p.getRg());

            if (cpfNorm != null && cpfJaExisteParaOutro(conn, p.getId(), cpfNorm)) {
                throw new RuntimeException("CPF já cadastrado para outro paciente.");
            }
            if (rgNorm != null && rgJaExisteParaOutro(conn, p.getId(), rgNorm)) {
                throw new RuntimeException("RG já cadastrado para outro paciente.");
            }

            p.setCpf(cpfNorm);
            p.setRg(rgNorm);

            preencherStatementBasico(stmt, p);

            stmt.setString(9, nvlTrim(p.getRua()));
            stmt.setString(10, nvlTrim(p.getNumero()));
            stmt.setString(11, nvlTrim(p.getComplemento()));
            stmt.setString(12, nvlTrim(p.getBairro()));
            stmt.setString(13, nvlTrim(p.getCidade()));
            stmt.setString(14, normalizarDocumento(p.getCep()));
            stmt.setString(15, nvlTrim(p.getUf()));
            stmt.setLong(16, p.getId());

            stmt.executeUpdate();

            audit.registrarAuto("EDITAR", "PACIENTE",
                    String.valueOf(p.getId()),
                    "nome=" + p.getNome()
                            + ", cpf=" + (p.getCpf() == null ? "" : p.getCpf())
                            + ", rg=" + (p.getRg() == null ? "" : p.getRg()));

        } catch (SQLException e) {
            tratarErroSql(e);
        }
    }

    private void preencherStatementBasico(PreparedStatement stmt, Paciente p) throws SQLException {
        stmt.setString(1, nvlTrim(p.getNome()));

        String cpf = normalizarDocumento(p.getCpf());
        if (cpf == null) stmt.setNull(2, Types.VARCHAR);
        else stmt.setString(2, cpf);

        String rg = normalizarDocumento(p.getRg());
        if (rg == null) stmt.setNull(3, Types.VARCHAR);
        else stmt.setString(3, rg);

        if (p.getDataNascimento() != null) {
            stmt.setString(4, p.getDataNascimento().format(DATA_FORMATTER));
        } else {
            stmt.setNull(4, Types.VARCHAR);
        }

        String tel = normalizarDocumento(p.getTelefone());
        if (tel == null) stmt.setNull(5, Types.VARCHAR);
        else stmt.setString(5, tel);

        stmt.setString(6, nvlTrim(p.getEndereco()));

        String resp = nvlTrim(p.getResponsavelLegal());
        if (resp == null) stmt.setNull(7, Types.VARCHAR);
        else stmt.setString(7, resp);

        stmt.setInt(8, p.isAtivo() ? 1 : 0);
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
        p.setRg(rs.getString("rg"));

        String dataStr = rs.getString("data_nascimento");
        if (dataStr != null && !dataStr.isBlank()) {
            p.setDataNascimento(LocalDate.parse(dataStr, DATA_FORMATTER));
        }

        p.setTelefone(rs.getString("telefone"));
        p.setEndereco(rs.getString("endereco"));
        p.setResponsavelLegal(rs.getString("responsavel_legal"));
        p.setAtivo(rs.getInt("ativo") == 1);

        try {
            p.setRua(rs.getString("rua"));
            p.setNumero(rs.getString("numero"));
            p.setComplemento(rs.getString("complemento"));
            p.setBairro(rs.getString("bairro"));
            p.setCidade(rs.getString("cidade"));
            p.setCep(rs.getString("cep"));
            p.setUf(rs.getString("uf"));
        } catch (SQLException ignored) {}

        return p;
    }

    public void inativar(Long id) {
        alterarStatus(id, false);
    }

    public void ativar(Long id) {
        alterarStatus(id, true);
    }

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

    private boolean rgJaExisteParaOutro(Connection conn, Long idAtual, String rgNorm) throws SQLException {
        if (rgNorm == null || rgNorm.isBlank()) return false;

        String sql = "SELECT 1 FROM paciente WHERE rg = ? AND id <> ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rgNorm);
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
        String m = msg.toLowerCase();
        return m.contains("unique") && (m.contains("uk_paciente_cpf") || m.contains("paciente.cpf"));
    }

    private boolean isUniqueRgError(SQLException e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        String m = msg.toLowerCase();
        return m.contains("unique") && (m.contains("uk_paciente_rg") || m.contains("paciente.rg"));
    }

    private boolean isCheckCpfOuRgError(SQLException e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        return msg.toLowerCase().contains("ck_paciente_cpf_ou_rg");
    }

    private void tratarErroSql(SQLException e) {
        if (isUniqueCpfError(e)) {
            throw new RuntimeException("CPF já cadastrado para outro paciente.");
        }
        if (isUniqueRgError(e)) {
            throw new RuntimeException("RG já cadastrado para outro paciente.");
        }
        if (isCheckCpfOuRgError(e)) {
            throw new RuntimeException("Informe CPF ou RG do paciente.");
        }
        throw new RuntimeException("Erro ao salvar paciente", e);
    }
}