package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AnexoPacienteDAO {

    private static final DateTimeFormatter DB_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AnexoPacienteDAO() {
        criarOuAjustarTabela();
    }

    // Item simples para TableView
    public static class AnexoPacienteItem {
        private final Long id;
        private final Long pacienteId;
        private final Integer anamnese;      // pode ser null
        private final String nomeArquivo;
        private final String caminhoArquivo;
        private final Long tamanhoBytes;     // pode ser null
        private final String descricao;      // pode ser null
        private final String dataHora;

        public AnexoPacienteItem(Long id, Long pacienteId, Integer anamnese, String nomeArquivo,
                                 String caminhoArquivo, Long tamanhoBytes, String descricao, String dataHora) {
            this.id = id;
            this.pacienteId = pacienteId;
            this.anamnese = anamnese;
            this.nomeArquivo = nomeArquivo;
            this.caminhoArquivo = caminhoArquivo;
            this.tamanhoBytes = tamanhoBytes;
            this.descricao = descricao;
            this.dataHora = dataHora;
        }

        public Long getId() { return id; }
        public Long getPacienteId() { return pacienteId; }
        public Integer getAnamnese() { return anamnese; }
        public String getNomeArquivo() { return nomeArquivo; }
        public String getCaminhoArquivo() { return caminhoArquivo; }
        public Long getTamanhoBytes() { return tamanhoBytes; }
        public String getDescricao() { return descricao; }
        public String getDataHora() { return dataHora; }

        public File getFile() {
            return (caminhoArquivo == null) ? null : new File(caminhoArquivo);
        }
    }

    private void criarOuAjustarTabela() {
        String create = """
                CREATE TABLE IF NOT EXISTS anexo_paciente (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    paciente_id INTEGER NOT NULL,
                    anamnese INTEGER NULL,
                    nome_arquivo TEXT NOT NULL,
                    caminho_arquivo TEXT NOT NULL,
                    tamanho_bytes INTEGER NULL,
                    descricao TEXT NULL,
                    data_hora TEXT NOT NULL
                );
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(create);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela anexo_paciente", e);
        }

        Set<String> cols = new HashSet<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(anexo_paciente)")) {
            while (rs.next()) cols.add(rs.getString("name"));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao ler schema de anexo_paciente", e);
        }

        addColumnIfMissing(cols, "anamnese", "INTEGER NULL");
        addColumnIfMissing(cols, "nome_arquivo", "TEXT NOT NULL DEFAULT ''");
        addColumnIfMissing(cols, "caminho_arquivo", "TEXT NOT NULL DEFAULT ''");
        addColumnIfMissing(cols, "tamanho_bytes", "INTEGER NULL");
        addColumnIfMissing(cols, "descricao", "TEXT NULL");
        addColumnIfMissing(cols, "data_hora", "TEXT NOT NULL DEFAULT ''");
    }

    private void addColumnIfMissing(Set<String> cols, String col, String ddl) {
        if (cols.contains(col)) return;
        String sql = "ALTER TABLE anexo_paciente ADD COLUMN " + col + " " + ddl + ";";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar coluna '" + col + "' em anexo_paciente", e);
        }
    }

    // =========================
    // PDF (já existia)
    // =========================

    public void anexarPdf(Long pacienteId, Integer anamneseId, File arquivoPdf, String descricao) {
        if (pacienteId == null) throw new IllegalArgumentException("pacienteId nulo.");
        if (arquivoPdf == null) throw new IllegalArgumentException("arquivoPdf nulo.");

        Path base = Paths.get(System.getProperty("user.dir"), "data", "anexos", "paciente_" + pacienteId);
        try {
            Files.createDirectories(base);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar pasta de anexos: " + base, e);
        }

        String safeName = arquivoPdf.getName().replaceAll("[^a-zA-Z0-9._\\- ]", "_");
        String unique = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + safeName;

        Path destino = base.resolve(unique);

        try {
            Files.copy(arquivoPdf.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao copiar PDF para: " + destino, e);
        }

        Long tamanho = null;
        try { tamanho = Files.size(destino); } catch (Exception ignored) {}

        String agora = LocalDateTime.now().format(DB_FMT);

        String sql = """
                INSERT INTO anexo_paciente
                (paciente_id, anamnese, nome_arquivo, caminho_arquivo, tamanho_bytes, descricao, data_hora)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, pacienteId);
            if (anamneseId == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, anamneseId);

            ps.setString(3, unique);
            ps.setString(4, destino.toAbsolutePath().toString());

            if (tamanho == null) ps.setNull(5, Types.BIGINT);
            else ps.setLong(5, tamanho);

            if (descricao == null || descricao.isBlank()) ps.setNull(6, Types.VARCHAR);
            else ps.setString(6, descricao);

            ps.setString(7, agora);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao anexar PDF", e);
        }
    }

    public List<AnexoPacienteItem> listarPorPaciente(Long pacienteId) {
        String sql = "SELECT * FROM anexo_paciente WHERE paciente_id = ? ORDER BY data_hora DESC";
        List<AnexoPacienteItem> list = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, pacienteId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AnexoPacienteItem(
                            rs.getLong("id"),
                            rs.getLong("paciente_id"),
                            (rs.getObject("anamnese") == null ? null : rs.getInt("anamnese")),
                            rs.getString("nome_arquivo"),
                            rs.getString("caminho_arquivo"),
                            (rs.getObject("tamanho_bytes") == null ? null : rs.getLong("tamanho_bytes")),
                            rs.getString("descricao"),
                            rs.getString("data_hora")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar anexos por paciente", e);
        }

        return list;
    }

    public void remover(Long id) {
        String path = null;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT caminho_arquivo FROM anexo_paciente WHERE id = ?")) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) path = rs.getString(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar anexo", e);
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM anexo_paciente WHERE id = ?")) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover anexo", e);
        }

        if (path != null) {
            try { Files.deleteIfExists(Paths.get(path)); } catch (Exception ignored) {}
        }
    }

    public void abrirNoSistema(File file) {
        if (file == null) throw new IllegalArgumentException("Arquivo nulo.");
        if (!file.exists()) throw new IllegalArgumentException("Arquivo não encontrado: " + file.getAbsolutePath());
        if (!Desktop.isDesktopSupported()) throw new IllegalStateException("Desktop não suportado.");

        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir arquivo no sistema", e);
        }
    }

    // ============================================================
    // NOVO: ARQUIVO POR EVOLUÇÃO (TXT editável + UPSERT em anexo_paciente)
    // ============================================================

    /**
     * Cria ou atualiza um arquivo TXT editável para uma evolução (anamnese tipo EVOLUCAO),
     * vinculado ao paciente e ao id da anamnese (coluna anamnese).
     *
     * - Um arquivo por evolução (id).
     * - Registro na tabela anexo_paciente com descricao = "EVOLUCAO".
     */
    public void criarOuAtualizarArquivoEvolucao(Long pacienteId, Integer anamneseId, String conteudo) {
        if (pacienteId == null) throw new IllegalArgumentException("pacienteId nulo.");
        if (anamneseId == null) throw new IllegalArgumentException("anamneseId nulo.");

        Path base = Paths.get(System.getProperty("user.dir"), "data", "anexos", "paciente_" + pacienteId);
        try {
            Files.createDirectories(base);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar pasta de anexos: " + base, e);
        }

        String nomeArquivo = "EVOLUCAO_" + anamneseId + ".txt";
        Path destino = base.resolve(nomeArquivo);

        try {
            Files.writeString(destino, conteudo == null ? "" : conteudo, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao escrever arquivo da evolução: " + destino, e);
        }

        Long tamanho = null;
        try { tamanho = Files.size(destino); } catch (Exception ignored) {}

        String agora = LocalDateTime.now().format(DB_FMT);

        // verifica se já existe um anexo "EVOLUCAO" para esta anamneseId
        Long existenteId = null;
        String q = """
            SELECT id FROM anexo_paciente
             WHERE paciente_id = ?
               AND anamnese = ?
               AND (descricao = 'EVOLUCAO' OR descricao IS NULL)
             ORDER BY id DESC
             LIMIT 1
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setLong(1, pacienteId);
            ps.setInt(2, anamneseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) existenteId = rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar anexo de evolução existente", e);
        }

        if (existenteId == null) {
            String ins = """
                INSERT INTO anexo_paciente
                (paciente_id, anamnese, nome_arquivo, caminho_arquivo, tamanho_bytes, descricao, data_hora)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(ins)) {

                ps.setLong(1, pacienteId);
                ps.setInt(2, anamneseId);
                ps.setString(3, nomeArquivo);
                ps.setString(4, destino.toAbsolutePath().toString());

                if (tamanho == null) ps.setNull(5, Types.BIGINT);
                else ps.setLong(5, tamanho);

                ps.setString(6, "EVOLUCAO");
                ps.setString(7, agora);

                ps.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Erro ao inserir anexo TXT da evolução", e);
            }
        } else {
            String upd = """
                UPDATE anexo_paciente
                   SET nome_arquivo = ?,
                       caminho_arquivo = ?,
                       tamanho_bytes = ?,
                       descricao = 'EVOLUCAO',
                       data_hora = ?
                 WHERE id = ?
            """;
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(upd)) {

                ps.setString(1, nomeArquivo);
                ps.setString(2, destino.toAbsolutePath().toString());

                if (tamanho == null) ps.setNull(3, Types.BIGINT);
                else ps.setLong(3, tamanho);

                ps.setString(4, agora);
                ps.setLong(5, existenteId);

                ps.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Erro ao atualizar anexo TXT da evolução", e);
            }
        }
    }
}