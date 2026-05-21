package br.com.clinica.controller.anamnese;

import br.com.clinica.model.Anamnese;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AnamneseSelectionManager {

    public void carregarSelecionada(
            Anamnese anamnese,
            ComboBox<String> cbTipo,
            AnamneseJsonMapper jsonMapper,
            AnamneseFormManager formManager,
            AnamneseClinicalHistoryManager clinicalHistoryManager,
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
            ComboBox<String> cbAlcool
    ) {
        if (anamnese == null) {
            return;
        }

        cbTipo.getSelectionModel().select(anamnese.getTipo());
        String json = anamnese.getDadosJson() == null ? "" : anamnese.getDadosJson();

        formManager.preencherCamposBasicos(
                json,
                jsonMapper,
                taQueixa,
                taEvolucao,
                taObservacoes,
                anamnese.getObservacoes(),
                tfPA,
                tfFC,
                tfFR,
                tfTemp,
                tfPeso,
                tfAltura,
                tfSpO2,
                taAntecedentes,
                taMedicacoes,
                taAlergias,
                taCirurgias,
                taSono,
                taAtividadeFisica,
                taAlimentacao,
                taExameGeral,
                taExameSegmentar
        );

        carregarHistoricoClinico(
                json,
                jsonMapper,
                clinicalHistoryManager,
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
                chkAnsiedadeDepressao,
                taAntecedentes,
                chkSemMedicacoes,
                taMedicacoes,
                chkNegaAlergias,
                taAlergias,
                chkNegaCirurgias,
                taCirurgias
        );

        selecionarComboSePreenchido(cbTabagismo, extrairCampoJson(jsonMapper, json, "tabagismo"));
        selecionarComboSePreenchido(cbAlcool, extrairCampoJson(jsonMapper, json, "alcool"));
    }

    private void carregarHistoricoClinico(
            String json,
            AnamneseJsonMapper jsonMapper,
            AnamneseClinicalHistoryManager clinicalHistoryManager,
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
            TextArea taAntecedentes,
            CheckBox chkSemMedicacoes,
            TextArea taMedicacoes,
            CheckBox chkNegaAlergias,
            TextArea taAlergias,
            CheckBox chkNegaCirurgias,
            TextArea taCirurgias
    ) {
        boolean negaAntecedentes = isTrue(jsonMapper, extrairCampoJson(jsonMapper, json, "antecedentes_nega"));
        chkNegaAntecedentes.setSelected(negaAntecedentes);

        String listaAntecedentes = extrairCampoJson(jsonMapper, json, "antecedentes_lista");
        clinicalHistoryManager.aplicarAntecedentesPorLista(
                listaAntecedentes,
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
                !negaAntecedentes,
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

        boolean semMedicacoes = isTrue(jsonMapper, extrairCampoJson(jsonMapper, json, "med_sem_uso"));
        chkSemMedicacoes.setSelected(semMedicacoes);
        taMedicacoes.setDisable(semMedicacoes);

        boolean negaAlergias = isTrue(jsonMapper, extrairCampoJson(jsonMapper, json, "alergias_nega"));
        chkNegaAlergias.setSelected(negaAlergias);
        taAlergias.setDisable(negaAlergias);

        boolean negaCirurgias = isTrue(jsonMapper, extrairCampoJson(jsonMapper, json, "cirurgias_nega"));
        chkNegaCirurgias.setSelected(negaCirurgias);
        taCirurgias.setDisable(negaCirurgias);
    }

    private void selecionarComboSePreenchido(ComboBox<String> comboBox, String valor) {
        if (valor != null && !valor.isBlank()) {
            comboBox.getSelectionModel().select(valor);
        }
    }

    private String extrairCampoJson(AnamneseJsonMapper jsonMapper, String json, String campo) {
        return jsonMapper.extrairCampo(json == null ? "" : json, campo);
    }

    private boolean isTrue(AnamneseJsonMapper jsonMapper, String value) {
        return jsonMapper.isTrue(value);
    }
}
