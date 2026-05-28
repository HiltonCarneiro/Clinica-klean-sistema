package br.com.clinica.util;

public class JsonFieldExtractor {

    public String getString(String json, String field) {
        if (json == null || json.isBlank() || field == null || field.isBlank()) {
            return "";
        }

        String t = json.trim();
        String key = "\"" + field + "\":\"";
        int i = t.indexOf(key);

        if (i < 0) {
            return "";
        }

        int start = i + key.length();
        int end = start;
        boolean escape = false;

        while (end < t.length()) {
            char c = t.charAt(end);

            if (escape) {
                escape = false;
                end++;
                continue;
            }

            if (c == '\\') {
                escape = true;
                end++;
                continue;
            }

            if (c == '"') {
                break;
            }

            end++;
        }

        if (end <= start || end >= t.length()) {
            return "";
        }

        String raw = t.substring(start, end);

        return raw.replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trim();
    }
}