package br.com.clinica.controller;

import br.com.clinica.model.Usuario;
import br.com.clinica.service.LoginService;
import br.com.clinica.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblErro;

    private final LoginService loginService = new LoginService();

    @FXML
    private void onEntrar(ActionEvent e) {
        lblErro.setText("");

        String login = txtUsuario.getText();
        String senha = txtSenha.getText();

        Usuario usuario = loginService.autenticar(login, senha); // ajuste pro seu método real
        if (usuario == null) {
            lblErro.setText("Usuário ou senha inválidos.");
            return;
        }

        Session.setUsuario(usuario);
        abrirTelaPrincipalNoMesmoStage(usuario);
    }



    private void abrirTelaPrincipalNoMesmoStage(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 700);

            //CSS
            scene.getStylesheets().add(getClass().getResource("/style/app.css").toExternalForm());

            MainController mainController = loader.getController();

            String cargo = usuario.getNome();
            String pessoa = usuario.getPessoaNome();
            String login = usuario.getLogin();

            String texto = (cargo != null ? cargo : "SEM CARGO")
                    + " - "
                    + (pessoa != null && !pessoa.isBlank() ? pessoa : "SEM NOME")
                    + " (" + login + ")";

            mainController.setUsuarioLogado(texto);

            Stage stageAtual = (Stage) txtUsuario.getScene().getWindow();
            stageAtual.setTitle("Clínica Klean - Saúde Integrativa | Sistema");
            stageAtual.setResizable(true);
            stageAtual.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            lblErro.setText("Erro ao abrir o sistema.");
        }
    }
}