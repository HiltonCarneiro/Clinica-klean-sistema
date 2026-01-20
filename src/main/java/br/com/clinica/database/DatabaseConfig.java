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
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            String sqlPaciente = """
                CREATE TABLE IF NOT EXISTS paciente (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    cpf TEXT UNIQUE,
                    data_nascimento TEXT,
                    telefone TEXT,
                    endereco TEXT,
                    responsavel_legal TEXT,
                    ativo INTEGER NOT NULL DEFAULT 1
                );
            """;

            stmt.execute(sqlPaciente);

            System.out.println("Banco de dados inicializado com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao inicializar o banco: " + e.getMessage());
        }
    }
}