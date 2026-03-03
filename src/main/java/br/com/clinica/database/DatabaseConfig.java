package br.com.clinica.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

public class DatabaseConfig {

    private static String VENDOR;
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    // Pool config
    private static int POOL_MAX = 10;
    private static int POOL_MIN_IDLE = 2;
    private static long POOL_CONN_TIMEOUT_MS = 10_000;
    private static long POOL_IDLE_TIMEOUT_MS = 120_000;
    private static long POOL_MAX_LIFETIME_MS = 1_800_000;

    private static volatile boolean LOADED = false;

    private static HikariDataSource PG_DS;

    private static void loadPropsIfNeeded() {
        if (LOADED) return;

        Properties p = new Properties();

        // 1) tenta classpath
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/db.properties")) {
            if (in != null) p.load(in);
        } catch (Exception ignored) {}

        // 2) tenta arquivo local
        try {
            Path local = Paths.get("db.properties");
            if (Files.exists(local)) {
                try (InputStream in = Files.newInputStream(local)) {
                    p.load(in);
                }
            }
        } catch (Exception ignored) {}

        VENDOR = p.getProperty("db.vendor", "sqlite").trim().toLowerCase();
        URL = p.getProperty("db.url", "").trim();
        USER = p.getProperty("db.user", "").trim();
        PASSWORD = p.getProperty("db.password", "").trim();

        POOL_MAX = parseInt(p.getProperty("pool.max", "10"), 10);
        POOL_MIN_IDLE = parseInt(p.getProperty("pool.minIdle", "2"), 2);
        POOL_CONN_TIMEOUT_MS = parseLong(p.getProperty("pool.connectionTimeoutMs", "10000"), 10_000);
        POOL_IDLE_TIMEOUT_MS = parseLong(p.getProperty("pool.idleTimeoutMs", "120000"), 120_000);
        POOL_MAX_LIFETIME_MS = parseLong(p.getProperty("pool.maxLifetimeMs", "1800000"), 1_800_000);

        LOADED = true;
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static long parseLong(String s, long def) {
        try { return Long.parseLong(s.trim()); } catch (Exception e) { return def; }
    }

    private static boolean isPostgres() {
        return "postgres".equalsIgnoreCase(VENDOR) || "postgresql".equalsIgnoreCase(VENDOR);
    }

    private static void ensurePostgresDataSource() {
        if (PG_DS != null) return;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ignored) {}

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(URL);
        cfg.setUsername(USER);
        cfg.setPassword(PASSWORD);

        cfg.setMaximumPoolSize(Math.max(1, POOL_MAX));
        cfg.setMinimumIdle(Math.max(0, POOL_MIN_IDLE));
        cfg.setConnectionTimeout(POOL_CONN_TIMEOUT_MS);
        cfg.setIdleTimeout(POOL_IDLE_TIMEOUT_MS);
        cfg.setMaxLifetime(POOL_MAX_LIFETIME_MS);

        cfg.setValidationTimeout(3_000);
        cfg.setConnectionTestQuery("SELECT 1");

        // Timeouts do driver Postgres (evitam ficar “preso” em rede/SSL)
        cfg.addDataSourceProperty("connectTimeout", "5");  // segundos
        cfg.addDataSourceProperty("socketTimeout", "10");  // segundos
        cfg.addDataSourceProperty("loginTimeout", "5");    // segundos

        // Cache de prepared statements do driver (pequeno ganho e menos overhead)
        cfg.addDataSourceProperty("preparedStatementCacheQueries", "256");
        cfg.addDataSourceProperty("preparedStatementCacheSizeMiB", "8");

        // Ajuda a evitar “travadas” longas:
        cfg.addDataSourceProperty("tcpKeepAlive", "true");
        // Se você usa Supabase, normalmente precisa SSL:
        // (se já estiver no JDBC URL, ok)

        PG_DS = new HikariDataSource(cfg);
    }

    public static Connection getConnection() throws SQLException {
        loadPropsIfNeeded();

        if (isPostgres()) {
            ensurePostgresDataSource();
            return PG_DS.getConnection();
        }

        // SQLite (local)
        try { Class.forName("org.sqlite.JDBC"); } catch (ClassNotFoundException ignored) {}
        ensureSqliteFile();

        return DriverManager.getConnection(URL);
    }

    private static void ensureSqliteFile() {
        if (URL == null) return;
        if (!URL.startsWith("jdbc:sqlite:")) return;

        String pathStr = URL.replace("jdbc:sqlite:", "").trim();
        if (pathStr.isBlank()) return;

        try {
            Path path = Paths.get(pathStr).toAbsolutePath();
            Path dir = path.getParent();
            if (dir != null && !Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (Exception e) {
            System.out.println("Não foi possível preparar diretório do SQLite: " + e.getMessage());
        }
    }
}