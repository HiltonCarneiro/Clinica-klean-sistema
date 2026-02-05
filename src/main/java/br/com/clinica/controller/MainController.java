package br.com.clinica.controller;

import br.com.clinica.auth.AuthGuard;
import br.com.clinica.auth.Permissao;
import br.com.clinica.auth.exceptions.AcessoNegadoException;
import br.com.clinica.auth.exceptions.NaoAutenticadoException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.util.ArrayDeque;
import java.util.Deque;

public class MainController {

    @FXML private Label lblUsuarioLogado;

    // HOME (tela inicial)
    @FXML private VBox homeBox;

    // CONTEÚDO (telas carregadas dentro do main)
    @FXML private AnchorPane contentPane;

    private String usuarioLogado;

    // Histórico para VOLTAR: guarda "estado anterior"
    private static class NavState {
        final boolean wasHomeVisible;
        final Parent previousView; // pode ser null se era home

        NavState(boolean wasHomeVisible, Parent previousView) {
            this.wasHomeVisible = wasHomeVisible;
            this.previousView = previousView;
        }
    }

    private final Deque<NavState> history = new ArrayDeque<>();

    @FXML
    private void initialize() {
        // começa na home
        mostrarHome();
    }

    /**
     * Chamado pelo LoginController depois de autenticar
     */
    public void setUsuarioLogado(String usuario) {
        this.usuarioLogado = usuario;
        if (lblUsuarioLogado != null) {
            lblUsuarioLogado.setText(usuario);
        }
    }

    // ================== BOTÕES FIXOS ==================

    @FXML
    private void onInicio() {
        history.clear();
        mostrarHome();
    }

    @FXML
    private void onVoltar() {
        if (history.isEmpty()) {
            // se não tem histórico, volta para home
            mostrarHome();
            return;
        }

        NavState prev = history.pop();

        if (prev.wasHomeVisible) {
            mostrarHome();
            return;
        }

        if (prev.previousView != null) {
            mostrarConteudo(prev.previousView);
        } else {
            mostrarHome();
        }
    }

    // ================== AÇÕES (MENU / HOME) ==================

    @FXML
    private void onPacientes() {
        abrirTelaNoConteudo("/view/paciente-view.fxml", Permissao.PACIENTE_VER);
    }

    @FXML
    private void onAgenda() {
        abrirTelaNoConteudo("/view/agenda-view.fxml", Permissao.AGENDA_VER);
    }

    @FXML
    private void onCaixa() {
        abrirTelaNoConteudo("/view/caixa-view.fxml", Permissao.FINANCEIRO_VER);
    }

    @FXML
    private void onMovimentoCaixa() {
        abrirTelaNoConteudo("/view/movimento-caixa-view.fxml", Permissao.FINANCEIRO_VER);
    }

    @FXML
    private void onEstoque() {
        abrirTelaNoConteudo("/view/estoque-view.fxml", Permissao.ESTOQUE_VER);
    }

    @FXML
    private void onRelatorios() {
        abrirTelaNoConteudo("/view/relatorios-view.fxml", Permissao.RELATORIOS_VER);
        // Se você ainda não tem essa tela, pode manter seu "mostrarInfo(...)"
    }

    @FXML
    private void onUsuarios() {
        abrirTelaNoConteudo("/view/usuarios-view.fxml", Permissao.USUARIO_GERENCIAR);
        // Se você ainda não tem essa tela, pode manter seu "mostrarInfo(...)"
    }

    // ================== NAVEGAÇÃO INTERNA ==================

    private void abrirTelaNoConteudo(String fxmlPath, Permissao permissao) {
        try {
            AuthGuard.exigirPermissao(permissao);

            // Salva estado atual no histórico (para VOLTAR)
            pushHistory();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            mostrarConteudo(view);

        } catch (NaoAutenticadoException | AcessoNegadoException e) {
            mostrarErro("Acesso negado", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela", e.getMessage());
        }
    }

    private void pushHistory() {
        boolean homeVisivel = homeBox != null && homeBox.isVisible();

        Parent atual = null;
        if (contentPane != null && !contentPane.getChildren().isEmpty()) {
            if (contentPane.getChildren().get(0) instanceof Parent p) {
                atual = p;
            }
        }

        history.push(new NavState(homeVisivel, atual));
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

    private void mostrarInfo(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}