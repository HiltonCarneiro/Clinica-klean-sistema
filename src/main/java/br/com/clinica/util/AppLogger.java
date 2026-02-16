package br.com.clinica.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.*;

public final class AppLogger {

    private static final Logger LOG = Logger.getLogger("ClinicaApp");
    private static boolean initialized = false;

    private AppLogger() {}

    public static synchronized Logger get() {
        if (!initialized) init();
        return LOG;
    }

    private static void init() {
        try {
            Path dir = Path.of("logs");
            Files.createDirectories(dir);

            Handler fh = new FileHandler("logs/app.log", true);
            fh.setFormatter(new SimpleFormatter());
            fh.setLevel(Level.ALL);

            LOG.setUseParentHandlers(false); // não duplica no console
            LOG.addHandler(fh);
            LOG.setLevel(Level.ALL);

            initialized = true;

        } catch (IOException e) {
            // fallback: mantém logger padrão
            initialized = true;
        }
    }

    public static void error(String msg, Throwable t) {
        get().log(Level.SEVERE, msg, t);
    }

    public static void info(String msg) {
        get().info(msg);
    }
}
