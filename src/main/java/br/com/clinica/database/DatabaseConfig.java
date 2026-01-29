package br.com.clinica.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    private static final String URL = "jdbc:sqlite:clinica.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");

            // ===== PERFIL =====
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS perfil (
                    id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL UNIQUE
                );
            """);

            // ===== USUARIO (com pessoa_nome) =====
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuario (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome        TEXT NOT NULL,            -- CARGO (ex: ENFERMEIRA)
                    pessoa_nome TEXT,                     -- Nome da pessoa
                    login       TEXT NOT NULL UNIQUE,     -- login normalizado (ex: patricia.goncalo)
                    senha       TEXT NOT NULL,
                    ativo       INTEGER NOT NULL DEFAULT 1,
                    perfil_id   INTEGER NOT NULL,
                    FOREIGN KEY (perfil_id) REFERENCES perfil(id)
                );
            """);

            // MIGRAÇÃO: se banco antigo não tinha pessoa_nome
            try {
                stmt.execute("ALTER TABLE usuario ADD COLUMN pessoa_nome TEXT;");
            } catch (SQLException ignored) { }

            // ===== PACIENTE =====
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

            // ===== PRODUTO =====
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS produto (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome           TEXT NOT NULL,
                    tipo           TEXT NOT NULL,
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

            // ===== AGENDAMENTO =====
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS agendamento (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    data            TEXT NOT NULL,
                    hora_inicio     TEXT NOT NULL,
                    hora_fim        TEXT NOT NULL,
                    paciente_id     INTEGER,
                    profissional_id INTEGER NOT NULL,
                    sala            TEXT NOT NULL,
                    procedimento    TEXT,
                    observacoes     TEXT,
                    status          TEXT NOT NULL DEFAULT 'AGENDADO',
                    FOREIGN KEY (paciente_id) REFERENCES paciente(id),
                    FOREIGN KEY (profissional_id) REFERENCES usuario(id)
                );
            """);

            // ===== MIGRAÇÃO PERFIL: MEDICO -> MEDICA (se existir) =====
            stmt.execute("UPDATE perfil SET nome='MEDICA' WHERE nome='MEDICO';");

            // ===== PERFIS INICIAIS =====
            stmt.execute("""
                INSERT OR IGNORE INTO perfil (nome) VALUES
                    ('ADMINISTRADOR'),
                    ('ENFERMEIRA'),
                    ('RECEPCIONISTA'),
                    ('NUTRICIONISTA'),
                    ('FISIOTERAPEUTA'),
                    ('PSICOLOGA'),
                    ('MEDICA');
            """);

            // ===== USUÁRIOS INICIAIS (login normalizado) =====
            stmt.execute("""
                INSERT OR IGNORE INTO usuario (nome, pessoa_nome, login, senha, ativo, perfil_id) VALUES
                    ('ENFERMEIRA',     'Irenilza Lins',      'irencilza.lins',       '123',   1, (SELECT id FROM perfil WHERE nome='ENFERMEIRA')),
                    ('NUTRICIONISTA',  'Gerlane Cavalcante', 'gerlane.cavalcante',   '123',   1, (SELECT id FROM perfil WHERE nome='NUTRICIONISTA')),
                    ('FISIOTERAPEUTA', 'Isabelly Fernandes', 'isabelly.fernandes',   '123',   1, (SELECT id FROM perfil WHERE nome='FISIOTERAPEUTA')),
                    ('MEDICA',         'Enolla Mayenne',     'enolla.mayenne',       '123',   1, (SELECT id FROM perfil WHERE nome='MEDICA')),
                    ('PSICOLOGA',      'Gisélia Costa',      'giselia.costa',        '123',   1, (SELECT id FROM perfil WHERE nome='PSICOLOGA')),
                    ('RECEPCIONISTA',  'Patrícia Gonçalo',   'patricia.goncalo',     '123',   1, (SELECT id FROM perfil WHERE nome='RECEPCIONISTA')),
                    ('ADMINISTRADOR',  'Irenilza Lins',      'irencilza.lins.admin', 'admin', 1, (SELECT id FROM perfil WHERE nome='ADMINISTRADOR'));
            """);

            // MIGRAÇÃO: se existia usuário "medico"
            stmt.execute("""
                UPDATE usuario
                   SET nome='MEDICA',
                       perfil_id=(SELECT id FROM perfil WHERE nome='MEDICA')
                 WHERE nome='MEDICO';
            """);

            System.out.println("Banco de dados inicializado/migrado com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao inicializar o banco: " + e.getMessage());
        }
    }
}
