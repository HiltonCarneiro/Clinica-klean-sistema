package br.com.clinica.controller;

import br.com.clinica.auth.AuthGuard;
import br.com.clinica.auth.Permissao;
import br.com.clinica.auth.exceptions.AcessoNegadoException;
import br.com.clinica.auth.exceptions.NaoAutenticadoException;
import br.com.clinica.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class MainController {

    // conecta controller ao main-view.fxml
    @FXML private VBox homeBox;
    @FXML private AnchorPane contentPane;
    @FXML private Label lblUsuarioLogado;

    private String usuarioLogado;

    @FXML private Button btnBack;
    @FXML private Button btnForward;

    private final Deque<String> backStack = new ArrayDeque<>();
    private final Deque<String> forwardStack = new ArrayDeque<>();
    private String currentView = null;

    @FXML
    private void initialize() {
        mostrarHome();
        atualizarUsuarioLogado();
    }

    private void atualizarUsuarioLogado() {
        if (Session.getUsuario() != null) {
            lblUsuarioLogado.setText(Session.getUsuario().getPessoaNome());
        } else {
            lblUsuarioLogado.setText("-");
        }
        atualizarBotoesNavegacao();
    }

    private void atualizarBotoesNavegacao() {
        if (btnBack != null) btnBack.setDisable(backStack.isEmpty());
        if (btnForward != null) btnForward.setDisable(forwardStack.isEmpty());
    }

    /** Chamado pelo LoginController depois de autenticar */
    public void setUsuarioLogado(String usuario) {
        this.usuarioLogado = usuario;
        if (lblUsuarioLogado != null) {
            lblUsuarioLogado.setText(usuario);
        }
    }

    // ================== BOTÕES FIXOS ==================

    @FXML
    private void onInicio() {
        backStack.clear();
        forwardStack.clear();
        currentView = null;
        mostrarHome();
        atualizarBotoesNavegacao();
    }

    @FXML
    private void onBack() {
        if (backStack.isEmpty()) return;

        String previous = backStack.pop();
        if (currentView != null) forwardStack.push(currentView);

        loadView(previous, false);
        atualizarBotoesNavegacao();
    }

    @FXML
    private void onForward() {
        if (forwardStack.isEmpty()) return;

        String next = forwardStack.pop();
        if (currentView != null) backStack.push(currentView);

        loadView(next, false);
        atualizarBotoesNavegacao();
    }

    // ================== AÇÕES (MENU / HOME) ==================

    @FXML private void onPacientes() { abrirTelaNoConteudo("/view/paciente-view.fxml", Permissao.PACIENTE_VER); }
    @FXML private void onAgenda() { abrirTelaNoConteudo("/view/agenda-view.fxml", Permissao.AGENDA_VER); }
    @FXML private void onCaixa() { abrirTelaNoConteudo("/view/caixa-view.fxml", Permissao.FINANCEIRO_VER); }
    @FXML private void onEstoque() { abrirTelaNoConteudo("/view/estoque-view.fxml", Permissao.ESTOQUE_VER); }
    @FXML private void onRelatorios() { abrirTelaNoConteudo("/view/relatorios-view.fxml", Permissao.RELATORIOS_VER); }
    @FXML private void onUsuarios() { abrirTelaNoConteudo("/view/usuarios-view.fxml", Permissao.USUARIO_GERENCIAR); }
    @FXML
    private void onAuditoria() {abrirTelaNoConteudo("/view/auditoria-view.fxml", Permissao.AUDITORIA_VER);}
    // ================== NAVEGAÇÃO INTERNA ==================

    private void abrirTelaNoConteudo(String fxmlPath, Permissao permissao) {
        try {
            AuthGuard.exigirPermissao(permissao);
            loadView(fxmlPath, true);
        } catch (NaoAutenticadoException | AcessoNegadoException e) {
            mostrarErro("Acesso negado", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela", e.getMessage());
        }
    }

    private void loadView(String fxmlPath, boolean pushHistory) {
        try {
            if (pushHistory) {
                if (currentView != null) backStack.push(currentView);
                forwardStack.clear();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            currentView = fxmlPath;
            mostrarConteudo(view);
            atualizarBotoesNavegacao();

        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarErro("Erro ao carregar FXML", ex.getMessage());
        }
    }

    private void mostrarHome() {
        if (homeBox != null) {
            homeBox.setVisible(true);
            homeBox.setManaged(true);
        }
        if (contentPane != null) {
            contentPane.getChildren().clear();
            contentPane.setVisible(false);
            contentPane.setManaged(false);
        }
    }

    private void mostrarConteudo(Parent view) {
        if (homeBox != null) {
            homeBox.setVisible(false);
            homeBox.setManaged(false);
        }

        contentPane.getChildren().setAll(view);
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);

        contentPane.setVisible(true);
        contentPane.setManaged(true);
    }

    // ================== ALERTAS ==================

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}