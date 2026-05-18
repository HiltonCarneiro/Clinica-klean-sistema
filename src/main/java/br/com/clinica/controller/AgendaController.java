package br.com.clinica.controller;

import br.com.clinica.controller.agenda.AgendaComboBoxConfigurer;
import br.com.clinica.controller.agenda.AgendaDatePickerConfigurer;
import br.com.clinica.controller.agenda.AgendaDisplayFormatter;
import br.com.clinica.controller.agenda.AgendaProcedureSuggestionManager;
import br.com.clinica.controller.agenda.AgendaTableManager;
import br.com.clinica.controller.agenda.AgendaTextFormatters;
import br.com.clinica.dto.AgendaFormData;
import br.com.clinica.exception.BusinessException;
import br.com.clinica.mapper.AgendaFormMapper;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.model.enums.SalaAtendimento;
import br.com.clinica.service.AgendaAccessPolicy;
import br.com.clinica.service.AgendaService;
import br.com.clinica.session.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

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

    private final AgendaComboBoxConfigurer<Usuario> profissionalComboConfigurer =
            new AgendaComboBoxConfigurer<>();

    private final AgendaComboBoxConfigurer<Paciente> pacienteComboConfigurer =
            new AgendaComboBoxConfigurer<>();

    private final AgendaProcedureSuggestionManager procedureSuggestionManager =
            new AgendaProcedureSuggestionManager(agendaService);

    @FXML
    public void initialize() {
        configureDatePicker();
        configureComboBoxes();
        configureFields();
        configureTable();
        configureProfileRules();

        tableManager.clear(tbAgenda);
        setMessage("Selecione uma data para visualizar a agenda.");
    }

    private void configureDatePicker() {
        datePickerConfigurer.configure(
                dpData,
                this::setMessage,
                this::carregarAgendaDoDia
        );
    }

    private void configureComboBoxes() {
        profissionalComboConfigurer.configure(
                cbProfissional,
                agendaService.listarProfissionaisAtivos(),
                displayFormatter::profissional
        );

        pacienteComboConfigurer.configure(
                cbPaciente,
                agendaService.listarPacientes(),
                paciente -> paciente == null || paciente.getNome() == null
                        ? ""
                        : paciente.getNome()
        );

        cbSala.setItems(FXCollections.observableArrayList(SalaAtendimento.values()));
    }

    private void configureFields() {
        txtHoraInicio.setTextFormatter(AgendaTextFormatters.horaFormatter());
        txtHoraFim.setTextFormatter(AgendaTextFormatters.horaFormatter());

        procedureSuggestionManager.configure(txtProcedimento, cbSala);
    }

    private void configureTable() {
        tableManager.configure(
                tbAgenda,
                colHora,
                colProfissional,
                colSala,
                colPaciente,
                colStatus,
                this::preencherFormulario
        );
    }

    private void configureProfileRules() {
        Usuario usuarioLogado = Session.getUsuario();

        if (!accessPolicy.podeVerTodos(usuarioLogado)) {
            if (usuarioLogado != null) {
                cbProfissional.setValue(usuarioLogado);
            }

            cbProfissional.setDisable(true);
        }

        if (btnFinalizarConsulta != null) {
            boolean canFinish = accessPolicy.podeFinalizarConsulta(usuarioLogado);

            btnFinalizarConsulta.setVisible(canFinish);
            btnFinalizarConsulta.setManaged(canFinish);
        }
    }

    @FXML
    private void onNovo() {
        limparFormularioCompleto();
        setMessage("");
    }

    @FXML
    private void onSalvar() {
        try {
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

            agendaService.salvar(formData);

            txtProcedimento.setText(formData.getProcedimento());

            setMessage("Agendamento salvo com sucesso.");
            limparFormulario();
            carregarAgendaDoDia();

        } catch (BusinessException e) {
            setMessage(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            setMessage("Erro ao salvar agendamento: " + e.getMessage());
        }
    }

    @FXML
    private void onIniciarAtendimento() {
        Agendamento selected = tableManager.getSelected(tbAgenda);

        if (selected == null) {
            setMessage("Selecione um agendamento para iniciar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/anamnese-view.fxml")
            );

            Parent root = loader.load();

            AnamneseController controller = loader.getController();
            controller.setAgendamento(selected);

            Stage stage = new Stage();
            stage.setTitle("Anamnese / Evolução");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            carregarAgendaDoDia();

        } catch (Exception e) {
            e.printStackTrace();
            setMessage("Erro ao iniciar atendimento: " + e.getMessage());
        }
    }

    @FXML
    private void onFinalizarConsulta() {
        try {
            Agendamento selected = tableManager.getSelected(tbAgenda);
            agendaService.finalizarConsulta(selected);

            setMessage("Consulta finalizada.");
            carregarAgendaDoDia();

        } catch (BusinessException e) {
            setMessage(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            setMessage("Erro ao finalizar consulta: " + e.getMessage());
        }
    }

    @FXML
    private void onAtualizarLista() {
        carregarAgendaDoDia();
    }

    @FXML
    private void onDataAlterada() {
        LocalDate data = dpData.getValue();

        if (data == null) {
            return;
        }

        if (data.isBefore(LocalDate.now())) {
            setMessage("Não é permitido selecionar data no passado.");
            dpData.setValue(null);
            dpData.getEditor().clear();
            tableManager.clear(tbAgenda);
            return;
        }

        carregarAgendaDoDia();
    }

    private void carregarAgendaDoDia() {
        try {
            LocalDate data = dpData.getValue();

            if (data == null) {
                tableManager.clear(tbAgenda);
                setMessage("Selecione uma data para visualizar a agenda.");
                return;
            }

            List<Agendamento> agendamentos = agendaService.listarAgenda(
                    data,
                    cbProfissional.getValue(),
                    accessPolicy.podeVerTodos(Session.getUsuario()),
                    Session.getUsuario()
            );

            tableManager.setItems(tbAgenda, agendamentos);
            setMessage("Agenda carregada.");

        } catch (BusinessException e) {
            tableManager.clear(tbAgenda);
            setMessage(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            tableManager.clear(tbAgenda);
            setMessage("Erro ao carregar agenda: " + e.getMessage());
        }
    }

    private void preencherFormulario(Agendamento agendamento) {
        if (agendamento == null) {
            return;
        }

        formMapper.fillForm(
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
        limparFormulario();

        dpData.setValue(null);

        if (dpData.getEditor() != null) {
            dpData.getEditor().clear();
        }

        tableManager.clear(tbAgenda);
    }

    private void limparFormulario() {
        formMapper.clearForm(
                txtHoraInicio,
                txtHoraFim,
                txtProcedimento,
                txtObservacoes,
                cbPaciente,
                cbSala
        );
    }

    private Usuario obterProfissionalSelecionado() {
        return profissionalComboConfigurer.resolveSelectedItem(
                cbProfissional,
                displayFormatter::profissional
        );
    }

    private Paciente obterPacienteSelecionado() {
        return pacienteComboConfigurer.resolveSelectedItem(
                cbPaciente,
                paciente -> paciente == null || paciente.getNome() == null
                        ? ""
                        : paciente.getNome()
        );
    }

    private void setMessage(String message) {
        if (lblMensagem != null) {
            lblMensagem.setText(message == null ? "" : message);
        }
    }
}