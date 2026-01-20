package br.com.clinica;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Paciente;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseConfig.initializeDatabase();

        // TESTE CRUD
        PacienteDAO dao = new PacienteDAO();

        Paciente novo = new Paciente();
        novo.setNome("João Teste");
        novo.setCpf("12345678900");
        novo.setTelefone("99999-0000");

        dao.salvar(novo);

        System.out.println("=== PACIENTES NO BANCO ===");
        dao.listarTodos().forEach(System.out::println);

        // Interface básica só para manter a janela aberta
        Label label = new Label("Sistema da Clínica - JavaFX funcionando!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 600, 400);

        stage.setTitle("Clínica Integração");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}