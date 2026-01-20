package br.com.clinica.controller;

import br.com.clinica.model.Usuario;
import br.com.clinica.service.LoginService;
import br.com.clinica.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private Label lblErro;

    private final LoginService loginService = new LoginService();

    @FXML
    private void onEntrar() {
        String login = txtUsuario.getText();
        String senha = txtSenha.getText();

        lblErro.setText("");

        Usuario usuario = loginService.autenticar(login, senha);

        if (usuario == null) {
            lblErro.setText("Usuário ou senha inválidos.");
            return;
        }

        // guarda na sessão
        Session.setUsuario(usuario);

        abrirTelaPrincipal();
    }

    private void abrirTelaPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Clínica Integração - Sistema");
            stage.setResizable(false);
            stage.show();

            // fecha login
            ((Stage) txtUsuario.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
