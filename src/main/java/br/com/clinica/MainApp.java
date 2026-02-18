package br.com.clinica;

import br.com.clinica.service.BackupService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        //backup automático diário (1 por dia)
        try {
            BackupService.BackupResult r = BackupService.backupDiarioSeNecessario();
            System.out.println("[Backup] " + r.mensagem + (r.arquivo != null ? " -> " + r.arquivo : ""));
        } catch (Exception e) {
            // Não deixa o app morrer por causa de backup
            System.err.println("[Backup] Falha ao executar backup automático: " + e.getMessage());
        }

        //carrega tela de login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1000, 650);

        stage.setTitle("Clínica Klean - Saúde Integrativa | Sistema");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}