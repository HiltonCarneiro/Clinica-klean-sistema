package br.com.clinica.controller.anamnese;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnamneseFormManager {

    public void preencherCamposBasicos(
            String json,
            AnamneseJsonMapper jsonMapper,
            TextArea taQueixa,
            TextArea taEvolucao,
            TextArea taObservacoes,
            String observacoes,
            TextField tfPA,
            TextField tfFC,
            TextField tfFR,
            TextField tfTemp,
            TextField tfPeso,
            TextField tfAltura,
            TextField tfSpO2,
            TextArea taAntecedentes,
            TextArea taMedicacoes,
            TextArea taAlergias,
            TextArea taCirurgias,
            TextArea taSono,
            TextArea taAtividadeFisica,
            TextArea taAlimentacao,
            TextArea taExameGeral,
            TextArea taExameSegmentar
    ) {
        taQueixa.setText(extrair(jsonMapper, json, "queixa"));
        taEvolucao.setText(extrair(jsonMapper, json, "evolucao"));
        taObservacoes.setText(observacoes == null ? "" : observacoes);

        tfPA.setText(extrair(jsonMapper, json, "pa"));
        tfFC.setText(extrair(jsonMapper, json, "fc"));
        tfFR.setText(extrair(jsonMapper, json, "fr"));
        tfTemp.setText(extrair(jsonMapper, json, "temp"));
        tfPeso.setText(extrair(jsonMapper, json, "peso"));
        tfAltura.setText(extrair(jsonMapper, json, "altura"));
        tfSpO2.setText(extrair(jsonMapper, json, "spo2"));

        taAntecedentes.setText(extrair(jsonMapper, json, "antecedentes"));
        taMedicacoes.setText(extrair(jsonMapper, json, "medicacoes"));
        taAlergias.setText(extrair(jsonMapper, json, "alergias"));
        taCirurgias.setText(extrair(jsonMapper, json, "cirurgias"));

        taSono.setText(extrair(jsonMapper, json, "sono"));
        taAtividadeFisica.setText(extrair(jsonMapper, json, "atividade_fisica"));
        taAlimentacao.setText(extrair(jsonMapper, json, "alimentacao"));

        taExameGeral.setText(extrair(jsonMapper, json, "exame_geral"));
        taExameSegmentar.setText(extrair(jsonMapper, json, "exame_segmentar"));
    }

    public String montarJsonCompleto(
            TextArea taQueixa,
            TextArea taEvolucao,
            TextField tfPA,
            TextField tfFC,
            TextField tfFR,
            TextField tfTemp,
            TextField tfPeso,
            TextField tfAltura,
            TextField tfSpO2,
            TextArea taAntecedentes,
            TextArea taMedicacoes,
            TextArea taAlergias,
            TextArea taCirurgias,
            TextArea taSono,
            TextArea taAtividadeFisica,
            TextArea taAlimentacao,
            TextArea taExameGeral,
            TextArea taExameSegmentar,
            CheckBox chkNegaAntecedentes,
            CheckBox chkHipertensao,
            CheckBox chkDiabetes,
            CheckBox chkDislipidemia,
            CheckBox chkAsma,
            CheckBox chkCardiopatia,
            CheckBox chkDoencaRenal,
            CheckBox chkDoencaHepatica,
            CheckBox chkAutoimune,
            CheckBox chkCancer,
            CheckBox chkAnsiedadeDepressao,
            CheckBox chkSemMedicacoes,
            CheckBox chkNegaAlergias,
            CheckBox chkNegaCirurgias,
            ComboBox<String> cbTabagismo,
            ComboBox<String> cbAlcool,
            AnamneseClinicalHistoryManager clinicalHistoryManager,
            AnamneseJsonMapper jsonMapper
    ) {
        Map<String, String> campos = new LinkedHashMap<>();

        campos.put("queixa", safe(taQueixa));
        campos.put("evolucao", safe(taEvolucao));

        campos.put("pa", safe(tfPA));
        campos.put("fc", safe(tfFC));
        campos.put("fr", safe(tfFR));
        campos.put("temp", safe(tfTemp));
        campos.put("peso", safe(tfPeso));
        campos.put("altura", safe(tfAltura));
        campos.put("spo2", safe(tfSpO2));

        campos.put(
                "antecedentes",
                clinicalHistoryManager.montarTextoAntecedentesParaTela(
                        chkNegaAntecedentes,
                        taAntecedentes,
                        chkHipertensao,
                        chkDiabetes,
                        chkDislipidemia,
                        chkAsma,
                        chkCardiopatia,
                        chkDoencaRenal,
                        chkDoencaHepatica,
                        chkAutoimune,
                        chkCancer,
                        chkAnsiedadeDepressao
                )
        );

        campos.put(
                "antecedentes_lista",
                clinicalHistoryManager.montarListaAntecedentes(
                        chkNegaAntecedentes,
                        chkHipertensao,
                        chkDiabetes,
                        chkDislipidemia,
                        chkAsma,
                        chkCardiopatia,
                        chkDoencaRenal,
                        chkDoencaHepatica,
                        chkAutoimune,
                        chkCancer,
                        chkAnsiedadeDepressao
                )
        );

        campos.put("antecedentes_nega", chkNegaAntecedentes.isSelected() ? "1" : "0");

        campos.put("medicacoes", safe(taMedicacoes));
        campos.put("med_sem_uso", chkSemMedicacoes.isSelected() ? "1" : "0");

        campos.put("alergias", safe(taAlergias));
        campos.put("alergias_nega", chkNegaAlergias.isSelected() ? "1" : "0");

        campos.put("cirurgias", safe(taCirurgias));
        campos.put("cirurgias_nega", chkNegaCirurgias.isSelected() ? "1" : "0");

        campos.put("tabagismo", cbTabagismo.getValue());
        campos.put("alcool", cbAlcool.getValue());

        campos.put("sono", safe(taSono));
        campos.put("atividade_fisica", safe(taAtividadeFisica));
        campos.put("alimentacao", safe(taAlimentacao));

        campos.put("exame_geral", safe(taExameGeral));
        campos.put("exame_segmentar", safe(taExameSegmentar));

        return jsonMapper.montarJson(campos);
    }

    public void limparFormulario(
            TextArea taQueixa,
            TextArea taEvolucao,
            TextArea taObservacoes,
            TextField tfPA,
            TextField tfFC,
            TextField tfFR,
            TextField tfTemp,
            TextField tfPeso,
            TextField tfAltura,
            TextField tfSpO2,
            TextArea taAntecedentes,
            TextArea taMedicacoes,
            TextArea taAlergias,
            TextArea taCirurgias,
            CheckBox chkNegaAntecedentes,
            CheckBox chkHipertensao,
            CheckBox chkDiabetes,
            CheckBox chkDislipidemia,
            CheckBox chkAsma,
            CheckBox chkCardiopatia,
            CheckBox chkDoencaRenal,
            CheckBox chkDoencaHepatica,
            CheckBox chkAutoimune,
            CheckBox chkCancer,
            CheckBox chkAnsiedadeDepressao,
            CheckBox chkSemMedicacoes,
            CheckBox chkNegaAlergias,
            CheckBox chkNegaCirurgias,
            ComboBox<String> cbTabagismo,
            ComboBox<String> cbAlcool,
            TextArea taSono,
            TextArea taAtividadeFisica,
            TextArea taAlimentacao,
            TextArea taExameGeral,
            TextArea taExameSegmentar,
            AnamneseClinicalHistoryManager clinicalHistoryManager
    ) {
        taQueixa.clear();
        taEvolucao.clear();
        taObservacoes.clear();

        tfPA.clear();
        tfFC.clear();
        tfFR.clear();
        tfTemp.clear();
        tfPeso.clear();
        tfAltura.clear();
        tfSpO2.clear();

        taAntecedentes.clear();
        taMedicacoes.clear();
        taAlergias.clear();
        taCirurgias.clear();

        chkNegaAntecedentes.setSelected(false);

        clinicalHistoryManager.desmarcarAntecedentes(
                chkHipertensao,
                chkDiabetes,
                chkDislipidemia,
                chkAsma,
                chkCardiopatia,
                chkDoencaRenal,
                chkDoencaHepatica,
                chkAutoimune,
                chkCancer,
                chkAnsiedadeDepressao
        );

        clinicalHistoryManager.setAntecedentesEnabled(
                true,
                chkHipertensao,
                chkDiabetes,
                chkDislipidemia,
                chkAsma,
                chkCardiopatia,
                chkDoencaRenal,
                chkDoencaHepatica,
                chkAutoimune,
                chkCancer,
                chkAnsiedadeDepressao,
                taAntecedentes
        );

        chkSemMedicacoes.setSelected(false);
        taMedicacoes.setDisable(false);

        chkNegaAlergias.setSelected(false);
        taAlergias.setDisable(false);

        chkNegaCirurgias.setSelected(false);
        taCirurgias.setDisable(false);

        cbTabagismo.getSelectionModel().select("Não");
        cbAlcool.getSelectionModel().select("Não");

        taSono.clear();
        taAtividadeFisica.clear();
        taAlimentacao.clear();

        taExameGeral.clear();
        taExameSegmentar.clear();
    }

    private String extrair(AnamneseJsonMapper jsonMapper, String json, String campo) {
        return jsonMapper.extrairCampo(json == null ? "" : json, campo);
    }

    private String safe(TextInputControl c) {
        return c == null || c.getText() == null ? "" : c.getText().trim();
    }
}
