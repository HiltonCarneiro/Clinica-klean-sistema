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

        // abre tela principal passando o usuário autenticado
        abrirTelaPrincipal(usuario);
    }

    private void abrirTelaPrincipal(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
            Scene scene = new Scene(loader.load());

            // pega o controller da tela principal
            MainController mainController = loader.getController();
            // aqui você escolhe o que mostrar: nome, cargo, login...
            // Exemplo: mostrar o nome ou o login
            String nomeOuLogin = usuario.getNome() != null && !usuario.getNome().isBlank()
                    ? usuario.getNome()
                    : usuario.getLogin();
            mainController.setUsuarioLogado(nomeOuLogin);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Clínica Integração - Sistema");
            stage.setResizable(false);
            stage.show();

            // fecha a tela de login
            ((Stage) txtUsuario.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}