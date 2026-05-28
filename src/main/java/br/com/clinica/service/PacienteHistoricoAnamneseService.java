package br.com.clinica.service;

import br.com.clinica.dao.AnamneseDAO;
import br.com.clinica.model.Anamnese;
import br.com.clinica.util.JsonFieldExtractor;

import java.util.Collections;
import java.util.List;

public class PacienteHistoricoAnamneseService {

    private final AnamneseDAO anamneseDAO = new AnamneseDAO();
    private final JsonFieldExtractor jsonFieldExtractor = new JsonFieldExtractor();

    public List<Anamnese> listarPorPaciente(Long pacienteId) {
        if (pacienteId == null) {
            return Collections.emptyList();
        }

        return anamneseDAO.listarPorPaciente(pacienteId);
    }

    public String montarResumo(Anamnese anamnese) {
        if (anamnese == null) {
            return "";
        }

        String rawJson = safe(anamnese.getDadosJson());
        String obs = safe(anamnese.getObservacoes());

        String tipo = formatTipo(safe(anamnese.getTipo()));
        String dataHora = safe(anamnese.getDataHora());

        String queixa = jsonFieldExtractor.getString(rawJson, "queixa");
        String evolucao = jsonFieldExtractor.getString(rawJson, "evolucao");

        String pa = jsonFieldExtractor.getString(rawJson, "pa");
        String fc = jsonFieldExtractor.getString(rawJson, "fc");
        String fr = jsonFieldExtractor.getString(rawJson, "fr");
        String temp = jsonFieldExtractor.getString(rawJson, "temp");
        String peso = jsonFieldExtractor.getString(rawJson, "peso");
        String altura = jsonFieldExtractor.getString(rawJson, "altura");
        String spo2 = jsonFieldExtractor.getString(rawJson, "spo2");

        StringBuilder sb = new StringBuilder();

        sb.append("Registro clínico\n");
        sb.append(tipo);

        if (!dataHora.isBlank()) {
            sb.append(" — ").append(dataHora);
        }

        sb.append("\n\n");

        appendSecaoTexto(sb, "Queixa principal", queixa);
        appendSecaoTexto(sb, "Evolução", evolucao);
        appendSecaoTexto(sb, "Sinais vitais", montarLinhaSinaisVitais(pa, fc, fr, temp, peso, altura, spo2));
        appendSecaoTexto(sb, "Observações", obs);

        String out = sb.toString().trim();

        return out.isBlank() ? "Sem detalhes para exibir neste registro." : out;
    }

    public String formatTipo(String tipo) {
        if (tipo == null) return "";

        return switch (tipo.trim().toUpperCase()) {
            case "ANAMNESE_INICIAL" -> "Anamnese inicial";
            case "EVOLUCAO" -> "Evolução";
            default -> tipo;
        };
    }

    private void appendSecaoTexto(StringBuilder sb, String titulo, String texto) {
        String valor = safe(texto).trim();

        if (valor.isBlank()) {
            return;
        }

        sb.append(titulo).append("\n");
        sb.append(valor).append("\n\n");
    }

    private String montarLinhaSinaisVitais(
            String pa,
            String fc,
            String fr,
            String temp,
            String peso,
            String altura,
            String spo2
    ) {
        StringBuilder s = new StringBuilder();

        appendCampo(s, "PA", pa, "mmHg");
        appendCampo(s, "FC", fc, "bpm");
        appendCampo(s, "FR", fr, "irpm");
        appendCampo(s, "Temp", temp, "°C");
        appendCampo(s, "Peso", peso, "kg");
        appendCampo(s, "Altura", altura, "m");
        appendCampo(s, "SpO₂", spo2, "%");

        return s.toString().trim();
    }

    private void appendCampo(StringBuilder sb, String label, String valor, String sufixo) {
        String v = safe(valor).trim();

        if (v.isBlank()) {
            return;
        }

        if (sb.length() > 0) {
            sb.append("   ");
        }

        sb.append(label).append(": ").append(v);

        if (sufixo != null && !sufixo.isBlank()) {
            sb.append(" ").append(sufixo);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}