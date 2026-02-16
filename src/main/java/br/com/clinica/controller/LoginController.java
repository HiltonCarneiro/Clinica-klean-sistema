package br.com.clinica.controller;

import br.com.clinica.model.Usuario;
import br.com.clinica.service.LoginService;
import br.com.clinica.session.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsuario;

    @FXML private PasswordField txtSenha;
    @FXML private TextField txtSenhaVisivel;
    @FXML private Button btnToggleSenha;

    @FXML private Label lblErro;

    private final LoginService loginService = new LoginService();

    @FXML
    private void initialize() {
        // Mantém o texto sincronizado entre o PasswordField e o TextField
        if (txtSenha != null && txtSenhaVisivel != null) {
            txtSenhaVisivel.textProperty().bindBidirectional(txtSenha.textProperty());

            // começa escondido
            txtSenhaVisivel.setVisible(false);
            txtSenhaVisivel.setManaged(false);
        }
    }

    @FXML
    private void onToggleSenha(ActionEvent e) {
        if (txtSenha == null || txtSenhaVisivel == null) return;

        boolean mostrar = !txtSenhaVisivel.isVisible();

        txtSenhaVisivel.setVisible(mostrar);
        txtSenhaVisivel.setManaged(mostrar);

        txtSenha.setVisible(!mostrar);
        txtSenha.setManaged(!mostrar);

        // Mantém o cursor no fim e o foco no campo correto
        if (mostrar) {
            txtSenhaVisivel.requestFocus();
            txtSenhaVisivel.positionCaret(txtSenhaVisivel.getText().length());
            if (btnToggleSenha != null) btnToggleSenha.setText("🙈");
        } else {
            txtSenha.requestFocus();
            txtSenha.positionCaret(txtSenha.getText().length());
            if (btnToggleSenha != null) btnToggleSenha.setText("👁");
        }
    }

    @FXML
    private void onEntrar(ActionEvent e) {
        lblErro.setText("");

        String login = txtUsuario.getText() != null ? txtUsuario.getText().trim() : "";
        String senha = txtSenha.getText() != null ? txtSenha.getText() : "";

        Usuario usuario = loginService.autenticar(login, senha); // seu método
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

            // CSS
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

        } catch (Exception ex) {
            ex.printStackTrace();
            lblErro.setText("Erro ao abrir o sistema.");
        }
    }
}