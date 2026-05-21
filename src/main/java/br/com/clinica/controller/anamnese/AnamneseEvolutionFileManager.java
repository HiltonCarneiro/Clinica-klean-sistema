package br.com.clinica.controller.anamnese;

import br.com.clinica.model.Anamnese;

public class AnamneseEvolutionFileManager {

    public String gerarTextoEvolucaoParaArquivo(Anamnese anamnese, AnamneseJsonMapper jsonMapper) {
        if (anamnese == null) {
            return "";
        }

        String json = anamnese.getDadosJson() == null ? "" : anamnese.getDadosJson();
        StringBuilder sb = new StringBuilder();

        sb.append("Data/Hora: ").append(safe(anamnese.getDataHora())).append("\n");
        sb.append("Tipo: ").append(safe(anamnese.getTipo())).append("\n\n");

        sb.append("Queixa:\n").append(extrairCampo(jsonMapper, json, "queixa")).append("\n\n");
        sb.append("Evolução:\n").append(extrairCampo(jsonMapper, json, "evolucao")).append("\n\n");
        sb.append("Observações:\n").append(safe(anamnese.getObservacoes())).append("\n\n");

        sb.append("Sinais Vitais:\n");
        sb.append("PA: ").append(extrairCampo(jsonMapper, json, "pa")).append("\n");
        sb.append("FC: ").append(extrairCampo(jsonMapper, json, "fc")).append("\n");
        sb.append("FR: ").append(extrairCampo(jsonMapper, json, "fr")).append("\n");
        sb.append("Temp: ").append(extrairCampo(jsonMapper, json, "temp")).append("\n");
        sb.append("Peso: ").append(extrairCampo(jsonMapper, json, "peso")).append("\n");
        sb.append("Altura: ").append(extrairCampo(jsonMapper, json, "altura")).append("\n");
        sb.append("SpO2: ").append(extrairCampo(jsonMapper, json, "spo2")).append("\n\n");

        return sb.toString();
    }

    private String extrairCampo(AnamneseJsonMapper jsonMapper, String json, String campo) {
        if (jsonMapper == null) {
            return "";
        }

        return jsonMapper.extrairCampo(json, campo);
    }

    private String safe(String valor) {
        return valor == null ? "" : valor;
    }
}
