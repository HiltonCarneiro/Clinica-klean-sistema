package br.com.clinica.controller.agenda;

import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.model.enums.SalaAtendimento;
import br.com.clinica.service.AgendaAccessPolicy;
import br.com.clinica.service.AgendaService;
import br.com.clinica.session.Session;
import javafx.collections.FXCollections;
import javafx.scene.control.*;

import java.util.function.Consumer;

public class AgendaInitialSetupManager {

    public void configure(
            DatePicker dpData,
            ComboBox<Usuario> cbProfissional,
            ComboBox<Paciente> cbPaciente,
            ComboBox<SalaAtendimento> cbSala,
            TextField txtHoraInicio,
            TextField txtHoraFim,
            TextField txtProcedimento,
            Button btnFinalizarConsulta,
            TableView<Agendamento> tbAgenda,
            TableColumn<Agendamento, String> colHora,
            TableColumn<Agendamento, String> colProfissional,
            TableColumn<Agendamento, String> colSala,
            TableColumn<Agendamento, String> colPaciente,
            TableColumn<Agendamento, String> colStatus,
            AgendaService agendaService,
            AgendaAccessPolicy accessPolicy,
            AgendaDisplayFormatter displayFormatter,
            AgendaDatePickerConfigurer datePickerConfigurer,
            AgendaTableManager tableManager,
            AgendaComboBoxConfigurer<Usuario> profissionalComboConfigurer,
            AgendaComboBoxConfigurer<Paciente> pacienteComboConfigurer,
            AgendaProcedureSuggestionManager procedureSuggestionManager,
            Consumer<String> messageConsumer,
            Runnable carregarAgendaDoDia,
            Consumer<Agendamento> preencherFormulario
    ) {
        configureDatePicker(
                dpData,
                datePickerConfigurer,
                messageConsumer,
                carregarAgendaDoDia
        );

        configureComboBoxes(
                cbProfissional,
                cbPaciente,
                cbSala,
                agendaService,
                displayFormatter,
                profissionalComboConfigurer,
                pacienteComboConfigurer
        );

        configureFields(
                txtHoraInicio,
                txtHoraFim,
                txtProcedimento,
                cbSala,
                procedureSuggestionManager
        );

        configureTable(
                tbAgenda,
                colHora,
                colProfissional,
                colSala,
                colPaciente,
                colStatus,
                tableManager,
                preencherFormulario
        );

        configureProfileRules(
                cbProfissional,
                btnFinalizarConsulta,
                accessPolicy
        );
    }

    private void configureDatePicker(
            DatePicker dpData,
            AgendaDatePickerConfigurer datePickerConfigurer,
            Consumer<String> messageConsumer,
            Runnable carregarAgendaDoDia
    ) {
        datePickerConfigurer.configure(
                dpData,
                messageConsumer,
                carregarAgendaDoDia
        );
    }

    private void configureComboBoxes(
            ComboBox<Usuario> cbProfissional,
            ComboBox<Paciente> cbPaciente,
            ComboBox<SalaAtendimento> cbSala,
            AgendaService agendaService,
            AgendaDisplayFormatter displayFormatter,
            AgendaComboBoxConfigurer<Usuario> profissionalComboConfigurer,
            AgendaComboBoxConfigurer<Paciente> pacienteComboConfigurer
    ) {
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

        cbSala.setItems(
                FXCollections.observableArrayList(SalaAtendimento.values())
        );
    }

    private void configureFields(
            TextField txtHoraInicio,
            TextField txtHoraFim,
            TextField txtProcedimento,
            ComboBox<SalaAtendimento> cbSala,
            AgendaProcedureSuggestionManager procedureSuggestionManager
    ) {
        txtHoraInicio.setTextFormatter(AgendaTextFormatters.horaFormatter());
        txtHoraFim.setTextFormatter(AgendaTextFormatters.horaFormatter());

        procedureSuggestionManager.configure(txtProcedimento, cbSala);
    }

    private void configureTable(
            TableView<Agendamento> tbAgenda,
            TableColumn<Agendamento, String> colHora,
            TableColumn<Agendamento, String> colProfissional,
            TableColumn<Agendamento, String> colSala,
            TableColumn<Agendamento, String> colPaciente,
            TableColumn<Agendamento, String> colStatus,
            AgendaTableManager tableManager,
            Consumer<Agendamento> preencherFormulario
    ) {
        tableManager.configure(
                tbAgenda,
                colHora,
                colProfissional,
                colSala,
                colPaciente,
                colStatus,
                preencherFormulario
        );
    }

    private void configureProfileRules(
            ComboBox<Usuario> cbProfissional,
            Button btnFinalizarConsulta,
            AgendaAccessPolicy accessPolicy
    ) {
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
}
