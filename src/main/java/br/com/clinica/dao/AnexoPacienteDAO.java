package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
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

        public File getFile() { return new File(caminhoArquivo); }
    }

    // Inserir / Listar / Remover

    public long anexarPdf(Long pacienteId, Integer anamneseId, File arquivoPdf, String descricao) {
        if (pacienteId == null) throw new IllegalArgumentException("pacienteId é obrigatório");
        if (arquivoPdf == null) throw new IllegalArgumentException("Arquivo é obrigatório");
        if (!arquivoPdf.exists()) throw new IllegalArgumentException("Arquivo não existe");
        if (!arquivoPdf.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Somente PDF é permitido.");
        }

        // pasta destino: {projeto}/data/anexos/paciente_{id}/
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
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, pacienteId);

            if (anamneseId == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, anamneseId); // ✅ coluna "anamnese"

            ps.setString(3, arquivoPdf.getName()); // ✅ nome_arquivo
            ps.setString(4, destino.toAbsolutePath().toString()); // caminho_arquivo

            if (tamanho == null) ps.setNull(5, Types.BIGINT);
            else ps.setLong(5, tamanho); // tamanho_bytes

            if (descricao == null || descricao.isBlank()) ps.setNull(6, Types.VARCHAR);
            else ps.setString(6, descricao.trim());

            ps.setString(7, agora);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }

            return -1;

        } catch (SQLException e) {
            // se falhar no banco, tenta apagar o arquivo copiado
            try { Files.deleteIfExists(destino); } catch (Exception ignored) {}
            throw new RuntimeException("Erro ao inserir anexo no banco", e);
        }
    }

    // overload (se você chamar sem descrição)
    public long anexarPdf(Long pacienteId, Integer anamneseId, File arquivoPdf) {
        return anexarPdf(pacienteId, anamneseId, arquivoPdf, null);
    }

    public List<AnexoPacienteItem> listarPorPaciente(Long pacienteId) {
        if (pacienteId == null) return List.of();

        String sql = """
                SELECT id, paciente_id, anamnese, nome_arquivo, caminho_arquivo, tamanho_bytes, descricao, data_hora
                FROM anexo_paciente
                WHERE paciente_id = ?
                ORDER BY data_hora DESC, id DESC
                """;

        List<AnexoPacienteItem> list = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, pacienteId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    Long pid = rs.getLong("paciente_id");

                    int a = rs.getInt("anamnese");
                    Integer anamnese = rs.wasNull() ? null : a;

                    String nome = rs.getString("nome_arquivo");
                    String caminho = rs.getString("caminho_arquivo");

                    long t = rs.getLong("tamanho_bytes");
                    Long tamanho = rs.wasNull() ? null : t;

                    String desc = rs.getString("descricao");
                    String dh = rs.getString("data_hora");

                    list.add(new AnexoPacienteItem(id, pid, anamnese, nome, caminho, tamanho, desc, dh));
                }
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar anexos do paciente", e);
        }
    }

    public void remover(Long anexoId) {
        if (anexoId == null) return;

        String caminho = null;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT caminho_arquivo FROM anexo_paciente WHERE id = ?")) {
            ps.setLong(1, anexoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) caminho = rs.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar anexo", e);
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM anexo_paciente WHERE id = ?")) {
            ps.setLong(1, anexoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover anexo no banco", e);
        }

        if (caminho != null && !caminho.isBlank()) {
            try { Files.deleteIfExists(Paths.get(caminho)); } catch (IOException ignored) {}
        }
    }

    public void abrirNoSistema(File pdf) {
        if (pdf == null || !pdf.exists()) throw new IllegalArgumentException("Arquivo não encontrado.");
        if (!Desktop.isDesktopSupported()) throw new IllegalStateException("Desktop não suportado nesse ambiente.");
        try {
            Desktop.getDesktop().open(pdf);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível abrir o PDF.", e);
        }
    }

    // =========================================================
    // Tabela / Ajustes
    // =========================================================

    private void criarOuAjustarTabela() {
        // Cria se não existir (exatamente no formato do seu print)
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

        // garante colunas caso alguma instalação antiga esteja diferente
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
}
