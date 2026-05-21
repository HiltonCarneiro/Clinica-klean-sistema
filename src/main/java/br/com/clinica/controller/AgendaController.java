package br.com.clinica.controller;

import br.com.clinica.controller.agenda.*;
import br.com.clinica.dto.AgendaFormData;
import br.com.clinica.mapper.AgendaFormMapper;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.model.enums.SalaAtendimento;
import br.com.clinica.service.AgendaAccessPolicy;
import br.com.clinica.service.AgendaService;
import br.com.clinica.session.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class AgendaController {

    @FXML private DatePicker dpData;
    @FXML private ComboBox<Usuario> cbProfissional;
    @FXML private ComboBox<Paciente> cbPaciente;
    @FXML private ComboBox<SalaAtendimento> cbSala;

    @FXML private TextField txtHoraInicio;
    @FXML private TextField txtHoraFim;
    @FXML private TextField txtProcedimento;
    @FXML private TextArea txtObservacoes;

    @FXML private Label lblMensagem;
    @FXML private Button btnFinalizarConsulta;

    @FXML private TableView<Agendamento> tbAgenda;
    @FXML private TableColumn<Agendamento, String> colHora;
    @FXML private TableColumn<Agendamento, String> colProfissional;
    @FXML private TableColumn<Agendamento, String> colSala;
    @FXML private TableColumn<Agendamento, String> colPaciente;
    @FXML private TableColumn<Agendamento, String> colStatus;

    private final AgendaService agendaService = new AgendaService();
    private final AgendaAccessPolicy accessPolicy = new AgendaAccessPolicy();
    private final AgendaDisplayFormatter displayFormatter = new AgendaDisplayFormatter();
    private final AgendaDatePickerConfigurer datePickerConfigurer = new AgendaDatePickerConfigurer();
    private final AgendaTableManager tableManager = new AgendaTableManager();
    private final AgendaFormMapper formMapper = new AgendaFormMapper();
    private final AgendaNavigationManager navigationManager = new AgendaNavigationManager();
    private final AgendaDateRuleManager dateRuleManager = new AgendaDateRuleManager();
    private final AgendaInitialSetupManager initialSetupManager = new AgendaInitialSetupManager();
    private final AgendaLoadManager loadManager = new AgendaLoadManager();
    private final AgendaFormManager formManager = new AgendaFormManager();
    private final AgendaSelectionManager selectionManager = new AgendaSelectionManager();
    private final AgendaMessageManager messageManager = new AgendaMessageManager();
    private final AgendaDateChangeManager dateChangeManager = new AgendaDateChangeManager();
    private final AgendaRefreshManager refreshManager = new AgendaRefreshManager();

    private final ObservableList<Agendamento> agendamentos =
            FXCollections.observableArrayList();

    private final AgendaComboBoxConfigurer<Usuario> profissionalComboConfigurer =
            new AgendaComboBoxConfigurer<>();

    private final AgendaComboBoxConfigurer<Paciente> pacienteComboConfigurer =
            new AgendaComboBoxConfigurer<>();

    private final AgendaProcedureSuggestionManager procedureSuggestionManager =
            new AgendaProcedureSuggestionManager(agendaService);

    private AgendaActionManager actionManager;

    @FXML
    public void initialize() {

        actionManager = new AgendaActionManager(
                agendaService,
                this::setMessage
        );

        initialSetupManager.configure(
                dpData,
                cbProfissional,
                cbPaciente,
                cbSala,
                txtHoraInicio,
                txtHoraFim,
                txtProcedimento,
                btnFinalizarConsulta,
                tbAgenda,
                colHora,
                colProfissional,
                colSala,
                colPaciente,
                colStatus,
                agendaService,
                accessPolicy,
                displayFormatter,
                datePickerConfigurer,
                tableManager,
                profissionalComboConfigurer,
                pacienteComboConfigurer,
                procedureSuggestionManager,
                this::setMessage,
                this::carregarAgendaDoDia,
                this::preencherFormulario
        );

        tbAgenda.setItems(agendamentos);

        tableManager.clear(tbAgenda);

        setMessage("Selecione uma data para visualizar a agenda.");
    }

    @FXML
    private void onNovo() {

        limparFormularioCompleto();

        setMessage("");
    }

    @FXML
    private void onSalvar() {

        AgendaFormData formData = formMapper.fromForm(
                dpData,
                txtHoraInicio,
                txtHoraFim,
                cbProfissional,
                cbPaciente,
                cbSala,
                txtProcedimento,
                txtObservacoes,
                obterProfissionalSelecionado(),
                obterPacienteSelecionado()
        );

        boolean salvou = actionManager.salvar(formData);

        if (salvou) {

            txtProcedimento.setText(formData.getProcedimento());

            limparFormulario();

            carregarAgendaDoDia();
        }
    }

    @FXML
    private void onIniciarAtendimento() {

        Agendamento selected = selectionManager.obterSelecionado(tbAgenda);

        boolean valido = selectionManager.validarSelecao(
                selected,
                this::setMessage,
                "Selecione um agendamento para iniciar."
        );

        if (!valido) {
            return;
        }

        try {

            navigationManager.abrirAtendimento(selected);

            carregarAgendaDoDia();

        } catch (Exception e) {

            e.printStackTrace();

            setMessage("Erro ao iniciar atendimento: " + e.getMessage());
        }
    }

    @FXML
    private void onFinalizarConsulta() {

        Agendamento selected = selectionManager.obterSelecionado(tbAgenda);

        boolean valido = selectionManager.validarSelecao(
                selected,
                this::setMessage,
                "Selecione um agendamento para finalizar."
        );

        if (!valido) {
            return;
        }

        actionManager.finalizarConsulta(selected);

        carregarAgendaDoDia();
    }

    @FXML
    private void onAtualizarLista() {
        refreshManager.atualizarLista(this::carregarAgendaDoDia);
    }

    @FXML
    private void onDataAlterada() {

        dateChangeManager.onDataAlterada(
                dpData,
                tbAgenda,
                dateRuleManager,
                this::setMessage,
                this::carregarAgendaDoDia
        );
    }

    private void carregarAgendaDoDia() {

        LocalDate data = dpData.getValue();

        if (data == null) {

            tableManager.clear(tbAgenda);

            setMessage("Selecione uma data para visualizar a agenda.");

            return;
        }

        loadManager.carregarAsync(
                () -> actionManager.carregarAgendaDoDia(
                        data,
                        cbProfissional.getValue(),
                        accessPolicy.podeVerTodos(Session.getUsuario()),
                        Session.getUsuario()
                ),
                tbAgenda,
                agendamentos,
                this::setMessage
        );
    }

    private void preencherFormulario(Agendamento agendamento) {

        formManager.preencherFormulario(
                formMapper,
                agendamento,
                dpData,
                txtHoraInicio,
                txtHoraFim,
                cbProfissional,
                cbPaciente,
                cbSala,
                txtProcedimento,
                txtObservacoes
        );
    }

    private void limparFormularioCompleto() {

        formManager.limparFormularioCompleto(
                formMapper,
                dpData,
                txtHoraInicio,
                txtHoraFim,
                txtProcedimento,
                txtObservacoes,
                cbPaciente,
                cbSala,
                tbAgenda,
                tableManager
        );
    }

    private void limparFormulario() {

        formManager.limparFormulario(
                formMapper,
                txtHoraInicio,
                txtHoraFim,
                txtProcedimento,
                txtObservacoes,
                cbPaciente,
                cbSala
        );
    }

    private Usuario obterProfissionalSelecionado() {

        return formManager.obterProfissionalSelecionado(
                cbProfissional,
                profissionalComboConfigurer,
                displayFormatter
        );
    }

    private Paciente obterPacienteSelecionado() {

        return formManager.obterPacienteSelecionado(
                cbPaciente,
                pacienteComboConfigurer
        );
    }

    private void setMessage(String message) {
        messageManager.setMessage(lblMensagem, message);
    }
}