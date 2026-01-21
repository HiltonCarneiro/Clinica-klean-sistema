package br.com.clinica.controller;

import br.com.clinica.dao.AgendamentoDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.model.enums.SalaAtendimento;
import br.com.clinica.model.enums.StatusAgendamento;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AgendaController {

    private static final DateTimeFormatter HORA_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    private final AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();

    // ==== Campos da tela ====

    @FXML
    private DatePicker dpData;

    @FXML
    private ComboBox<Usuario> cbProfissional;

    @FXML
    private ComboBox<Paciente> cbPaciente;

    @FXML
    private ComboBox<SalaAtendimento> cbSala;

    @FXML
    private TextField txtHoraInicio;

    @FXML
    private TextField txtHoraFim;

    @FXML
    private TextField txtProcedimento;

    @FXML
    private TextArea txtObservacoes;

    @FXML
    private Label lblMensagem;

    @FXML
    private TableView<Agendamento> tbAgenda;

    @FXML
    private TableColumn<Agendamento, String> colHora;

    @FXML
    private TableColumn<Agendamento, String> colProfissional;

    @FXML
    private TableColumn<Agendamento, String> colSala;

    @FXML
    private TableColumn<Agendamento, String> colPaciente;

    @FXML
    private TableColumn<Agendamento, String> colStatus;

    // ==== Inicialização ====

    @FXML
    private void initialize() {
        dpData.setValue(LocalDate.now());

        // Profissionais (usuários ativos)
        List<Usuario> profissionais = usuarioDAO.listarProfissionaisAtivos();
        cbProfissional.setItems(FXCollections.observableArrayList(profissionais));

        // Pacientes
        List<Paciente> pacientes = pacienteDAO.listarTodos();
        cbPaciente.setItems(FXCollections.observableArrayList(pacientes));

        // Salas
        cbSala.setItems(FXCollections.observableArrayList(SalaAtendimento.values()));

        // Colunas da tabela
        colHora.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getHoraInicio().format(HORA_FORMATTER) +
                                " - " +
                                c.getValue().getHoraFim().format(HORA_FORMATTER)
                )
        );

        colProfissional.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getProfissionalNome()
                )
        );

        colSala.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getSala() != null
                                ? c.getValue().getSala().getDescricao()
                                : ""
                )
        );

        colPaciente.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getPacienteNome() != null
                                ? c.getValue().getPacienteNome()
                                : ""
                )
        );

        colStatus.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getStatus() != null
                                ? c.getValue().getStatus().name()
                                : ""
                )
        );

        carregarAgendaDoDia();
    }

    // ==== Ações dos botões ====

    // BOTÃO NOVO (era o que tava dando erro no FXML)
    @FXML
    private void onNovo() {
        lblMensagem.setText("");
        limparFormulario();
        // mantém data/profissional/sala para facilitar
    }

    @FXML
    private void onSalvar() {
        try {
            lblMensagem.setText("");

            LocalDate data = dpData.getValue();
            if (data == null) {
                lblMensagem.setText("Informe a data.");
                return;
            }

            String txtIni = txtHoraInicio.getText() != null ? txtHoraInicio.getText().trim() : "";
            String txtFim = txtHoraFim.getText() != null ? txtHoraFim.getText().trim() : "";

            if (txtIni.isBlank() || txtFim.isBlank()) {
                lblMensagem.setText("Informe hora início e hora fim.");
                return;
            }

            LocalTime horaInicio = LocalTime.parse(txtIni, HORA_FORMATTER);
            LocalTime horaFim = LocalTime.parse(txtFim, HORA_FORMATTER);

            Usuario profissional = cbProfissional.getValue();
            SalaAtendimento sala = cbSala.getValue();
            Paciente paciente = cbPaciente.getValue();

            if (profissional == null || sala == null) {
                lblMensagem.setText("Selecione o profissional e a sala.");
                return;
            }

            if (!horaFim.isAfter(horaInicio)) {
                lblMensagem.setText("Hora fim deve ser depois da hora início.");
                return;
            }

            Agendamento ag = new Agendamento();
            ag.setData(data);
            ag.setHoraInicio(horaInicio);
            ag.setHoraFim(horaFim);
            ag.setProfissionalId(profissional.getId().intValue());
            ag.setProfissionalNome(profissional.getNome());
            ag.setSala(sala);
            ag.setProcedimento(txtProcedimento.getText() != null
                    ? txtProcedimento.getText().trim()
                    : "");
            ag.setObservacoes(txtObservacoes.getText() != null
                    ? txtObservacoes.getText().trim()
                    : "");
            ag.setStatus(StatusAgendamento.AGENDADO);

            if (paciente != null) {
                ag.setPacienteId(paciente.getId().intValue());
                ag.setPacienteNome(paciente.getNome());
            }

            // Regra de conflito (profissional, sala, paciente)
            if (agendamentoDAO.existeConflito(ag)) {
                lblMensagem.setText(
                        "Conflito de agenda: profissional, sala ou paciente já possuem agendamento neste horário."
                );
                return;
            }

            agendamentoDAO.salvar(ag);
            lblMensagem.setText("Agendamento salvo com sucesso.");

            limparFormulario();
            carregarAgendaDoDia();

        } catch (Exception e) {
            e.printStackTrace();
            lblMensagem.setText("Erro ao salvar agendamento: " + e.getMessage());
        }
    }

    @FXML
    private void onAtualizarLista() {
        carregarAgendaDoDia();
    }

    @FXML
    private void onDataAlterada() {
        carregarAgendaDoDia();
    }

    // ==== Métodos auxiliares ====

    private void carregarAgendaDoDia() {
        LocalDate data = dpData.getValue();
        if (data == null) {
            data = LocalDate.now();
            dpData.setValue(data);
        }

        List<Agendamento> lista = agendamentoDAO.listarPorData(data);
        tbAgenda.setItems(FXCollections.observableArrayList(lista));
    }

    private void limparFormulario() {
        txtHoraInicio.clear();
        txtHoraFim.clear();
        txtProcedimento.clear();
        txtObservacoes.clear();
        cbPaciente.getSelectionModel().clearSelection();
        // deixa data / profissional / sala selecionados
    }
}
