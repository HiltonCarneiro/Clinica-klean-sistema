package br.com.clinica.controller.agenda;

import br.com.clinica.controller.AnamneseController;
import br.com.clinica.model.Agendamento;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AgendaNavigationManager {

    public void abrirAtendimento(Agendamento agendamento) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/anamnese-view.fxml")
        );

        Parent root = loader.load();

        AnamneseController controller = loader.getController();
        controller.setAgendamento(agendamento);

        Stage stage = new Stage();
        stage.setTitle("Anamnese / Evolução");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }
}