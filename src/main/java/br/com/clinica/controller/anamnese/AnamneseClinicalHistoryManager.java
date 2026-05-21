package br.com.clinica.controller.anamnese;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;

public class AnamneseClinicalHistoryManager {

    public void configurarChecksHistorico(
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
        if (chkNegaAntecedentes != null) {
            chkNegaAntecedentes.selectedProperty().addListener((obs, oldValue, negou) -> {
                setAntecedentesEnabled(
                        !negou,
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

                if (negou) {
                    desmarcarAntecedentes(
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

                    if (taAntecedentes != null) {
                        taAntecedentes.clear();
                    }
                }
            });
        }

        if (chkSemMedicacoes != null) {
            chkSemMedicacoes.selectedProperty().addListener((obs, oldValue, semUso) -> {
                if (taMedicacoes != null) {
                    taMedicacoes.setDisable(semUso);

                    if (semUso) {
                        taMedicacoes.clear();
                    }
                }
            });
        }

        if (chkNegaAlergias != null) {
            chkNegaAlergias.selectedProperty().addListener((obs, oldValue, nega) -> {
                if (taAlergias != null) {
                    taAlergias.setDisable(nega);

                    if (nega) {
                        taAlergias.clear();
                    }
                }
            });
        }

        if (chkNegaCirurgias != null) {
            chkNegaCirurgias.selectedProperty().addListener((obs, oldValue, nega) -> {
                if (taCirurgias != null) {
                    taCirurgias.setDisable(nega);

                    if (nega) {
                        taCirurgias.clear();
                    }
                }
            });
        }
    }

    public void setAntecedentesEnabled(
            boolean enabled,
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
            TextArea taAntecedentes
    ) {
        setDisable(chkHipertensao, !enabled);
        setDisable(chkDiabetes, !enabled);
        setDisable(chkDislipidemia, !enabled);
        setDisable(chkAsma, !enabled);
        setDisable(chkCardiopatia, !enabled);
        setDisable(chkDoencaRenal, !enabled);
        setDisable(chkDoencaHepatica, !enabled);
        setDisable(chkAutoimune, !enabled);
        setDisable(chkCancer, !enabled);
        setDisable(chkAnsiedadeDepressao, !enabled);

        if (taAntecedentes != null) {
            taAntecedentes.setDisable(!enabled);
        }
    }

    public void desmarcarAntecedentes(
            CheckBox chkHipertensao,
            CheckBox chkDiabetes,
            CheckBox chkDislipidemia,
            CheckBox chkAsma,
            CheckBox chkCardiopatia,
            CheckBox chkDoencaRenal,
            CheckBox chkDoencaHepatica,
            CheckBox chkAutoimune,
            CheckBox chkCancer,
            CheckBox chkAnsiedadeDepressao
    ) {
        setSelected(chkHipertensao, false);
        setSelected(chkDiabetes, false);
        setSelected(chkDislipidemia, false);
        setSelected(chkAsma, false);
        setSelected(chkCardiopatia, false);
        setSelected(chkDoencaRenal, false);
        setSelected(chkDoencaHepatica, false);
        setSelected(chkAutoimune, false);
        setSelected(chkCancer, false);
        setSelected(chkAnsiedadeDepressao, false);
    }

    public void aplicarAntecedentesPorLista(
            String lista,
            CheckBox chkHipertensao,
            CheckBox chkDiabetes,
            CheckBox chkDislipidemia,
            CheckBox chkAsma,
            CheckBox chkCardiopatia,
            CheckBox chkDoencaRenal,
            CheckBox chkDoencaHepatica,
            CheckBox chkAutoimune,
            CheckBox chkCancer,
            CheckBox chkAnsiedadeDepressao
    ) {
        desmarcarAntecedentes(
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

        if (lista == null || lista.isBlank()) {
            return;
        }

        String[] parts = lista.split(";");

        for (String raw : parts) {
            String item = raw.trim().toLowerCase();

            if (item.isBlank()) {
                continue;
            }

            if (item.contains("hipert")) {
                setSelected(chkHipertensao, true);
                continue;
            }

            if (item.contains("diabet")) {
                setSelected(chkDiabetes, true);
                continue;
            }

            if (item.contains("dislip")) {
                setSelected(chkDislipidemia, true);
                continue;
            }

            if (item.contains("asma")) {
                setSelected(chkAsma, true);
                continue;
            }

            if (item.contains("cardio")) {
                setSelected(chkCardiopatia, true);
                continue;
            }

            if (item.contains("renal")) {
                setSelected(chkDoencaRenal, true);
                continue;
            }

            if (item.contains("hepát") || item.contains("hepat")) {
                setSelected(chkDoencaHepatica, true);
                continue;
            }

            if (item.contains("autoim")) {
                setSelected(chkAutoimune, true);
                continue;
            }

            if (item.contains("cânc") || item.contains("canc")) {
                setSelected(chkCancer, true);
                continue;
            }

            if (item.contains("ansied") || item.contains("depress")) {
                setSelected(chkAnsiedadeDepressao, true);
            }
        }
    }

    public String montarListaAntecedentes(
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
            CheckBox chkAnsiedadeDepressao
    ) {
        if (isSelected(chkNegaAntecedentes)) {
            return "";
        }

        List<String> itens = new ArrayList<>();

        if (isSelected(chkHipertensao)) {
            itens.add("Hipertensão");
        }

        if (isSelected(chkDiabetes)) {
            itens.add("Diabetes");
        }

        if (isSelected(chkDislipidemia)) {
            itens.add("Dislipidemia");
        }

        if (isSelected(chkAsma)) {
            itens.add("Asma");
        }

        if (isSelected(chkCardiopatia)) {
            itens.add("Cardiopatia");
        }

        if (isSelected(chkDoencaRenal)) {
            itens.add("Doença renal");
        }

        if (isSelected(chkDoencaHepatica)) {
            itens.add("Doença hepática");
        }

        if (isSelected(chkAutoimune)) {
            itens.add("Autoimune");
        }

        if (isSelected(chkCancer)) {
            itens.add("Câncer");
        }

        if (isSelected(chkAnsiedadeDepressao)) {
            itens.add("Ansiedade/Depressão");
        }

        return String.join("; ", itens);
    }

    public String montarTextoAntecedentesParaTela(
            CheckBox chkNegaAntecedentes,
            TextArea taAntecedentes,
            CheckBox chkHipertensao,
            CheckBox chkDiabetes,
            CheckBox chkDislipidemia,
            CheckBox chkAsma,
            CheckBox chkCardiopatia,
            CheckBox chkDoencaRenal,
            CheckBox chkDoencaHepatica,
            CheckBox chkAutoimune,
            CheckBox chkCancer,
            CheckBox chkAnsiedadeDepressao
    ) {
        if (isSelected(chkNegaAntecedentes)) {
            return "Nega antecedentes.";
        }

        String lista = montarListaAntecedentes(
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
        );

        String outros = taAntecedentes == null || taAntecedentes.getText() == null
                ? ""
                : taAntecedentes.getText().trim();

        if (lista.isBlank() && outros.isBlank()) {
            return "";
        }

        if (!lista.isBlank() && outros.isBlank()) {
            return lista;
        }

        if (lista.isBlank()) {
            return outros;
        }

        return lista + "\nOutros: " + outros;
    }

    private void setDisable(CheckBox checkBox, boolean disabled) {
        if (checkBox != null) {
            checkBox.setDisable(disabled);
        }
    }

    private void setSelected(CheckBox checkBox, boolean selected) {
        if (checkBox != null) {
            checkBox.setSelected(selected);
        }
    }

    private boolean isSelected(CheckBox checkBox) {
        return checkBox != null && checkBox.isSelected();
    }
}
