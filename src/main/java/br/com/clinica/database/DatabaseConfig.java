package br.com.clinica.database;

import br.com.clinica.auth.Perfis;
import br.com.clinica.auth.Permissao;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

public class DatabaseConfig {

    // Carregado de src/main/resources/db.properties
    private static String VENDOR;
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static boolean LOADED = false;

    private DatabaseConfig() {}

    private static synchronized void loadPropsIfNeeded() {
        if (LOADED) return;

        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar /db.properties", e);
        }

        VENDOR = props.getProperty("db.vendor", "sqlite").trim();
        // ✅ fallback correto (banco real), não clinica_backup.db
        URL = props.getProperty("db.url", "jdbc:sqlite:clinica.db").trim();

        USER = props.getProperty("db.user", "");
        PASSWORD = props.getProperty("db.password", "");

        if (USER == null) USER = "";
        if (PASSWORD == null) PASSWORD = "";

        LOADED = true;
    }

    private static boolean isSqlite() {
        return VENDOR != null && VENDOR.equalsIgnoreCase("sqlite");
    }

    private static boolean isPostgres() {
        return VENDOR != null && VENDOR.equalsIgnoreCase("postgres");
    }

    private static void ensureSqliteFileDirectory() {
        if (!isSqlite()) return;
        if (URL == null) return;

        final String prefix = "jdbc:sqlite:";
        if (!URL.startsWith(prefix)) return;

        String path = URL.substring(prefix.length()).trim();
        if (path.isBlank() || ":memory:".equals(path)) return;

        // Normaliza (caso alguém use file:)
        if (path.startsWith("file:")) path = path.substring("file:".length());

        Path p = Paths.get(path).normalize();
        Path parent = p.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (Exception e) {
                throw new RuntimeException("Não foi possível criar o diretório do SQLite em: " + parent, e);
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        loadPropsIfNeeded();

        // Drivers (opcional, mas ajuda)
        try {
            if (isPostgres()) Class.forName("org.postgresql.Driver");
            if (isSqlite()) Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {}

        if (isSqlite()) {
            ensureSqliteFileDirectory();
        }

        Connection conn;
        if (USER.isBlank()) {
            conn = DriverManager.getConnection(URL);
        } else {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        }

        // SQLite: habilita FK
        if (isSqlite()) {
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON;");
            }
        }

        return conn;
    }

    /**
     * Inicializa o schema do SQLite (desenvolvimento/local).
     * Para PostgreSQL (nuvem/produção), o recomendado é criar o schema por script/migrations
     * e aqui apenas retornar.
     */
    public static void initializeDatabase() {
        loadPropsIfNeeded();

        // No Postgres: schema deve ser criado por script/migration (não aqui, com SQL de SQLite)
        if (isPostgres()) {
            return;
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");

            // =========================
            // PERFIL
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS perfil (
                    id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL UNIQUE
                );
            """);

            // =========================
            // USUARIO
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuario (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome        TEXT NOT NULL,            -- CARGO (ex: ENFERMEIRA)
                    pessoa_nome TEXT,                     -- Nome da pessoa
                    login       TEXT NOT NULL UNIQUE,     -- login (ex: patricia.goncalo)
                    senha       TEXT NOT NULL,
                    ativo       INTEGER NOT NULL DEFAULT 1,
                    perfil_id   INTEGER NOT NULL,
                    FOREIGN KEY (perfil_id) REFERENCES perfil(id)
                );
            """);

            // Migração: se banco antigo não tinha pessoa_nome
            try {
                stmt.execute("ALTER TABLE usuario ADD COLUMN pessoa_nome TEXT;");
            } catch (SQLException ignored) {}

            // =========================
            // PERFIL_PERMISSAO
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS perfil_permissao (
                    perfil_id INTEGER NOT NULL,
                    permissao TEXT NOT NULL,
                    PRIMARY KEY (perfil_id, permissao),
                    FOREIGN KEY (perfil_id) REFERENCES perfil(id)
                );
            """);

            // =========================
            // PACIENTE
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS paciente (
                    id                INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome              TEXT NOT NULL,
                    cpf               TEXT UNIQUE,
                    data_nascimento   TEXT,
                    telefone          TEXT,
                    endereco          TEXT,
                    responsavel_legal TEXT,
                    ativo             INTEGER NOT NULL DEFAULT 1,
                    rua               TEXT,
                    numero            TEXT,
                    bairro            TEXT,
                    cidade            TEXT,
                    cep               TEXT,
                    uf                TEXT
                );
            """);

            // =========================
            // PRODUTO
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS produto (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome           TEXT NOT NULL,
                    tipo           TEXT NOT NULL,         -- enum TipoProduto name()
                    unidade        TEXT,
                    estoque_atual  REAL NOT NULL DEFAULT 0,
                    estoque_minimo REAL NOT NULL DEFAULT 0,
                    lote           TEXT,
                    validade       TEXT,
                    preco_custo    REAL,
                    preco_venda    REAL,
                    ativo          INTEGER NOT NULL DEFAULT 1
                );
            """);

            // =========================
            // AGENDAMENTO
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS agendamento (
                    id                INTEGER PRIMARY KEY AUTOINCREMENT,
                    data              TEXT NOT NULL,
                    hora_inicio       TEXT NOT NULL,
                    hora_fim          TEXT NOT NULL,
                    profissional_id   INTEGER NOT NULL,
                    profissional_nome TEXT NOT NULL,
                    paciente_id       INTEGER,
                    paciente_nome     TEXT,
                    sala              TEXT NOT NULL,
                    procedimento      TEXT,
                    observacoes       TEXT,
                    status            TEXT NOT NULL,
                    FOREIGN KEY (paciente_id) REFERENCES paciente(id),
                    FOREIGN KEY (profissional_id) REFERENCES usuario(id)
                );
            """);

            // =========================
            // ANAMNESE
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS anamnese (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    paciente_id     INTEGER NOT NULL,
                    agendamento_id  INTEGER,
                    profissional_id INTEGER NOT NULL,
                    data_hora       TEXT NOT NULL,
                    tipo            TEXT NOT NULL DEFAULT 'EVOLUCAO',
                    dados_json      TEXT NOT NULL,
                    observacoes     TEXT,
                    FOREIGN KEY (paciente_id) REFERENCES paciente(id),
                    FOREIGN KEY (agendamento_id) REFERENCES agendamento(id),
                    FOREIGN KEY (profissional_id) REFERENCES usuario(id)
                );
            """);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_anamnese_agendamento ON anamnese(agendamento_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_anamnese_paciente_data ON anamnese(paciente_id, data_hora);");

            // =========================
            // ANEXO_PACIENTE
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS anexo_paciente (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    paciente_id     INTEGER NOT NULL,
                    nome_arquivo    TEXT NOT NULL,
                    caminho_arquivo TEXT NOT NULL,
                    tamanho_bytes   INTEGER,
                    descricao       TEXT,
                    data_hora       TEXT NOT NULL,
                    anamnese_id     INTEGER,
                    FOREIGN KEY (paciente_id) REFERENCES paciente(id),
                    FOREIGN KEY (anamnese_id) REFERENCES anamnese(id)
                );
            """);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_anexo_paciente_paciente ON anexo_paciente(paciente_id);");

            // =========================
            // MOVIMENTO_CAIXA
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS movimento_caixa (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    data            TEXT NOT NULL, -- formato yyyy-MM-dd
                    descricao       TEXT NOT NULL,
                    tipo            TEXT NOT NULL, -- ENTRADA/SAIDA
                    valor           REAL NOT NULL,
                    forma_pagamento TEXT,
                    paciente_nome   TEXT,
                    observacao      TEXT
                );
            """);

            // =========================
            // NOTA
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS nota (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    data_hora       TEXT NOT NULL,
                    id_paciente     INTEGER NOT NULL,
                    id_profissional INTEGER NOT NULL,
                    forma_pagamento TEXT NOT NULL,
                    total_bruto     REAL NOT NULL,
                    desconto        REAL NOT NULL DEFAULT 0,
                    total_liquido   REAL NOT NULL,
                    observacao      TEXT,
                    FOREIGN KEY (id_paciente) REFERENCES paciente(id),
                    FOREIGN KEY (id_profissional) REFERENCES usuario(id)
                );
            """);

            // =========================
            // NOTA_ITEM
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS nota_item (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_nota        INTEGER NOT NULL,
                    tipo_item      TEXT NOT NULL,     -- PRODUTO ou PROCEDIMENTO
                    id_produto     INTEGER,           -- pode ser null se PROCEDIMENTO
                    descricao      TEXT NOT NULL,
                    quantidade     REAL NOT NULL,
                    valor_unitario REAL NOT NULL,
                    valor_total    REAL NOT NULL,
                    FOREIGN KEY (id_nota) REFERENCES nota(id),
                    FOREIGN KEY (id_produto) REFERENCES produto(id)
                );
            """);

            // =========================
            // SEED (perfis + permissões)
            // =========================
            seedPerfis(conn);
            seedPermissoes(conn);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar banco: " + e.getMessage(), e);
        }
    }

    private static void seedPerfis(Connection conn) throws SQLException {
        // SQLite
        String sql = "INSERT OR IGNORE INTO perfil (nome) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String[] perfis = {
                    Perfis.ADMIN,
                    Perfis.RECEPCIONISTA,
                    Perfis.ENFERMEIRA,
                    Perfis.NUTRICIONISTA,
                    Perfis.FISIOTERAPEUTA,
                    Perfis.PSICOLOGA,
                    Perfis.MEDICA
            };

            for (String p : perfis) {
                ps.setString(1, p);
                ps.executeUpdate();
            }
        }
    }

    private static void seedPermissoes(Connection conn) throws SQLException {
        long idAdmin = idPerfil(conn, Perfis.ADMIN);
        long idRecep = idPerfil(conn, Perfis.RECEPCIONISTA);
        long idEnf = idPerfil(conn, Perfis.ENFERMEIRA);
        long idNutri = idPerfil(conn, Perfis.NUTRICIONISTA);
        long idFisio = idPerfil(conn, Perfis.FISIOTERAPEUTA);
        long idPsico = idPerfil(conn, Perfis.PSICOLOGA);
        long idMed = idPerfil(conn, Perfis.MEDICA);

        insertPermissoes(conn, idAdmin, EnumSet.allOf(Permissao.class));

        insertPermissoes(conn, idRecep, EnumSet.of(
                Permissao.ESTOQUE_VER,
                Permissao.ESTOQUE_CADASTRAR_EDITAR,
                Permissao.ESTOQUE_ATIVAR_INATIVAR,
                Permissao.AGENDA_VER,
                Permissao.AGENDA_GERENCIAR,
                Permissao.USUARIO_GERENCIAR,
                Permissao.PACIENTE_CRIAR,
                Permissao.PACIENTE_EDITAR,
                Permissao.PACIENTE_VER,
                Permissao.PACIENTE_ATIVAR_INATIVAR,
                Permissao.FINANCEIRO_VER,
                Permissao.NOTA_GERAR
        ));

        Set<Permissao> profissionalSaude = EnumSet.of(
                Permissao.AGENDA_VER,
                Permissao.AGENDA_GERENCIAR,
                Permissao.PACIENTE_VER,
                Permissao.PACIENTE_CRIAR,
                Permissao.PACIENTE_EDITAR,
                Permissao.RELATORIOS_VER,
                Permissao.PRONTUARIO_VER,
                Permissao.PRONTUARIO_CRIAR,
                Permissao.PRONTUARIO_EDITAR_PROPRIO
        );

        insertPermissoes(conn, idEnf, profissionalSaude);
        insertPermissoes(conn, idNutri, profissionalSaude);
        insertPermissoes(conn, idFisio, profissionalSaude);
        insertPermissoes(conn, idPsico, profissionalSaude);
        insertPermissoes(conn, idMed, profissionalSaude);
    }

    private static long idPerfil(Connection conn, String nomePerfil) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM perfil WHERE nome = ?")) {
            ps.setString(1, nomePerfil);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }
        throw new SQLException("Perfil não encontrado no banco: " + nomePerfil);
    }

    private static void insertPermissoes(Connection conn, long perfilId, Set<Permissao> permissoes) throws SQLException {
        String sql = "INSERT OR IGNORE INTO perfil_permissao (perfil_id, permissao) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Permissao p : permissoes) {
                ps.setLong(1, perfilId);
                ps.setString(2, p.name());
                ps.executeUpdate();
            }
        }
    }
}