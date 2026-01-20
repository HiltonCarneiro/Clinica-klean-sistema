package br.com.clinica;

import br.com.clinica.database.DatabaseConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseConfig.initializeDatabase();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login-view.fxml"));
        Scene scene = new Scene(loader.load(), 400, 250);

        stage.setTitle("Clínica Integração - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}