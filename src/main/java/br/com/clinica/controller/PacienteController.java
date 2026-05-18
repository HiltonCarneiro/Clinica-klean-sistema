package br.com.clinica.controller;

import br.com.clinica.controller.paciente.*;
import br.com.clinica.dto.PacienteFormData;
import br.com.clinica.mapper.PacienteFormMapper;
import br.com.clinica.model.Paciente;
import br.com.clinica.service.PacienteService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class PacienteController {

    @FXML private VBox boxEscolha;
    @FXML private ScrollPane scrollCadastro;
    @FXML private VBox boxBusca;

    @FXML private TextField txtBusca;
    @FXML private TableView<Paciente> tableBusca;
    @FXML private TableColumn<Paciente, String> colBuscaNome;
    @FXML private TableColumn<Paciente, String> colBuscaCpf;
    @FXML private TableColumn<Paciente, String> colBuscaTelefone;
    @FXML private TableColumn<Paciente, String> colBuscaDataNascimento;
    @FXML private TableColumn<Paciente, String> colBuscaAtivo;

    @FXML private CheckBox chkMostrarInativosBusca;
    @FXML private Button btnEditarSelecionado;
    @FXML private Button btnHistoricoSelecionado;

    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtRg;
    @FXML private DatePicker dpDataNascimento;
    @FXML private TextField txtIdade;
    @FXML private TextField txtTelefone;

    @FXML private TextField txtRua;
    @FXML private TextField txtNumero;
    @FXML private CheckBox chkSemNumero;
    @FXML private TextField txtComplemento;
    @FXML private TextField txtBairro;
    @FXML private TextField txtCidade;
    @FXML private TextField txtCep;
    @FXML private TextField txtUf;
    @FXML private TextField txtResponsavelLegal;

    @FXML private Label lblMensagem;
    @FXML private Button btnInativar;
    @FXML private Button btnAtivar;
    @FXML private CheckBox chkMostrarInativos;

    private final PacienteService pacienteService = new PacienteService();
    private final PacienteFormMapper pacienteFormMapper = new PacienteFormMapper();
    private final PacienteDocumentFormatter formatter = new PacienteDocumentFormatter();
    private final PacienteDatePickerConfigurer datePickerConfigurer = new PacienteDatePickerConfigurer();
    private final PacienteAgeCalculator ageCalculator = new PacienteAgeCalculator();
    private final PacienteHistoryNavigator historyNavigator = new PacienteHistoryNavigator();

    private PacienteScreenManager screenManager;
    private PacienteFormManager formManager;
    private PacienteSearchManager searchManager;
    private PacienteActionManager actionManager;
    private PacienteCepManager cepManager;

    private Paciente pacienteSelecionado;
    private boolean voltarParaBusca;

    @FXML
    private void initialize() {

        screenManager = new PacienteScreenManager(
                boxEscolha,
                scrollCadastro,
                boxBusca
        );

        formManager = new PacienteFormManager(
                txtNome,
                txtCpf,
                txtRg,
                dpDataNascimento,
                txtIdade,
                txtTelefone,
                txtRua,
                txtNumero,
                chkSemNumero,
                txtComplemento,
                txtBairro,
                txtCidade,
                txtCep,
                txtUf,
                txtResponsavelLegal,
                formatter,
                this::formatRg
        );

        searchManager = new PacienteSearchManager(
                txtBusca,
                chkMostrarInativosBusca,
                tableBusca,
                colBuscaNome,
                colBuscaCpf,
                colBuscaTelefone,
                colBuscaDataNascimento,
                colBuscaAtivo,
                btnEditarSelecionado,
                btnHistoricoSelecionado,
                pacienteService,
                formatter,
                this::formatRg,
                paciente -> pacienteSelecionado = paciente
        );

        actionManager = new PacienteActionManager(
                pacienteService,
                pacienteFormMapper,
                this::obterDadosFormulario,
                this::setMensagem
        );

        cepManager = new PacienteCepManager(
                txtCep,
                txtRua,
                txtBairro,
                txtCidade,
                txtUf,
                this::setMensagem
        );

        configurarTelaInicial();
        configurarTabela();
        configurarCampos();
        configurarBotoes();

        screenManager.mostrarEscolha();

        setMensagem("");
    }

    private void configurarTelaInicial() {

        if (txtIdade != null) {
            txtIdade.setEditable(false);
            txtIdade.setFocusTraversable(false);
        }
    }

    private void configurarTabela() {
        searchManager.configurarTabela();
    }

    private void configurarCampos() {

        if (txtNome != null) {
            txtNome.setTextFormatter(
                    PacienteTextFormatterFactory.nameFormatter()
            );
        }

        if (txtCpf != null) {
            txtCpf.setTextFormatter(
                    PacienteTextFormatterFactory.digitsFormatter(
                            11,
                            formatter::formatCpf
                    )
            );
        }

        if (txtRg != null) {
            txtRg.setTextFormatter(
                    PacienteTextFormatterFactory.digitsFormatter(
                            7,
                            this::formatRg
                    )
            );
        }

        if (txtTelefone != null) {
            txtTelefone.setTextFormatter(
                    PacienteTextFormatterFactory.digitsFormatter(
                            11,
                            formatter::formatTelefone
                    )
            );
        }

        if (txtCep != null) {
            txtCep.setTextFormatter(
                    PacienteTextFormatterFactory.digitsFormatter(
                            8,
                            formatter::formatCep
                    )
            );
        }

        if (txtUf != null) {
            txtUf.setTextFormatter(
                    PacienteTextFormatterFactory.ufFormatter()
            );
        }

        if (txtNumero != null) {
            txtNumero.setTextFormatter(
                    PacienteTextFormatterFactory.digitsFormatter(
                            6,
                            value -> value
                    )
            );
        }

        if (chkSemNumero != null && txtNumero != null) {

            chkSemNumero.selectedProperty().addListener(
                    (obs, oldValue, selected) -> {

                        txtNumero.setDisable(selected);

                        if (selected) {
                            txtNumero.clear();
                        }
                    }
            );
        }

        datePickerConfigurer.configure(
                dpDataNascimento,
                this::setMensagem,
                this::atualizarIdade
        );

        if (dpDataNascimento != null) {

            dpDataNascimento.valueProperty().addListener(
                    (obs, oldValue, newValue) -> atualizarIdade()
            );
        }

        searchManager.configurarCampoBusca(this::onBuscarPaciente);

        cepManager.configurarBuscaPorEnter();
    }

    private void configurarBotoes() {
        atualizarBotoesStatus(null);
        searchManager.limparSelecao();
    }

    @FXML
    private void voltarEscolha() {

        if (screenManager.cadastroVisivel() && voltarParaBusca) {

            voltarParaBusca = false;

            irParaBusca();

            return;
        }

        searchManager.limparSelecao();

        screenManager.mostrarEscolha();

        setMensagem("");
    }

    @FXML
    private void irParaCadastro() {
        screenManager.mostrarCadastro();
    }

    @FXML
    private void irParaBusca() {

        screenManager.mostrarBusca();

        onBuscarPaciente();
    }

    @FXML
    private void onNovo() {

        pacienteSelecionado = null;
        voltarParaBusca = false;

        limparFormulario();

        atualizarBotoesStatus(null);

        setMensagem("");

        screenManager.mostrarCadastro();
    }

    @FXML
    private void onBuscarPaciente() {
        searchManager.buscar(this::setMensagem);
    }

    @FXML
    private void onLimparBusca() {
        searchManager.limparBusca(this::setMensagem);
    }

    @FXML
    private void onEditarSelecionado() {

        Paciente paciente = searchManager.obterSelecionado();

        if (paciente == null) {

            mostrarAviso("Selecione um paciente para editar.");

            return;
        }

        pacienteSelecionado = paciente;

        voltarParaBusca = true;

        preencherFormulario(paciente);

        atualizarBotoesStatus(paciente);

        screenManager.mostrarCadastro();
    }

    @FXML
    private void onVerHistoricoSelecionado() {

        Paciente paciente = searchManager.obterSelecionado();

        if (paciente == null) {

            mostrarAviso("Selecione um paciente para ver o histórico.");

            return;
        }

        try {

            historyNavigator.abrirHistorico(paciente);

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro("Erro ao abrir histórico do paciente.");
        }
    }

    @FXML
    private void onSalvar() {

        pacienteSelecionado = actionManager.salvar(pacienteSelecionado);

        atualizarBotoesStatus(pacienteSelecionado);

        atualizarListagens();
    }

    @FXML
    private void onInativar() {

        actionManager.inativar(pacienteSelecionado);

        atualizarBotoesStatus(pacienteSelecionado);

        atualizarListagens();
    }

    @FXML
    private void onAtivar() {

        actionManager.ativar(pacienteSelecionado);

        atualizarBotoesStatus(pacienteSelecionado);

        atualizarListagens();
    }

    @FXML
    private void onAtualizarLista() {

        boolean incluirInativos =
                chkMostrarInativos != null
                        && chkMostrarInativos.isSelected();

        searchManager.carregarTodos(
                incluirInativos,
                this::setMensagem
        );
    }

    private PacienteFormData obterDadosFormulario() {
        return formManager.obterDadosFormulario();
    }

    private void preencherFormulario(Paciente paciente) {

        formManager.preencherFormulario(
                pacienteFormMapper.toFormData(paciente)
        );

        atualizarIdade();

        setMensagem("");
    }

    private void limparFormulario() {
        formManager.limparFormulario();
    }

    private void atualizarIdade() {

        if (txtIdade == null) {
            return;
        }

        LocalDate nascimento =
                dpDataNascimento == null
                        ? null
                        : dpDataNascimento.getValue();

        txtIdade.setText(
                ageCalculator.calcularTexto(nascimento)
        );
    }

    private void atualizarListagens() {

        onAtualizarLista();

        if (boxBusca != null && boxBusca.isVisible()) {
            onBuscarPaciente();
        }
    }

    private void atualizarBotoesStatus(Paciente paciente) {

        boolean temPaciente =
                paciente != null
                        && paciente.getId() != null;

        boolean ativo =
                temPaciente
                        && paciente.isAtivo();

        if (btnInativar != null) {
            btnInativar.setDisable(!temPaciente || !ativo);
        }

        if (btnAtivar != null) {
            btnAtivar.setDisable(!temPaciente || ativo);
        }
    }

    private String formatRg(String value) {

        String digits = digits(value);

        if (digits.length() > 7) {
            digits = digits.substring(0, 7);
        }

        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < digits.length(); i++) {

            if (i == 1 || i == 4) {
                formatted.append(".");
            }

            formatted.append(digits.charAt(i));
        }

        return formatted.toString();
    }

    private String digits(String value) {
        return safe(value).replaceAll("\\D", "");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void setMensagem(String mensagem) {

        if (lblMensagem != null) {
            lblMensagem.setText(
                    mensagem == null ? "" : mensagem
            );
        }
    }

    private void mostrarAviso(String mensagem) {
        new Alert(Alert.AlertType.WARNING, mensagem).showAndWait();
    }

    private void mostrarErro(String mensagem) {
        new Alert(Alert.AlertType.ERROR, mensagem).showAndWait();
    }
}