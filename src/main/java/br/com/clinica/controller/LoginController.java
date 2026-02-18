package br.com.clinica.controller;

import br.com.clinica.dao.AuditoriaDAO;
import br.com.clinica.model.Usuario;
import br.com.clinica.service.LoginService;
import br.com.clinica.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsuario;

    // Campo padrão (oculto)
    @FXML private PasswordField txtSenha;

    // Campo alternativo (visível) - fica escondido por padrão no FXML
    @FXML private TextField txtSenhaVisivel;

    @FXML private Button btnToggleSenha;
    @FXML private Label lblErro;

    private boolean senhaVisivel = false;

    private final LoginService loginService = new LoginService();
    private final AuditoriaDAO auditoria = new AuditoriaDAO();

    @FXML
    public void initialize() {
        // garante estado inicial
        if (txtSenhaVisivel != null) {
            txtSenhaVisivel.setVisible(false);
            txtSenhaVisivel.setManaged(false);
        }
        senhaVisivel = false;
        if (lblErro != null) lblErro.setText("");
    }

    @FXML
    private void onToggleSenha() {
        senhaVisivel = !senhaVisivel;

        if (senhaVisivel) {
            // copiar senha para o campo visível
            txtSenhaVisivel.setText(txtSenha.getText());

            txtSenhaVisivel.setVisible(true);
            txtSenhaVisivel.setManaged(true);

            txtSenha.setVisible(false);
            txtSenha.setManaged(false);

            txtSenhaVisivel.requestFocus();
            txtSenhaVisivel.positionCaret(txtSenhaVisivel.getText().length());

            if (btnToggleSenha != null) btnToggleSenha.setText("🙈");
        } else {
            // copiar de volta para passwordfield
            txtSenha.setText(txtSenhaVisivel.getText());

            txtSenha.setVisible(true);
            txtSenha.setManaged(true);

            txtSenhaVisivel.setVisible(false);
            txtSenhaVisivel.setManaged(false);

            txtSenha.requestFocus();
            txtSenha.positionCaret(txtSenha.getText().length());

            if (btnToggleSenha != null) btnToggleSenha.setText("👁");
        }
    }

    @FXML
    private void onEntrar() {
        String login = txtUsuario != null ? txtUsuario.getText() : null;

        String senha;
        if (senhaVisivel && txtSenhaVisivel != null && txtSenhaVisivel.isVisible()) {
            senha = txtSenhaVisivel.getText();
        } else {
            senha = txtSenha != null ? txtSenha.getText() : null;
        }

        if (login == null || login.trim().isBlank() || senha == null || senha.isBlank()) {
            if (lblErro != null) lblErro.setText("Informe usuário e senha.");
            return;
        }

        Usuario u = loginService.autenticar(login.trim(), senha);

        if (u == null) {
            if (lblErro != null) lblErro.setText("Usuário ou senha inválidos.");
            // Nunca registrar senha
            auditoria.registrar("LOGIN_FALHA", "USUARIO", login.trim(), "falha de autenticação");
            return;
        }

        // sucesso
        Session.setUsuario(u);
        auditoria.registrar("LOGIN_OK", "USUARIO", String.valueOf(u.getId()), "login realizado");

        // abrir tela principal
        try {
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1100, 720);

            //css seguro:
            var cssUrl = getClass().getResource("/styles/app.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("CSS NÃO ENCONTRADO: /styles/app.css");
            }

            stage.setTitle("Clínica Klean - Saúde Integrativa | Sistema");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();

        } catch (Exception e) {
            if (lblErro != null) lblErro.setText("Erro ao abrir tela principal.");
            throw new RuntimeException("Erro ao abrir main-view.fxml", e);
        }
    }
}