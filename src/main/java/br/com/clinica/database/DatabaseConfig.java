package br.com.clinica.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    // Caminho do arquivo SQLite (será criado na pasta raiz do projeto)
    private static final String URL = "jdbc:sqlite:clinica.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Garante que as foreign keys funcionem corretamente
            stmt.execute("PRAGMA foreign_keys = ON;");

            // ====== TABELA PERFIL ======
            String sqlPerfil = """
                CREATE TABLE IF NOT EXISTS perfil (
                    id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL UNIQUE
                );
            """;

            // ====== TABELA USUARIO ======
            String sqlUsuario = """
                CREATE TABLE IF NOT EXISTS usuario (
                    id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome      TEXT NOT NULL,            -- aqui usamos o CARGO (ex: ENFERMEIRA)
                    login     TEXT NOT NULL UNIQUE,
                    senha     TEXT NOT NULL,
                    ativo     INTEGER NOT NULL DEFAULT 1,
                    perfil_id INTEGER NOT NULL,
                    FOREIGN KEY (perfil_id) REFERENCES perfil(id)
                );
            """;

            // ====== TABELA PACIENTE (com endereço detalhado) ======
            String sqlPaciente = """
                CREATE TABLE IF NOT EXISTS paciente (
                    id                INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome              TEXT NOT NULL,
                    cpf               TEXT UNIQUE,
                    data_nascimento   TEXT,
                    telefone          TEXT,
                    endereco          TEXT,          -- campo antigo (pode continuar usando se quiser)
                    responsavel_legal TEXT,
                    ativo             INTEGER NOT NULL DEFAULT 1,
                    rua               TEXT,
                    numero            TEXT,
                    bairro            TEXT,
                    cidade            TEXT,
                    cep               TEXT,
                    uf                TEXT
                );
            """;

            // ====== TABELA PRODUTO (ESTOQUE) ======
            String sqlProduto = """
                CREATE TABLE IF NOT EXISTS produto (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome           TEXT NOT NULL,
                    tipo           TEXT NOT NULL,           -- INSUMO ou VENDA
                    unidade        TEXT,                    -- ml, un, caixa, etc.
                    estoque_atual  REAL NOT NULL DEFAULT 0,
                    estoque_minimo REAL NOT NULL DEFAULT 0,
                    lote           TEXT,
                    validade       TEXT,                    -- yyyy-MM-dd
                    preco_custo    REAL,
                    preco_venda    REAL,
                    ativo          INTEGER NOT NULL DEFAULT 1
                );
            """;

            // Cria / garante as tabelas
            stmt.execute(sqlPerfil);
            stmt.execute(sqlUsuario);
            stmt.execute(sqlPaciente);
            stmt.execute(sqlProduto);

            // ====== DADOS INICIAIS DE PERFIL ======
            String sqlPerfisIniciais = """
                INSERT OR IGNORE INTO perfil (id, nome) VALUES
                    (1, 'ADMIN'),
                    (2, 'ENFERMEIRA'),
                    (3, 'RECEPCIONISTA'),
                    (4, 'NUTRICIONISTA'),
                    (5, 'FISIOTERAPEUTA'),
                    (6, 'MEDICO');
            """;
            stmt.execute(sqlPerfisIniciais);

            // ====== USUÁRIOS INICIAIS (NOME = CARGO) ======
            String sqlUsuariosIniciais = """
                INSERT OR IGNORE INTO usuario (id, nome, login, senha, ativo, perfil_id) VALUES
                    (1, 'ADMIN',          'admin',          'admin', 1, 1),
                    (2, 'ENFERMEIRA',     'enfermeira',     '123',   1, 2),
                    (3, 'RECEPCIONISTA',  'recepcionista',  '123',   1, 3),
                    (4, 'NUTRICIONISTA',  'nutricionista',  '123',   1, 4),
                    (5, 'FISIOTERAPEUTA', 'fisioterapeuta', '123',   1, 5),
                    (6, 'MEDICO',         'medico',         '123',   1, 6);
            """;
            stmt.execute(sqlUsuariosIniciais);

            System.out.println("Banco de dados inicializado com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao inicializar o banco: " + e.getMessage());
        }
    }
}