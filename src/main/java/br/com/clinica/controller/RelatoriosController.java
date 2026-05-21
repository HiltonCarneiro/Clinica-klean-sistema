package br.com.clinica.controller;

import br.com.clinica.controller.relatorios.RelatoriosAlertManager;
import br.com.clinica.controller.relatorios.RelatoriosCaixaSummaryManager;
import br.com.clinica.controller.relatorios.RelatoriosInitialSetupManager;
import br.com.clinica.controller.relatorios.RelatoriosNotaReimpressaoManager;
import br.com.clinica.controller.relatorios.RelatoriosPdfExportManager;
import br.com.clinica.controller.relatorios.RelatoriosSearchManager;
import br.com.clinica.controller.relatorios.RelatoriosValidationManager;
import br.com.clinica.dao.AgendamentoDAO;
import br.com.clinica.dao.MovimentoCaixaDAO;
import br.com.clinica.dao.NotaDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.Usuario;
import br.com.clinica.service.NotaPdfService;
import br.com.clinica.service.RelatorioAgendamentosPdfService;
import br.com.clinica.service.RelatorioCaixaPdfService;
import br.com.clinica.service.RelatorioNotasPdfService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RelatoriosController {

    @FXML private DatePicker dtCxInicio;
    @FXML private DatePicker dtCxFim;

    @FXML private TableView<MovimentoCaixa> tblCaixa;
    @FXML private TableColumn<MovimentoCaixa, String> colCxData;
    @FXML private TableColumn<MovimentoCaixa, String> colCxTipo;
    @FXML private TableColumn<MovimentoCaixa, String> colCxDescricao;
    @FXML private TableColumn<MovimentoCaixa, String> colCxForma;
    @FXML private TableColumn<MovimentoCaixa, Double> colCxValor;
    @FXML private TableColumn<MovimentoCaixa, String> colCxPaciente;

    @FXML private Label lblCxEntradas;
    @FXML private Label lblCxSaidas;
    @FXML private Label lblCxSaldo;

    private final ObservableList<MovimentoCaixa> caixaObs =
            FXCollections.observableArrayList();

    @FXML private DatePicker dtAgInicio;
    @FXML private DatePicker dtAgFim;
    @FXML private ComboBox<Usuario> cbAgProfissional;

    @FXML private TableView<Agendamento> tblAgendamentos;
    @FXML private TableColumn<Agendamento, String> colAgData;
    @FXML private TableColumn<Agendamento, String> colAgHora;
    @FXML private TableColumn<Agendamento, String> colAgProf;
    @FXML private TableColumn<Agendamento, String> colAgSala;
    @FXML private TableColumn<Agendamento, String> colAgPaciente;
    @FXML private TableColumn<Agendamento, String> colAgStatus;
    @FXML private TableColumn<Agendamento, String> colAgProced;

    private final ObservableList<Agendamento> agObs =
            FXCollections.observableArrayList();

    @FXML private DatePicker dtNotaInicio;
    @FXML private DatePicker dtNotaFim;
    @FXML private TextField txtPacienteFiltro;
    @FXML private ComboBox<Usuario> cbNotaProfissional;
    @FXML private ComboBox<String> cbNotaForma;

    @FXML private TableView<NotaDAO.NotaResumo> tblNotas;
    @FXML private TableColumn<NotaDAO.NotaResumo, Long> colNotaId;
    @FXML private TableColumn<NotaDAO.NotaResumo, String> colNotaDataHora;
    @FXML private TableColumn<NotaDAO.NotaResumo, String> colNotaPaciente;
    @FXML private TableColumn<NotaDAO.NotaResumo, String> colNotaProfissional;
    @FXML private TableColumn<NotaDAO.NotaResumo, String> colNotaForma;
    @FXML private TableColumn<NotaDAO.NotaResumo, Double> colNotaTotal;

    private final ObservableList<NotaDAO.NotaResumo> notasObs =
            FXCollections.observableArrayList();

    private final MovimentoCaixaDAO movimentoCaixaDAO = new MovimentoCaixaDAO();
    private final AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private final NotaDAO notaDAO = new NotaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final NotaPdfService notaPdfService = new NotaPdfService();
    private final RelatorioCaixaPdfService relCaixaPdf = new RelatorioCaixaPdfService();
    private final RelatorioAgendamentosPdfService relAgPdf = new RelatorioAgendamentosPdfService();
    private final RelatorioNotasPdfService relNotasPdf = new RelatorioNotasPdfService();

    private final RelatoriosInitialSetupManager initialSetupManager =
            new RelatoriosInitialSetupManager();

    private final RelatoriosValidationManager validationManager =
            new RelatoriosValidationManager();

    private final RelatoriosAlertManager alertManager =
            new RelatoriosAlertManager();

    private final RelatoriosPdfExportManager pdfExportManager =
            new RelatoriosPdfExportManager();

    private final RelatoriosNotaReimpressaoManager notaReimpressaoManager =
            new RelatoriosNotaReimpressaoManager();

    private final RelatoriosCaixaSummaryManager caixaSummaryManager =
            new RelatoriosCaixaSummaryManager();

    private final RelatoriosSearchManager searchManager =
            new RelatoriosSearchManager();

    private static final String FORMA_TODAS =
            RelatoriosInitialSetupManager.FORMA_TODAS;

    @FXML
    public void initialize() {

        initialSetupManager.configurarDatasPadrao(
                dtCxInicio,
                dtCxFim,
                dtAgInicio,
                dtAgFim,
                dtNotaInicio,
                dtNotaFim
        );

        ObservableList<Usuario> profObs =
                initialSetupManager.montarListaProfissionais(
                        usuarioDAO.listarProfissionaisAtivos()
                );

        initialSetupManager.configurarComboProfissionais(
                cbAgProfissional,
                profObs,
                this::nomeProf
        );

        initialSetupManager.configurarComboProfissionais(
                cbNotaProfissional,
                profObs,
                this::nomeProf
        );

        initialSetupManager.configurarComboFormaPagamento(cbNotaForma);

        initialSetupManager.configurarTabelaCaixa(
                tblCaixa,
                caixaObs,
                colCxData,
                colCxTipo,
                colCxDescricao,
                colCxForma,
                colCxValor,
                colCxPaciente
        );

        initialSetupManager.configurarTabelaAgendamentos(
                tblAgendamentos,
                agObs,
                colAgData,
                colAgHora,
                colAgProf,
                colAgSala,
                colAgPaciente,
                colAgStatus,
                colAgProced
        );

        initialSetupManager.configurarTabelaNotas(
                tblNotas,
                notasObs,
                colNotaId,
                colNotaDataHora,
                colNotaPaciente,
                colNotaProfissional,
                colNotaForma,
                colNotaTotal
        );

        onBuscarCaixa();
        onBuscarAgendamentos();
        onBuscarNotas();
    }

    private String nomeProf(Usuario u) {
        if (u == null) {
            return "";
        }

        String pessoa =
                u.getPessoaNome() != null && !u.getPessoaNome().isBlank()
                        ? u.getPessoaNome()
                        : "";

        String cargo =
                u.getNome() != null && !u.getNome().isBlank()
                        ? u.getNome()
                        : "";

        if (!pessoa.isBlank() && !cargo.isBlank()) {
            return pessoa + " (" + cargo + ")";
        }

        if (!pessoa.isBlank()) {
            return pessoa;
        }

        return cargo;
    }

    @FXML
    private void onBuscarCaixa() {
        searchManager.buscarCaixa(
                dtCxInicio.getValue(),
                dtCxFim.getValue(),
                movimentoCaixaDAO,
                caixaObs,
                validationManager,
                caixaSummaryManager,
                lblCxEntradas,
                lblCxSaidas,
                lblCxSaldo,
                this::erro
        );
    }

    @FXML
    private void onExportarCaixaPdf() {
        pdfExportManager.exportarCaixaPdf(
                tblCaixa,
                caixaObs,
                dtCxInicio.getValue(),
                dtCxFim.getValue(),
                relCaixaPdf,
                this::aviso,
                this::erro
        );
    }

    @FXML
    private void onBuscarAgendamentos() {
        searchManager.buscarAgendamentos(
                dtAgInicio.getValue(),
                dtAgFim.getValue(),
                cbAgProfissional.getValue(),
                agendamentoDAO,
                agObs,
                validationManager,
                this::erro
        );
    }

    @FXML
    private void onExportarAgendamentosPdf() {
        Usuario prof = cbAgProfissional.getValue();
        String profTxt = prof == null ? "" : nomeProf(prof);

        pdfExportManager.exportarAgendamentosPdf(
                tblAgendamentos,
                agObs,
                dtAgInicio.getValue(),
                dtAgFim.getValue(),
                profTxt,
                relAgPdf,
                this::aviso,
                this::erro
        );
    }

    @FXML
    private void onBuscarNotas() {
        String pacienteLike =
                txtPacienteFiltro.getText() == null
                        ? ""
                        : txtPacienteFiltro.getText().trim();

        searchManager.buscarNotas(
                dtNotaInicio.getValue(),
                dtNotaFim.getValue(),
                pacienteLike,
                cbNotaProfissional.getValue(),
                cbNotaForma.getValue(),
                FORMA_TODAS,
                notaDAO,
                notasObs,
                validationManager,
                this::erro
        );
    }

    @FXML
    private void onExportarNotasPdf() {
        String pacienteLike =
                txtPacienteFiltro.getText() == null
                        ? ""
                        : txtPacienteFiltro.getText().trim();

        Usuario prof = cbNotaProfissional.getValue();
        String profTxt = prof == null ? "" : nomeProf(prof);

        String forma = cbNotaForma.getValue();

        if (FORMA_TODAS.equals(forma)) {
            forma = "";
        }

        pdfExportManager.exportarNotasPdf(
                tblNotas,
                notasObs,
                dtNotaInicio.getValue(),
                dtNotaFim.getValue(),
                pacienteLike,
                profTxt,
                forma,
                relNotasPdf,
                this::aviso,
                this::erro
        );
    }

    @FXML
    private void onReimprimirNota() {
        notaReimpressaoManager.reimprimirNota(
                tblNotas,
                notaDAO,
                notaPdfService,
                this::aviso,
                this::erro
        );
    }

    private void erro(String titulo, String msg) {
        alertManager.erro(titulo, msg);
    }

    private void aviso(String titulo, String msg) {
        alertManager.aviso(titulo, msg);
    }
}