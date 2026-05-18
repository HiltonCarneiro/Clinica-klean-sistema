package br.com.clinica.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationService {

    public void abrirTela(String fxml, Stage stage) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource(fxml));

            Parent root = loader.load();

            Scene scene = new Scene(root);

            stage.setScene(scene);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}