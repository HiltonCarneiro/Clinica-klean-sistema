package br.com.clinica.controller;

import br.com.clinica.auth.AuthGuard;
import br.com.clinica.auth.Permissao;
import br.com.clinica.auth.exceptions.AcessoNegadoException;
import br.com.clinica.auth.exceptions.NaoAutenticadoException;
import br.com.clinica.dao.AgendamentoDAO;
import br.com.clinica.dao.AuditoriaDAO;
import br.com.clinica.dao.MovimentoCaixaDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.ProdutoDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.Produto;
import br.com.clinica.model.enums.TipoMovimento;
import br.com.clinica.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

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

    @FXML private Button btnDashboardCaixa;
    @FXML private Button btnDashboardEstoque;
    @FXML private Button btnDashboardRelatorios;
    @FXML private Button btnDashboardUsuarios;

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

    private String usuarioLogado;

    private final Deque<String> backStack = new ArrayDeque<>();
    private final Deque<String> forwardStack = new ArrayDeque<>();

    private String currentView = null;

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final MovimentoCaixaDAO movimentoCaixaDAO = new MovimentoCaixaDAO();
    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    @FXML
    private void initialize() {
        mostrarHome();
        atualizarUsuarioLogado();
        aplicarPermissoesHome();
        aplicarPermissoesMenu();
        carregarLogo();
        carregarDashboard();
    }

    private void carregarLogo() {
        var logoUrl = getClass().getResource("/images/logo-klean.png");

        if (logoUrl != null && imgLogoHome != null) {
            imgLogoHome.setImage(new Image(logoUrl.toExternalForm()));
        }
    }

    private void atualizarUsuarioLogado() {
        if (Session.getUsuario() != null) {
            lblUsuarioLogado.setText(Session.getUsuario().getPessoaNome());

            if (lblPerfilUsuario != null && Session.getUsuario().getPerfil() != null) {
                lblPerfilUsuario.setText("Perfil: " + Session.getUsuario().getPerfil().getNome());
            }
        } else {
            lblUsuarioLogado.setText("-");

            if (lblPerfilUsuario != null) {
                lblPerfilUsuario.setText("Perfil: -");
            }
        }

        atualizarBotoesNavegacao();
    }

    private void carregarDashboard() {
        carregarResumoAgenda();
        carregarResumoPacientes();

        if (temPermissao(Permissao.ESTOQUE_VER)) {
            carregarResumoEstoque();
        } else {
            setText(lblResumoEstoque, "• Consulte o histórico clínico antes de iniciar um atendimento.");
        }

        if (temPermissao(Permissao.FINANCEIRO_VER)) {
            carregarResumoFinanceiro();
        } else {
            setText(lblResumoFinanceiro, "• Registre evoluções e prontuários conforme o atendimento.");
        }

        if (temPermissao(Permissao.AUDITORIA_VER)) {
            carregarResumoAuditoria();
        }
    }

    private void carregarResumoAgenda() {
        try {
            List<Agendamento> agendaHoje = agendamentoDAO.listarPorData(LocalDate.now());

            setText(lblAgendaHojeValor, agendaHoje.size() + " agendamento(s)");
            setText(lblAgendaHojeSub, agendaHoje.isEmpty()
                    ? "Nenhum atendimento pendente hoje"
                    : "Próximo: " + safe(agendaHoje.get(0).getPacienteNome()));

            setText(lblResumoAgenda, agendaHoje.isEmpty()
                    ? "• Hoje não há agendamentos pendentes."
                    : "• Existem " + agendaHoje.size() + " atendimento(s) pendente(s) hoje.");

        } catch (Exception e) {
            setText(lblAgendaHojeValor, "-");
            setText(lblAgendaHojeSub, "Não foi possível carregar a agenda");
            setText(lblResumoAgenda, "• Agenda indisponível no momento.");
        }
    }

    private void carregarResumoPacientes() {
        try {
            int ativos = pacienteDAO.listarTodos(false).size();

            setText(lblPacientesAtivosValor, ativos + " ativo(s)");
            setText(lblPacientesAtivosSub, "Pacientes disponíveis para atendimento");
            setText(lblResumoPacientes, "• " + ativos + " paciente(s) ativo(s) cadastrados.");

        } catch (Exception e) {
            setText(lblPacientesAtivosValor, "-");
            setText(lblPacientesAtivosSub, "Não foi possível carregar pacientes");
            setText(lblResumoPacientes, "• Pacientes indisponíveis no momento.");
        }
    }

    private void carregarResumoEstoque() {
        try {
            List<Produto> baixoEstoque = produtoDAO.listar(false, true, false);

            setText(lblEstoqueCriticoValor, baixoEstoque.size() + " item(ns)");
            setText(lblEstoqueCriticoSub, baixoEstoque.isEmpty()
                    ? "Nenhum item crítico"
                    : "Itens abaixo do estoque mínimo");

            setText(lblResumoEstoque, baixoEstoque.isEmpty()
                    ? "• Estoque sem alertas críticos."
                    : "• " + baixoEstoque.size() + " item(ns) precisam de atenção no estoque.");

        } catch (Exception e) {
            setText(lblEstoqueCriticoValor, "-");
            setText(lblEstoqueCriticoSub, "Não foi possível carregar estoque");
            setText(lblResumoEstoque, "• Estoque indisponível no momento.");
        }
    }

    private void carregarResumoFinanceiro() {
        try {
            List<MovimentoCaixa> movimentos = movimentoCaixaDAO.listarPorPeriodo(LocalDate.now(), LocalDate.now());

            double entradas = movimentos.stream()
                    .filter(m -> m.getTipo() == TipoMovimento.ENTRADA)
                    .mapToDouble(MovimentoCaixa::getValor)
                    .sum();

            double saidas = movimentos.stream()
                    .filter(m -> m.getTipo() == TipoMovimento.SAIDA)
                    .mapToDouble(MovimentoCaixa::getValor)
                    .sum();

            double saldo = entradas - saidas;

            setText(lblFinanceiroHojeValor, formatMoney(saldo));
            setText(lblFinanceiroHojeSub, movimentos.size() + " movimentação(ões) hoje");
            setText(lblResumoFinanceiro, "• Saldo do dia: " + formatMoney(saldo)
                    + " | Entradas: " + formatMoney(entradas)
                    + " | Saídas: " + formatMoney(saidas));

        } catch (Exception e) {
            setText(lblFinanceiroHojeValor, "-");
            setText(lblFinanceiroHojeSub, "Não foi possível carregar financeiro");
            setText(lblResumoFinanceiro, "• Financeiro indisponível no momento.");
        }
    }

    private void carregarResumoAuditoria() {
        try {
            List<AuditoriaDAO.LinhaAuditoria> ultimos = auditoriaDAO.listarUltimos(3);

            if (ultimos.isEmpty()) {
                setText(lblAuditoriaRecente, "• Nenhuma atividade recente registrada.");
                return;
            }

            StringBuilder sb = new StringBuilder();

            for (AuditoriaDAO.LinhaAuditoria linha : ultimos) {
                sb.append("• ")
                        .append(safe(linha.acao))
                        .append(" em ")
                        .append(safe(linha.entidade))
                        .append(" por ")
                        .append(safe(linha.usuario))
                        .append("\n");
            }

            setText(lblAuditoriaRecente, sb.toString().trim());

        } catch (Exception e) {
            setText(lblAuditoriaRecente, "• Auditoria indisponível no momento.");
        }
    }

    private void atualizarBotoesNavegacao() {
        if (btnBack != null) {
            btnBack.setDisable(backStack.isEmpty());
        }

        if (btnForward != null) {
            btnForward.setDisable(forwardStack.isEmpty());
        }
    }

    private void aplicarPermissoesMenu() {
        if (menuBarTop == null) {
            return;
        }

        if (!temPermissao(Permissao.PACIENTE_VER)) removerItem(menuCadastros, miPacientes);
        if (!temPermissao(Permissao.AGENDA_VER)) removerItem(menuOperacoes, miAgenda);
        if (!temPermissao(Permissao.FINANCEIRO_VER)) removerItem(menuOperacoes, miCaixa);
        if (!temPermissao(Permissao.ESTOQUE_VER)) removerItem(menuOperacoes, miEstoque);
        if (!temPermissao(Permissao.RELATORIOS_VER)) removerItem(menuRelatorios, miRelatorios);
        if (!temPermissao(Permissao.USUARIO_GERENCIAR)) removerItem(menuAdministracao, miUsuarios);
        if (!temPermissao(Permissao.AUDITORIA_VER)) removerItem(menuAdministracao, miAuditoria);

        removerMenuSeVazio(menuCadastros);
        removerMenuSeVazio(menuOperacoes);
        removerMenuSeVazio(menuRelatorios);
        removerMenuSeVazio(menuAdministracao);
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
        if (menu == null) {
            return;
        }

        if (menu.getItems() == null || menu.getItems().isEmpty()) {
            removerMenu(menu);
        }
    }

    public void setUsuarioLogado(String usuario) {
        this.usuarioLogado = usuario;

        if (lblUsuarioLogado != null) {
            lblUsuarioLogado.setText(usuario);
        }
    }

    @FXML
    private void onInicio() {
        backStack.clear();
        forwardStack.clear();
        currentView = null;
        mostrarHome();
        carregarDashboard();
        atualizarBotoesNavegacao();
    }

    @FXML
    private void onBack() {
        if (backStack.isEmpty()) return;

        String previous = backStack.pop();

        if (currentView != null) {
            forwardStack.push(currentView);
        }

        if (HOME.equals(previous)) {
            mostrarHome();
        } else {
            loadView(previous, false);
        }
    }

    @FXML
    private void onForward() {
        if (forwardStack.isEmpty()) return;

        String next = forwardStack.pop();

        if (currentView != null) {
            backStack.push(currentView);
        }

        if (HOME.equals(next)) {
            mostrarHome();
        } else {
            loadView(next, false);
        }
    }

    @FXML private void onPacientes() { abrirTelaNoConteudo("/view/paciente-view.fxml", Permissao.PACIENTE_VER); }
    @FXML private void onAgenda() { abrirTelaNoConteudo("/view/agenda-view.fxml", Permissao.AGENDA_VER); }
    @FXML private void onCaixa() { abrirTelaNoConteudo("/view/caixa-view.fxml", Permissao.FINANCEIRO_VER); }
    @FXML private void onEstoque() { abrirTelaNoConteudo("/view/estoque-view.fxml", Permissao.ESTOQUE_VER); }
    @FXML private void onRelatorios() { abrirTelaNoConteudo("/view/relatorios-view.fxml", Permissao.RELATORIOS_VER); }
    @FXML private void onUsuarios() { abrirTelaNoConteudo("/view/usuarios-view.fxml", Permissao.USUARIO_GERENCIAR); }
    @FXML private void onAuditoria() { abrirTelaNoConteudo("/view/auditoria-view.fxml", Permissao.AUDITORIA_VER); }

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
                if (currentView != null) {
                    backStack.push(currentView);
                }

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

        aplicarPermissao(btnDashboardCaixa, Permissao.FINANCEIRO_VER);
        aplicarPermissao(btnDashboardEstoque, Permissao.ESTOQUE_VER);
        aplicarPermissao(btnDashboardRelatorios, Permissao.RELATORIOS_VER);
        aplicarPermissao(btnDashboardUsuarios, Permissao.USUARIO_GERENCIAR);

        aplicarPermissao(cardFinanceiroDashboard, Permissao.FINANCEIRO_VER);
        aplicarPermissao(cardEstoqueDashboard, Permissao.ESTOQUE_VER);

        boolean podeVerAreaAdmin =
                temPermissao(Permissao.FINANCEIRO_VER)
                        || temPermissao(Permissao.RELATORIOS_VER)
                        || temPermissao(Permissao.USUARIO_GERENCIAR)
                        || temPermissao(Permissao.AUDITORIA_VER);

        aplicarVisibilidade(adminDashboardBox, podeVerAreaAdmin);
    }

    private void aplicarPermissao(Button btn, Permissao permissao) {
        aplicarVisibilidade(btn, temPermissao(permissao));
    }

    private void aplicarPermissao(VBox box, Permissao permissao) {
        aplicarVisibilidade(box, temPermissao(permissao));
    }

    private void aplicarVisibilidade(Node node, boolean visivel) {
        if (node == null) return;

        node.setVisible(visivel);
        node.setManaged(visivel);
    }

    @FXML
    private void onSair() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Sair do sistema");
        confirm.setHeaderText("Encerrar sessão");
        confirm.setContentText("Deseja realmente sair e voltar para a tela de login?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        Session.limpar();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentPane.getScene().getWindow();

            Scene scene = new Scene(root);

            var cssUrl = getClass().getResource("/styles/app.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro", "Não foi possível voltar para a tela de login.");
        }
    }

    private void setText(Label label, String texto) {
        if (label != null) {
            label.setText(texto == null ? "" : texto);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatMoney(double valor) {
        return NumberFormat
                .getCurrencyInstance(new Locale("pt", "BR"))
                .format(valor);
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}