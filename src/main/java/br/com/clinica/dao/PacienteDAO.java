package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.Paciente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    // mesmo formato que você está gravando no banco (TEXT)
    private static final DateTimeFormatter DATA_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ======================================================================
    // SALVAR (insere ou atualiza)
    // ======================================================================
    public void salvar(Paciente paciente) {
        if (paciente.getId() == null) {
            inserir(paciente);
        } else {
            atualizar(paciente);
        }
    }

    // ======================================================================
    // INSERIR
    // ======================================================================
    private void inserir(Paciente p) {
        String sql = "INSERT INTO paciente (" +
                "nome, cpf, data_nascimento, telefone, endereco, responsavel_legal, ativo, " +
                "rua, numero, bairro, cidade, cep, uf" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preencherStatementBasico(stmt, p);

            // campos de endereço detalhado (podem ser nulos)
            stmt.setString(8, p.getRua());
            stmt.setString(9, p.getNumero());
            stmt.setString(10, p.getBairro());
            stmt.setString(11, p.getCidade());
            stmt.setString(12, p.getCep());
            stmt.setString(13, p.getUf());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir paciente", e);
        }
    }

    // ======================================================================
    // ATUALIZAR
    // ======================================================================
    public void atualizar(Paciente p) {
        if (p.getId() == null) {
            throw new IllegalArgumentException("Paciente sem ID para atualizar");
        }

        String sql = "UPDATE paciente SET " +
                "nome = ?, cpf = ?, data_nascimento = ?, telefone = ?, endereco = ?, " +
                "responsavel_legal = ?, ativo = ?, " +
                "rua = ?, numero = ?, bairro = ?, cidade = ?, cep = ?, uf = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preencherStatementBasico(stmt, p);

            stmt.setString(8, p.getRua());
            stmt.setString(9, p.getNumero());
            stmt.setString(10, p.getBairro());
            stmt.setString(11, p.getCidade());
            stmt.setString(12, p.getCep());
            stmt.setString(13, p.getUf());

            // ID agora é Long → setLong
            stmt.setLong(14, p.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar paciente", e);
        }
    }

    /**
     * Preenche as 7 primeiras posições do PreparedStatement
     * (nome, cpf, data_nascimento, telefone, endereco, responsavel_legal, ativo)
     */
    private void preencherStatementBasico(PreparedStatement stmt, Paciente p) throws SQLException {
        stmt.setString(1, p.getNome());
        stmt.setString(2, p.getCpf());

        if (p.getDataNascimento() != null) {
            stmt.setString(3, p.getDataNascimento().format(DATA_FORMATTER));
        } else {
            stmt.setNull(3, Types.VARCHAR);
        }

        stmt.setString(4, p.getTelefone());
        stmt.setString(5, p.getEndereco());
        stmt.setString(6, p.getResponsavelLegal());
        stmt.setInt(7, p.isAtivo() ? 1 : 0);
    }

    // ======================================================================
    // LISTAGENS
    // ======================================================================

    // lista todo mundo (ativos + inativos)
    public List<Paciente> listarTodos() {
        String sql = "SELECT * FROM paciente ORDER BY nome";
        return executarConsultaLista(sql, false);
    }

    // overload para o caso em que você usa o checkbox "Mostrar inativos"
    public List<Paciente> listarTodos(boolean incluirInativos) {
        if (incluirInativos) {
            return listarTodos();
        }
        return listarAtivos();
    }

    // só ativos
    public List<Paciente> listarAtivos() {
        String sql = "SELECT * FROM paciente WHERE ativo = 1 ORDER BY nome";
        return executarConsultaLista(sql, false);
    }

    public Paciente buscarPorId(Long id) {
        String sql = "SELECT * FROM paciente WHERE id = ?";
        List<Paciente> lista = executarConsultaLista(sql, true, id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    // método utilitário para SELECT que devolve lista
    private List<Paciente> executarConsultaLista(String sql,
                                                 boolean temParametro,
                                                 Long... idParametro) {
        List<Paciente> pacientes = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (temParametro && idParametro != null
                    && idParametro.length > 0 && idParametro[0] != null) {
                stmt.setLong(1, idParametro[0]);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pacientes.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pacientes", e);
        }

        return pacientes;
    }

    // converte uma linha do ResultSet em Paciente
    private Paciente mapRow(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();

        // ID agora é Long
        p.setId(rs.getLong("id"));
        p.setNome(rs.getString("nome"));
        p.setCpf(rs.getString("cpf"));

        String dataStr = rs.getString("data_nascimento");
        if (dataStr != null && !dataStr.isBlank()) {
            p.setDataNascimento(LocalDate.parse(dataStr, DATA_FORMATTER));
        }

        p.setTelefone(rs.getString("telefone"));
        p.setEndereco(rs.getString("endereco"));
        p.setResponsavelLegal(rs.getString("responsavel_legal"));
        p.setAtivo(rs.getInt("ativo") == 1);

        // campos de endereço detalhado (se existirem na tabela)
        try {
            p.setRua(rs.getString("rua"));
            p.setNumero(rs.getString("numero"));
            p.setBairro(rs.getString("bairro"));
            p.setCidade(rs.getString("cidade"));
            p.setCep(rs.getString("cep"));
            p.setUf(rs.getString("uf"));
        } catch (SQLException ignored) {
            // se for banco mais antigo sem essas colunas, só ignora
        }

        return p;
    }

    // ======================================================================
    // ATIVAR / INATIVAR
    // ======================================================================

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
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar status do paciente", e);
        }
    }
}
