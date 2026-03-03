package br.com.clinica.controller;

import br.com.clinica.dao.AuditoriaDAO;
import br.com.clinica.model.Usuario;
import br.com.clinica.service.LoginService;
import br.com.clinica.session.Session;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoginController {
    //teste
    @FXML private ImageView imgLogoLogin;

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
        var logoUrl = getClass().getResource("/images/logo-klean.png");
        if (logoUrl != null && imgLogoLogin != null) {
            imgLogoLogin.setImage(new Image(logoUrl.toExternalForm()));
        } else {
            System.out.println("Logo não encontrada: /images/logo-klean.png");
        }

        // Pré-aquecer conexão (evita primeira autenticação muito lenta)
        new Thread(() -> {
            try (java.sql.Connection c = br.com.clinica.database.DatabaseConfig.getConnection()) {
                // abrir/fechar já aquece pool/SSL
            } catch (Exception ignored) {}
        }, "db-warmup").start();
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

        final String loginFinal = login.trim();
        final String senhaFinal = senha;

        if (lblErro != null) lblErro.setText("");

        // feedback/anti-double-click
        if (txtUsuario != null) txtUsuario.setDisable(true);
        if (txtSenha != null) txtSenha.setDisable(true);
        if (txtSenhaVisivel != null) txtSenhaVisivel.setDisable(true);
        if (btnToggleSenha != null) btnToggleSenha.setDisable(true);

        Task<Usuario> task = new Task<>() {
            @Override
            protected Usuario call() {
                return loginService.autenticar(loginFinal, senhaFinal);
            }
        };

        task.setOnSucceeded(evt -> {
            try {
                Usuario u = task.getValue();

                if (u == null) {
                    if (lblErro != null) lblErro.setText("Usuário ou senha inválidos.");

                    // Auditoria em background (não travar a UI)
                    new Thread(() ->
                            auditoria.registrar("LOGIN_FALHA", "USUARIO", loginFinal, "falha de autenticação"),
                            "auditoria-login-falha"
                    ).start();
                    return;
                }

                // sucesso
                Session.setUsuario(u);

                // Auditoria em background (não travar a troca de tela)
                new Thread(() ->
                        auditoria.registrar("LOGIN_OK", "USUARIO", String.valueOf(u.getId()), "login realizado"),
                        "auditoria-login-ok"
                ).start();

                // abrir tela principal
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
            } finally {
                if (txtUsuario != null) txtUsuario.setDisable(false);
                if (txtSenha != null) txtSenha.setDisable(false);
                if (txtSenhaVisivel != null) txtSenhaVisivel.setDisable(false);
                if (btnToggleSenha != null) btnToggleSenha.setDisable(false);
            }
        });

        task.setOnFailed(evt -> {
            try {
                Throwable ex = task.getException();
                if (lblErro != null) lblErro.setText("Erro ao autenticar. Verifique conexão com o banco.");
                if (ex != null) ex.printStackTrace();
            } finally {
                if (txtUsuario != null) txtUsuario.setDisable(false);
                if (txtSenha != null) txtSenha.setDisable(false);
                if (txtSenhaVisivel != null) txtSenhaVisivel.setDisable(false);
                if (btnToggleSenha != null) btnToggleSenha.setDisable(false);
            }
        });

        Thread th = new Thread(task, "login-auth-task");
        th.setDaemon(true);
        th.start();
    }
}