package br.com.clinica.controller.paciente;

import br.com.clinica.model.Paciente;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PacienteHistoryNavigator {

    public void abrirHistorico(Paciente paciente) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/paciente-historico-view.fxml")
            );

            Parent root = loader.load();
            Object controller = loader.getController();

            controller.getClass()
                    .getMethod("setPaciente", Paciente.class)
                    .invoke(controller, paciente);

            Stage stage = new Stage();
            stage.setTitle("Histórico do Paciente");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao abrir histórico do paciente.", e);
        }
    }
}