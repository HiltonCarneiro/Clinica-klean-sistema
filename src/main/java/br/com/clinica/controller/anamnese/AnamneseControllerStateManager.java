package br.com.clinica.controller.anamnese;

import br.com.clinica.dao.AnexoPacienteDAO;
import br.com.clinica.model.Anamnese;
import javafx.scene.control.TableView;

import java.util.function.Consumer;

public class AnamneseControllerStateManager {

    public void prepararTrocaDeAgendamento(
            TableView<Anamnese> tvHistorico,
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tvAnexos,
            AnamneseHistoryManager historyManager,
            AnamneseAttachmentManager attachmentManager,
            Runnable limparFormulario
    ) {
        if (limparFormulario != null) {
            limparFormulario.run();
        }

        if (historyManager != null && tvHistorico != null) {
            historyManager.limpar(tvHistorico);
        }

        if (attachmentManager != null && tvAnexos != null) {
            attachmentManager.limpar(tvAnexos);
        }
    }

    public void prepararNovaEvolucao(
            Runnable limparFormulario,
            Consumer<String> infoConsumer
    ) {
        if (limparFormulario != null) {
            limparFormulario.run();
        }

        if (infoConsumer != null) {
            infoConsumer.accept("Nova evolução pronta para preenchimento.");
        }
    }
}
