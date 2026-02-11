package br.com.clinica.controller;

import br.com.clinica.auth.Perfis;
import br.com.clinica.dao.AgendamentoDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.model.enums.SalaAtendimento;
import br.com.clinica.model.enums.StatusAgendamento;
import br.com.clinica.session.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AgendaController {

    private static final DateTimeFormatter HORA_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    private final AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();

    @FXML private DatePicker dpData;
    @FXML private ComboBox<Usuario> cbProfissional;
    @FXML private ComboBox<Paciente> cbPaciente;
    @FXML private ComboBox<SalaAtendimento> cbSala;
    @FXML private TextField txtHoraInicio;
    @FXML private TextField txtHoraFim;
    @FXML private TextField txtProcedimento;
    @FXML private TextArea txtObservacoes;
    @FXML private Label lblMensagem;

    @FXML private TableView<Agendamento> tbAgenda;
    @FXML private TableColumn<Agendamento, String> colHora;
    @FXML private TableColumn<Agendamento, String> colProfissional;
    @FXML private TableColumn<Agendamento, String> colSala;
    @FXML private TableColumn<Agendamento, String> colPaciente;
    @FXML private TableColumn<Agendamento, String> colStatus;

    @FXML
    private void initialize() {
        dpData.setValue(LocalDate.now());

        // Profissionais (usuários ativos filtrados no DAO)
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

        aplicarRegraDeVisaoPorPerfil();
        carregarAgendaDoDia();
    }

    private boolean podeVerTodos() {
        Usuario logado = Session.getUsuario();
        if (logado == null || logado.getPerfil() == null || logado.getPerfil().getNome() == null) {
            return true; // fallback
        }

        String perfil = logado.getPerfil().getNome();
        return Perfis.ADMIN.equals(perfil) || Perfis.RECEPCIONISTA.equals(perfil);
    }

    private void aplicarRegraDeVisaoPorPerfil() {
        if (!podeVerTodos()) {
            // Profissional: trava no usuário logado
            Usuario logado = Session.getUsuario();
            if (logado != null) {
                cbProfissional.setItems(FXCollections.observableArrayList(logado));
                cbProfissional.getSelectionModel().select(logado);
                cbProfissional.setDisable(true);
            }
        } else {
            // Admin/Recep: pode escolher (ou deixar vazio pra ver todos)
            cbProfissional.setDisable(false);
            cbProfissional.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void onNovo() {
        lblMensagem.setText("");
        limparFormulario();
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

            if (!horaFim.isAfter(horaInicio)) {
                lblMensagem.setText("Hora fim deve ser depois da hora início.");
                return;
            }

            SalaAtendimento sala = cbSala.getValue();
            if (sala == null) {
                lblMensagem.setText("Selecione a sala.");
                return;
            }

            Paciente paciente = cbPaciente.getValue();

            // Regra: profissional só agenda para ele mesmo
            Usuario profissional;
            if (podeVerTodos()) {
                profissional = cbProfissional.getValue();
            } else {
                profissional = Session.getUsuario();
            }

            if (profissional == null) {
                lblMensagem.setText("Selecione o profissional.");
                return;
            }

            String nomeProf = (profissional.getPessoaNome() != null && !profissional.getPessoaNome().isBlank())
                    ? profissional.getPessoaNome()
                    : profissional.getNome();

            Agendamento ag = new Agendamento();
            ag.setData(data);
            ag.setHoraInicio(horaInicio);
            ag.setHoraFim(horaFim);
            ag.setProfissionalId(profissional.getId());
            ag.setProfissionalNome(nomeProf);
            ag.setSala(sala);
            ag.setProcedimento(txtProcedimento.getText() != null ? txtProcedimento.getText().trim() : "");
            ag.setObservacoes(txtObservacoes.getText() != null ? txtObservacoes.getText().trim() : "");
            ag.setStatus(StatusAgendamento.AGENDADO);

            if (paciente != null) {
                ag.setPacienteId(Math.toIntExact(paciente.getId()));
                ag.setPacienteNome(paciente.getNome());
            }

            if (agendamentoDAO.existeConflito(ag)) {
                lblMensagem.setText("Conflito de agenda: profissional, sala ou paciente já possuem agendamento neste horário.");
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

    private void carregarAgendaDoDia() {
        LocalDate data = dpData.getValue();
        if (data == null) {
            data = LocalDate.now();
            dpData.setValue(data);
        }

        List<Agendamento> lista;

        if (podeVerTodos()) {
            // Admin/Recep: vê todos (opcional: se escolher alguém, filtra)
            Usuario escolhido = cbProfissional.getValue();
            if (escolhido != null) {
                lista = agendamentoDAO.listarPorDataEProfissional(data, escolhido.getId());
            } else {
                lista = agendamentoDAO.listarPorData(data);
            }
        } else {
            // Profissional: só os dele
            Usuario logado = Session.getUsuario();
            if (logado == null) {
                lista = agendamentoDAO.listarPorData(data); // fallback
            } else {
                lista = agendamentoDAO.listarPorDataEProfissional(data, logado.getId());
            }
        }

        tbAgenda.setItems(FXCollections.observableArrayList(lista));
    }

    @FXML
    private void onIniciarAtendimento() {
        Agendamento selecionado = tbAgenda.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            lblMensagem.setText("Selecione um agendamento para iniciar o atendimento.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/anamnese-view.fxml")
            );
            Parent root = loader.load();

            AnamneseController controller = loader.getController();
            controller.setAgendamento(selecionado);

            Stage stage = new Stage();
            stage.setTitle("Anamnese / Evolução");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            carregarAgendaDoDia();

        } catch (Exception e) {
            e.printStackTrace();
            lblMensagem.setText("Erro ao abrir a tela de anamnese.");
        }
    }

    private void limparFormulario() {
        txtHoraInicio.clear();
        txtHoraFim.clear();
        txtProcedimento.clear();
        txtObservacoes.clear();
        cbPaciente.getSelectionModel().clearSelection();
    }
}