package br.com.clinica.controller;

import br.com.clinica.auth.Permissao;
import br.com.clinica.auth.exceptions.AcessoNegadoException;
import br.com.clinica.auth.exceptions.NaoAutenticadoException;
import br.com.clinica.navigation.NavigationService;
import br.com.clinica.service.DialogService;
import br.com.clinica.service.MainDashboardService;
import br.com.clinica.service.MainDashboardService.DashboardResumo;
import br.com.clinica.service.MainDashboardService.IndicadorResumo;
import br.com.clinica.service.PermissionService;
import br.com.clinica.service.SessionViewService;
import br.com.clinica.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainController {

    private static final String VIEW_LOGIN = "/view/login-view.fxml";
    private static final String VIEW_PACIENTES = "/view/paciente-view.fxml";
    private static final String VIEW_AGENDA = "/view/agenda-view.fxml";
    private static final String VIEW_CAIXA = "/view/caixa-view.fxml";
    private static final String VIEW_ESTOQUE = "/view/estoque-view.fxml";
    private static final String VIEW_RELATORIOS = "/view/relatorios-view.fxml";
    private static final String VIEW_USUARIOS = "/view/usuarios-view.fxml";
    private static final String VIEW_AUDITORIA = "/view/auditoria-view.fxml";
    private static final String VIEW_AVALIACAO_FISICA = "/view/avaliacao-fisica-view.fxml";

    private static final String CSS_APP = "/styles/app.css";
    private static final String LOGO_HOME = "/images/logo-klean.png";

    @FXML private MenuBar menuBarTop;
    @FXML private ImageView imgLogoHome;
    @FXML private ImageView imgLogoSidebar;

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
    @FXML private MenuItem miAvaliacaoFisica;

    @FXML private Button btnCardPacientes;
    @FXML private Button btnCardAgenda;
    @FXML private Button btnCardCaixa;
    @FXML private Button btnCardEstoque;
    @FXML private Button btnCardUsuarios;
    @FXML private Button btnCardAvaliacaoFisica;

    @FXML private Button btnDashboardCaixa;
    @FXML private Button btnDashboardEstoque;
    @FXML private Button btnDashboardRelatorios;
    @FXML private Button btnDashboardUsuarios;
    @FXML private Button btnDashboardAvaliacaoFisica;

    @FXML private VBox cardFinanceiroDashboard;
    @FXML private VBox cardEstoqueDashboard;
    @FXML private VBox adminDashboardBox;

    @FXML private VBox homeBox;
    @FXML private AnchorPane contentPane;

    @FXML private Label lblUsuarioLogado;
    @FXML private Label lblPerfilUsuario;

    @FXML private Label lblAgendaHojeValor;
    @FXML private Label lblAgendaHojeSub;
    @FXML private Label lblPacientesAtivosValor;
    @FXML private Label lblPacientesAtivosSub;
    @FXML private Label lblFinanceiroHojeValor;
    @FXML private Label lblFinanceiroHojeSub;
    @FXML private Label lblEstoqueCriticoValor;
    @FXML private Label lblEstoqueCriticoSub;

    @FXML private Label lblResumoAgenda;
    @FXML private Label lblResumoPacientes;
    @FXML private Label lblResumoEstoque;
    @FXML private Label lblResumoFinanceiro;
    @FXML private Label lblAuditoriaRecente;

    @FXML private Button btnBack;
    @FXML private Button btnForward;

    private final MainDashboardService dashboardService = new MainDashboardService();
    private final NavigationService navigationService = new NavigationService(MainController.class);
    private final DialogService dialogService = new DialogService();
    private final PermissionService permissionService = new PermissionService();
    private final SessionViewService sessionViewService = new SessionViewService();

    private String usuarioLogado;

    @FXML
    private void initialize() {
        atualizarUsuarioLogado();
        carregarLogo();
        aplicarPermissoesHome();
        aplicarPermissoesMenu();
        mostrarHome();
        carregarDashboard();
        atualizarBotoesNavegacao();
    }

    public void setUsuarioLogado(String usuario) {
        this.usuarioLogado = usuario;
        setText(lblUsuarioLogado, usuario);
    }

    @FXML
    private void onInicio() {
        navigationService.clearHistory();
        mostrarHome();
        carregarDashboard();
    }

    @FXML
    private void onBack() {
        String destino = navigationService.back();

        if (destino == null) {
            atualizarBotoesNavegacao();
            return;
        }

        abrirDestinoHistorico(destino);
    }

    @FXML
    private void onForward() {
        String destino = navigationService.forward();

        if (destino == null) {
            atualizarBotoesNavegacao();
            return;
        }

        abrirDestinoHistorico(destino);
    }

    @FXML private void onPacientes() { abrirTelaNoConteudo(VIEW_PACIENTES, Permissao.PACIENTE_VER); }
    @FXML private void onAgenda() { abrirTelaNoConteudo(VIEW_AGENDA, Permissao.AGENDA_VER); }
    @FXML private void onCaixa() { abrirTelaNoConteudo(VIEW_CAIXA, Permissao.FINANCEIRO_VER); }
    @FXML private void onEstoque() { abrirTelaNoConteudo(VIEW_ESTOQUE, Permissao.ESTOQUE_VER); }
    @FXML private void onRelatorios() { abrirTelaNoConteudo(VIEW_RELATORIOS, Permissao.RELATORIOS_VER); }
    @FXML private void onUsuarios() { abrirTelaNoConteudo(VIEW_USUARIOS, Permissao.USUARIO_GERENCIAR); }
    @FXML private void onAuditoria() { abrirTelaNoConteudo(VIEW_AUDITORIA, Permissao.AUDITORIA_VER); }
    @FXML private void onAvaliacaoFisica() { abrirTelaNoConteudo(VIEW_AVALIACAO_FISICA, Permissao.AVALIACAO_FISICA_VER); }

    @FXML
    private void onSair() {
        boolean confirmou = dialogService.confirmar(
                "Sair do sistema",
                "Encerrar sessão",
                "Deseja realmente sair e voltar para a tela de login?"
        );

        if (!confirmou) return;

        Session.limpar();
        voltarParaLogin();
    }

    private void abrirTelaNoConteudo(String fxmlPath, Permissao permissao) {
        try {
            permissionService.exigir(permissao);

            Parent view = navigationService.load(fxmlPath);
            navigationService.navigateTo(fxmlPath);

            mostrarConteudo(view);
            atualizarBotoesNavegacao();

        } catch (NaoAutenticadoException | AcessoNegadoException e) {
            dialogService.erro("Acesso negado", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            dialogService.erro("Erro ao abrir tela", e.getMessage());
        }
    }

    private void abrirDestinoHistorico(String destino) {
        try {
            if (navigationService.isHome(destino)) {
                mostrarHomeSemAlterarHistorico();
                carregarDashboard();
            } else {
                Parent view = navigationService.load(destino);
                mostrarConteudo(view);
            }

            atualizarBotoesNavegacao();

        } catch (Exception e) {
            e.printStackTrace();
            dialogService.erro("Erro ao navegar", e.getMessage());
        }
    }

    private void mostrarHome() {
        navigationService.replaceCurrent(NavigationService.HOME);
        mostrarHomeSemAlterarHistorico();
        atualizarBotoesNavegacao();
    }

    private void mostrarHomeSemAlterarHistorico() {
        permissionService.aplicarVisibilidade(homeBox, true);

        if (contentPane != null) {
            contentPane.getChildren().clear();
            permissionService.aplicarVisibilidade(contentPane, false);
        }
    }

    private void mostrarConteudo(Parent view) {
        permissionService.aplicarVisibilidade(homeBox, false);

        if (contentPane == null) {
            dialogService.erro("Erro de layout", "A área de conteúdo principal não foi encontrada no FXML.");
            return;
        }

        contentPane.getChildren().setAll(view);

        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);

        permissionService.aplicarVisibilidade(contentPane, true);
    }

    private void atualizarUsuarioLogado() {
        sessionViewService.atualizarUsuarioLogado(lblUsuarioLogado, lblPerfilUsuario);
        usuarioLogado = sessionViewService.obterNomeUsuarioLogado();
    }

    private void carregarLogo() {
        URL logoUrl = getClass().getResource(LOGO_HOME);

        if (logoUrl == null) return;

        Image logo = new Image(logoUrl.toExternalForm());

        if (imgLogoHome != null) {
            imgLogoHome.setImage(logo);
        }

        if (imgLogoSidebar != null) {
            imgLogoSidebar.setImage(logo);
        }
    }

    private void carregarDashboard() {
        DashboardResumo resumo = dashboardService.carregarResumo();

        aplicarIndicador(resumo.agenda(), lblAgendaHojeValor, lblAgendaHojeSub, lblResumoAgenda);
        aplicarIndicador(resumo.pacientes(), lblPacientesAtivosValor, lblPacientesAtivosSub, lblResumoPacientes);
        aplicarIndicador(resumo.estoque(), lblEstoqueCriticoValor, lblEstoqueCriticoSub, lblResumoEstoque);
        aplicarIndicador(resumo.financeiro(), lblFinanceiroHojeValor, lblFinanceiroHojeSub, lblResumoFinanceiro);

        setText(lblAuditoriaRecente, resumo.auditoriaRecente());
    }

    private void aplicarIndicador(IndicadorResumo indicador, Label valor, Label subtitulo, Label resumo) {
        setText(valor, indicador.valor());
        setText(subtitulo, indicador.subtitulo());
        setText(resumo, indicador.resumo());
    }

    private void aplicarPermissoesMenu() {
        if (menuBarTop == null) return;

        permissionService.removerItemSeSemPermissao(menuCadastros, miPacientes, Permissao.PACIENTE_VER);
        permissionService.removerItemSeSemPermissao(menuOperacoes, miAgenda, Permissao.AGENDA_VER);
        permissionService.removerItemSeSemPermissao(menuOperacoes, miCaixa, Permissao.FINANCEIRO_VER);
        permissionService.removerItemSeSemPermissao(menuOperacoes, miEstoque, Permissao.ESTOQUE_VER);
        permissionService.removerItemSeSemPermissao(menuOperacoes, miAvaliacaoFisica, Permissao.AVALIACAO_FISICA_VER);
        permissionService.removerItemSeSemPermissao(menuRelatorios, miRelatorios, Permissao.RELATORIOS_VER);
        permissionService.removerItemSeSemPermissao(menuAdministracao, miUsuarios, Permissao.USUARIO_GERENCIAR);
        permissionService.removerItemSeSemPermissao(menuAdministracao, miAuditoria, Permissao.AUDITORIA_VER);

        permissionService.removerMenuSeVazio(menuBarTop, menuCadastros);
        permissionService.removerMenuSeVazio(menuBarTop, menuOperacoes);
        permissionService.removerMenuSeVazio(menuBarTop, menuRelatorios);
        permissionService.removerMenuSeVazio(menuBarTop, menuAdministracao);
    }

    private void aplicarPermissoesHome() {
        permissionService.aplicarPermissao(btnCardPacientes, Permissao.PACIENTE_VER);
        permissionService.aplicarPermissao(btnCardAgenda, Permissao.AGENDA_VER);
        permissionService.aplicarPermissao(btnCardCaixa, Permissao.FINANCEIRO_VER);
        permissionService.aplicarPermissao(btnCardEstoque, Permissao.ESTOQUE_VER);
        permissionService.aplicarPermissao(btnCardUsuarios, Permissao.USUARIO_GERENCIAR);
        permissionService.aplicarPermissao(btnCardAvaliacaoFisica, Permissao.AVALIACAO_FISICA_VER);

        permissionService.aplicarPermissao(btnDashboardCaixa, Permissao.FINANCEIRO_VER);
        permissionService.aplicarPermissao(btnDashboardEstoque, Permissao.ESTOQUE_VER);
        permissionService.aplicarPermissao(btnDashboardRelatorios, Permissao.RELATORIOS_VER);
        permissionService.aplicarPermissao(btnDashboardUsuarios, Permissao.USUARIO_GERENCIAR);
        permissionService.aplicarPermissao(btnDashboardAvaliacaoFisica, Permissao.AVALIACAO_FISICA_VER);

        permissionService.aplicarPermissao(cardFinanceiroDashboard, Permissao.FINANCEIRO_VER);
        permissionService.aplicarPermissao(cardEstoqueDashboard, Permissao.ESTOQUE_VER);

        permissionService.aplicarVisibilidade(adminDashboardBox, deveExibirAreaAdministrativa());
    }

    private boolean deveExibirAreaAdministrativa() {
        return permissionService.temAlgumaPermissao(
                Permissao.FINANCEIRO_VER,
                Permissao.RELATORIOS_VER,
                Permissao.USUARIO_GERENCIAR,
                Permissao.AUDITORIA_VER
        );
    }

    private void voltarParaLogin() {
        URL loginUrl = getClass().getResource(VIEW_LOGIN);

        if (loginUrl == null) {
            dialogService.erro("FXML não encontrado", "Não foi possível localizar a tela de login.");
            return;
        }

        try {
            Parent root = new FXMLLoader(loginUrl).load();
            Stage stage = obterStageAtual();

            if (stage == null) {
                dialogService.erro("Erro", "Não foi possível identificar a janela atual.");
                return;
            }

            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource(CSS_APP);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setMinWidth(1200);
            stage.setMinHeight(760);
            stage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();
            dialogService.erro("Erro", "Não foi possível voltar para a tela de login.");
        }
    }

    private Stage obterStageAtual() {
        Node[] candidatos = {contentPane, homeBox, menuBarTop};

        for (Node candidato : candidatos) {
            if (candidato != null && candidato.getScene() != null) {
                return (Stage) candidato.getScene().getWindow();
            }
        }

        return null;
    }

    private void atualizarBotoesNavegacao() {
        if (btnBack != null) {
            btnBack.setDisable(!navigationService.canGoBack());
        }

        if (btnForward != null) {
            btnForward.setDisable(!navigationService.canGoForward());
        }
    }

    private void setText(Label label, String texto) {
        if (label != null) {
            label.setText(texto == null ? "" : texto);
        }
    }
}