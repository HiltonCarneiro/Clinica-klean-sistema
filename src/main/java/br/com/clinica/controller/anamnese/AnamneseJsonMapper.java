package br.com.clinica.controller.anamnese;

import java.util.Map;

public class AnamneseJsonMapper {

    public String montarJson(Map<String, String> campos) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        int index = 0;

        for (Map.Entry<String, String> entry : campos.entrySet()) {
            put(sb, entry.getKey(), entry.getValue());

            if (index < campos.size() - 1) {
                sb.append(",");
            }

            index++;
        }

        sb.append("}");
        return sb.toString();
    }

    public String extrairCampo(String json, String campo) {
        if (json == null || json.isBlank()) {
            return "";
        }

        String key = "\"" + campo + "\":\"";
        int i = json.indexOf(key);

        if (i < 0) {
            return "";
        }

        int start = i + key.length();
        int end = json.indexOf("\"", start);

        if (end < 0) {
            return "";
        }

        return json.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public boolean isTrue(String value) {
        if (value == null) {
            return false;
        }

        value = value.trim();

        return value.equals("1")
                || value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("sim");
    }

    private void put(StringBuilder sb, String campo, String valor) {
        sb.append("\"")
                .append(campo)
                .append("\":\"")
                .append(escape(valor))
                .append("\"");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}