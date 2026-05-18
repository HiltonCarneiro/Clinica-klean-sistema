package br.com.clinica.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class AgendaTimeParser {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter HORA_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm", LOCALE_PT_BR);

    private AgendaTimeParser() {
    }

    public static LocalTime parse(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(value.trim(), HORA_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    public static String format(LocalTime value) {

        if (value == null) {
            return "";
        }

        return value.format(HORA_FORMATTER);
    }
}