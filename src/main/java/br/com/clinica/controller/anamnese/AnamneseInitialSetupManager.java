package br.com.clinica.controller.anamnese;

import br.com.clinica.dao.AnexoPacienteDAO;
import br.com.clinica.model.Anamnese;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

public class AnamneseInitialSetupManager {

    public void configurarTelaInicial(
            ComboBox<String> cbTipo,
            ComboBox<String> cbTabagismo,
            ComboBox<String> cbAlcool,
            TextField tfPA,
            TextField tfFC,
            TextField tfFR,
            TextField tfTemp,
            TextField tfPeso,
            TextField tfAltura,
            TextField tfSpO2,
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
            TextArea taCirurgias,
            TableView<Anamnese> tvHistorico,
            TableColumn<Anamnese, String> colData,
            TableColumn<Anamnese, String> colTipo,
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tvAnexos,
            TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colAnexoData,
            TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colAnexoArquivo,
            TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colAnexoDescricao,
            Button btnAbrirPdf,
            Button btnRemoverPdf,
            AnamneseFieldFormatter fieldFormatter,
            AnamneseClinicalHistoryManager clinicalHistoryManager,
            AnamneseHistoryManager historyManager,
            AnamneseAttachmentManager attachmentManager,
            Consumer<Anamnese> onSelecionarHistorico,
            Consumer<String> onTipoAlterado
    ) {
        configurarCombos(cbTipo, cbTabagismo, cbAlcool, onTipoAlterado);
        configurarMascarasSinaisVitais(fieldFormatter, tfPA, tfFC, tfFR, tfTemp, tfPeso, tfAltura, tfSpO2);
        configurarHistoricoClinico(
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
        configurarTabelaHistorico(historyManager, tvHistorico, colData, colTipo, onSelecionarHistorico);
        configurarTabelaAnexos(attachmentManager, tvAnexos, colAnexoData, colAnexoArquivo, colAnexoDescricao, btnAbrirPdf, btnRemoverPdf);
    }

    private void configurarCombos(
            ComboBox<String> cbTipo,
            ComboBox<String> cbTabagismo,
            ComboBox<String> cbAlcool,
            Consumer<String> onTipoAlterado
    ) {
        cbTipo.setItems(FXCollections.observableArrayList("ANAMNESE_INICIAL", "EVOLUCAO"));
        cbTipo.getSelectionModel().select("ANAMNESE_INICIAL");
        cbTipo.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (onTipoAlterado != null) {
                onTipoAlterado.accept(newValue);
            }
        });

        cbTabagismo.setItems(FXCollections.observableArrayList("Não", "Sim", "Ex-tabagista"));
        cbAlcool.setItems(FXCollections.observableArrayList("Não", "Social", "Frequente"));
        cbTabagismo.getSelectionModel().select("Não");
        cbAlcool.getSelectionModel().select("Não");
    }

    private void configurarMascarasSinaisVitais(
            AnamneseFieldFormatter fieldFormatter,
            TextField tfPA,
            TextField tfFC,
            TextField tfFR,
            TextField tfTemp,
            TextField tfPeso,
            TextField tfAltura,
            TextField tfSpO2
    ) {
        fieldFormatter.configurarMascarasSinaisVitais(
                tfPA,
                tfFC,
                tfFR,
                tfTemp,
                tfPeso,
                tfAltura,
                tfSpO2
        );
    }

    private void configurarHistoricoClinico(
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
        clinicalHistoryManager.configurarChecksHistorico(
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
    }

    private void configurarTabelaHistorico(
            AnamneseHistoryManager historyManager,
            TableView<Anamnese> tvHistorico,
            TableColumn<Anamnese, String> colData,
            TableColumn<Anamnese, String> colTipo,
            Consumer<Anamnese> onSelecionarHistorico
    ) {
        historyManager.configurarTabela(
                tvHistorico,
                colData,
                colTipo,
                onSelecionarHistorico
        );
    }

    private void configurarTabelaAnexos(
            AnamneseAttachmentManager attachmentManager,
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tvAnexos,
            TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colAnexoData,
            TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colAnexoArquivo,
            TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colAnexoDescricao,
            Button btnAbrirPdf,
            Button btnRemoverPdf
    ) {
        attachmentManager.configurarTabela(
                tvAnexos,
                colAnexoData,
                colAnexoArquivo,
                colAnexoDescricao,
                btnAbrirPdf,
                btnRemoverPdf
        );
    }
}
