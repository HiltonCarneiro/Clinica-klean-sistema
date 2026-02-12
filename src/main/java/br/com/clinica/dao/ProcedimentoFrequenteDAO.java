package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.enums.SalaAtendimento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProcedimentoFrequenteDAO {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DB_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", LOCALE_PT_BR);

    public ProcedimentoFrequenteDAO() {
        garantirTabela();
    }

    private void garantirTabela() {
        String sql = """
            CREATE TABLE IF NOT EXISTS procedimento_frequente (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              nome TEXT NOT NULL,
              sala TEXT,
              uso_count INTEGER NOT NULL DEFAULT 0,
              ultimo_uso TEXT NOT NULL,
              UNIQUE(nome, sala)
            );
        """;

        String idx = """
            CREATE INDEX IF NOT EXISTS idx_proc_freq_sala_uso
            ON procedimento_frequente(sala, uso_count DESC, ultimo_uso DESC);
        """;

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             PreparedStatement ps2 = c.prepareStatement(idx)) {
            ps.execute();
            ps2.execute();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao garantir tabela de procedimentos frequentes", e);
        }
    }

    public void registrarUso(String nomeProcedimento, SalaAtendimento sala) {
        String nome = normalizar(nomeProcedimento);
        if (nome.isBlank()) return;

        String salaStr = (sala == null) ? null : sala.name();
        String agora = LocalDateTime.now().format(DB_FMT);

        String upsert = """
            INSERT INTO procedimento_frequente (nome, sala, uso_count, ultimo_uso)
            VALUES (?, ?, 1, ?)
            ON CONFLICT(nome, sala)
            DO UPDATE SET uso_count = uso_count + 1,
                          ultimo_uso = excluded.ultimo_uso;
        """;

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(upsert)) {

            ps.setString(1, nome);

            if (salaStr == null) ps.setNull(2, Types.VARCHAR);
            else ps.setString(2, salaStr);

            ps.setString(3, agora);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao registrar uso de procedimento", e);
        }
    }

    public List<String> sugerir(String digitado, SalaAtendimento sala, int limite) {
        String t = (digitado == null) ? "" : digitado.trim().toLowerCase(LOCALE_PT_BR);
        String salaStr = (sala == null) ? null : sala.name();

        List<String> out = new ArrayList<>();

        String sql = """
            SELECT nome
            FROM procedimento_frequente
            WHERE (? IS NULL OR sala = ? OR sala IS NULL)
              AND (LOWER(nome) LIKE ?)
            ORDER BY
              CASE WHEN sala = ? THEN 0
                   WHEN sala IS NULL THEN 1
                   ELSE 2 END,
              uso_count DESC,
              ultimo_uso DESC
            LIMIT ?;
        """;

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (salaStr == null) {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(1, salaStr);
                ps.setString(2, salaStr);
            }

            ps.setString(3, "%" + t + "%");

            if (salaStr == null) ps.setNull(4, Types.VARCHAR);
            else ps.setString(4, salaStr);

            ps.setInt(5, Math.max(1, limite));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString("nome"));
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao sugerir procedimentos", e);
        }

        return out;
    }

    public String normalizar(String s) {
        if (s == null) return "";
        String x = s.trim().replaceAll("\\s{2,}", " ");
        if (x.isBlank()) return "";

        String first = x.substring(0, 1).toUpperCase(LOCALE_PT_BR);
        String rest = x.substring(1);
        return first + rest;
    }
}