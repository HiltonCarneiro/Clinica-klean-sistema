package br.com.clinica.controller.anamnese;

import br.com.clinica.dao.AnexoPacienteDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Anamnese;
import br.com.clinica.model.Paciente;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

public class AnamneseAttachmentActionManager {

    public void anexarPdf(
            AnamneseAttachmentManager attachmentManager,
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tvAnexos,
            TextField tfDescricaoAnexo,
            Button btnAnexarPdf,
            Paciente paciente,
            Agendamento agendamento,
            Anamnese selecionada,
            Anamnese anamneseInicialAtual,
            Consumer<String> infoConsumer
    ) {
        attachmentManager.anexarPdf(
                tvAnexos,
                tfDescricaoAnexo,
                btnAnexarPdf,
                paciente,
                agendamento,
                selecionada,
                anamneseInicialAtual,
                infoConsumer
        );
    }

    public void abrirPdf(
            AnamneseAttachmentManager attachmentManager,
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tvAnexos,
            Consumer<String> infoConsumer
    ) {
        attachmentManager.abrirPdf(
                tvAnexos,
                infoConsumer
        );
    }

    public void removerPdf(
            AnamneseAttachmentManager attachmentManager,
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tvAnexos,
            Paciente paciente,
            Consumer<String> infoConsumer
    ) {
        attachmentManager.removerPdf(
                tvAnexos,
                paciente,
                infoConsumer
        );
    }
}
