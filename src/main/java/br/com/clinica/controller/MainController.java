package br.com.clinica.controller;

import br.com.clinica.auth.AuthGuard;
import br.com.clinica.auth.Permissao;
import br.com.clinica.auth.exceptions.AcessoNegadoException;
import br.com.clinica.auth.exceptions.NaoAutenticadoException;
import br.com.clinica.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class MainController {

    private static final String HOME = "__HOME__";

    @FXML private MenuBar menuBarTop;

    @FXML private ImageView imgLogoHome;

    @FXML private Menu menuCadastros;
    @FXML private Menu menuOperacoes;
    @FXML private Menu menuRelatorios;
    @FXML private Menu menuAdministracao;

    @FXML private MenuItem miPacientes;
    @FXML private MenuItem miAgenda;
    @FXML private MenuItem miCaixa;
    @FXML private MenuItem miEstoque;
    @FXML private MenuItem miRelatorios;
    @FXML private MenuItem miUsuarios;
    @FXML private MenuItem miAuditoria;

    @FXML private Button btnCardPacientes;
    @FXML private Button btnCardAgenda;
    @FXML private Button btnCardCaixa;
    @FXML private Button btnCardEstoque;
    @FXML private Button btnCardUsuarios;

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
        aplicarPermissoesHome();
        aplicarPermissoesMenu();

        //carregar a imagem
        var logoUrl = getClass().getResource("/images/logo-klean.png");
        if (logoUrl != null && imgLogoHome != null) {
            imgLogoHome.setImage(new Image(logoUrl.toExternalForm()));
        } else {
            System.out.println("Logo não encontrada ou ImageView null (imgLogoHome).");
        }

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

    private void aplicarPermissoesMenu() {
        if (menuBarTop == null) return;

        // remove menus que você não quer que apareçam nunca
        if (menuAdministracao != null) menuBarTop.getMenus().remove(menuAdministracao);

        // dentro de Operações, deixa SÓ Agenda
        removerItem(menuOperacoes, miCaixa);
        removerItem(menuOperacoes, miEstoque);

        // aplica permissão: se não tiver acesso, remove o menu inteiro
        if (!temPermissao(Permissao.PACIENTE_VER)) removerMenu(menuCadastros);
        if (!temPermissao(Permissao.AGENDA_VER)) removerMenu(menuOperacoes);
        if (!temPermissao(Permissao.RELATORIOS_VER)) removerMenu(menuRelatorios);

        // se sobrou menu vazio por algum motivo, remove também
        removerMenuSeVazio(menuCadastros);
        removerMenuSeVazio(menuOperacoes);
        removerMenuSeVazio(menuRelatorios);
    }

    private boolean temPermissao(Permissao permissao) {
        try {
            AuthGuard.exigirPermissao(permissao);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void removerItem(Menu menu, MenuItem item) {
        if (menu != null && item != null) {
            menu.getItems().remove(item);
        }
    }

    private void removerMenu(Menu menu) {
        if (menuBarTop != null && menu != null) {
            menuBarTop.getMenus().remove(menu);
        }
    }

    private void removerMenuSeVazio(Menu menu) {
        if (menu == null) return;
        if (menu.getItems() == null || menu.getItems().isEmpty()) {
            removerMenu(menu);
        }
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

        if (HOME.equals(previous)) {
            mostrarHome(); // isso já seta currentView=HOME e atualiza botões
        } else {
            loadView(previous, false);
        }
    }

    @FXML
    private void onForward() {
        if (forwardStack.isEmpty()) return;

        String next = forwardStack.pop();
        if (currentView != null) backStack.push(currentView);

        if (HOME.equals(next)) {
            mostrarHome();
        } else {
            loadView(next, false);
        }
    }

    // ================== AÇÕES (MENU / HOME) ==================

    @FXML private void onPacientes() { abrirTelaNoConteudo("/view/paciente-view.fxml", Permissao.PACIENTE_VER); }
    @FXML private void onAgenda() { abrirTelaNoConteudo("/view/agenda-view.fxml", Permissao.AGENDA_VER); }
    @FXML private void onCaixa() { abrirTelaNoConteudo("/view/caixa-view.fxml", Permissao.FINANCEIRO_VER); }
    @FXML private void onEstoque() { abrirTelaNoConteudo("/view/estoque-view.fxml", Permissao.ESTOQUE_VER); }
    @FXML private void onRelatorios() { abrirTelaNoConteudo("/view/relatorios-view.fxml", Permissao.RELATORIOS_VER); }
    @FXML private void onUsuarios() { abrirTelaNoConteudo("/view/usuarios-view.fxml", Permissao.USUARIO_GERENCIAR); }
    @FXML private void onAuditoria() { abrirTelaNoConteudo("/view/auditoria-view.fxml", Permissao.AUDITORIA_VER); }

    // NAVEGAÇÃO INTERNA
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
        currentView = HOME;
        atualizarBotoesNavegacao();
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

    private void aplicarPermissoesHome() {
        aplicarPermissao(btnCardPacientes, Permissao.PACIENTE_VER);
        aplicarPermissao(btnCardAgenda, Permissao.AGENDA_VER);
        aplicarPermissao(btnCardCaixa, Permissao.FINANCEIRO_VER);
        aplicarPermissao(btnCardEstoque, Permissao.ESTOQUE_VER);
        aplicarPermissao(btnCardUsuarios, Permissao.USUARIO_GERENCIAR);
    }

    private void aplicarPermissao(Button btn, Permissao permissao) {
        if (btn == null) return;

        boolean pode;

        try {
            AuthGuard.exigirPermissao(permissao);
            pode = true;
        } catch (Exception e) {
            pode = false;
        }

        btn.setVisible(pode);
        btn.setManaged(pode);
    }

    // ================== ✅ SAIR (VOLTA AO LOGIN) ==================
    @FXML
    private void onSair() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Sair do sistema");
        confirm.setHeaderText("Encerrar sessão");
        confirm.setContentText("Deseja realmente sair e voltar para a tela de login?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // limpa sessão
        Session.limpar();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentPane.getScene().getWindow();

            // ✅ ALTERAÇÃO MÍNIMA: recria a Scene e reaplica o MESMO CSS do app
            Scene scene = new Scene(root);
            var cssUrl = getClass().getResource("/styles/app.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro", "Não foi possível voltar para a tela de login.");
        }
    }

    // ALERTAS
    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}