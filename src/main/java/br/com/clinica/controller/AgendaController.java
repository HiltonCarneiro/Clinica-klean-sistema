package br.com.clinica.controller;

import br.com.clinica.auth.Perfis;
import br.com.clinica.dao.AgendamentoDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.ProcedimentoFrequenteDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.model.enums.SalaAtendimento;
import br.com.clinica.model.enums.StatusAgendamento;
import br.com.clinica.session.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AgendaController {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", LOCALE_PT_BR);

    // ✅ Regras fixas da clínica
    private static final LocalTime HORA_ABERTURA = LocalTime.of(7, 0);
    private static final LocalTime HORA_FECHAMENTO = LocalTime.of(19, 0);
    private static final LocalTime ALMOCO_INICIO = LocalTime.of(12, 0);
    private static final LocalTime ALMOCO_FIM = LocalTime.of(13, 0);

    private final AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final ProcedimentoFrequenteDAO procedimentoDAO = new ProcedimentoFrequenteDAO();

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

    private final ContextMenu sugestoesMenu = new ContextMenu();

    @FXML
    private void initialize() {
        dpData.setValue(LocalDate.now());

        cbProfissional.setItems(FXCollections.observableArrayList(usuarioDAO.listarProfissionaisAtivos()));
        cbPaciente.setItems(FXCollections.observableArrayList(pacienteDAO.listarTodos()));
        cbSala.setItems(FXCollections.observableArrayList(SalaAtendimento.values()));

        configurarMascarasHorario();
        configurarSugestaoProcedimento();

        colHora.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getHoraInicio().format(HORA_FORMATTER) +
                                " - " +
                                c.getValue().getHoraFim().format(HORA_FORMATTER)
                )
        );

        colProfissional.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getProfissionalNome())
        );

        colSala.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getSala() != null ? c.getValue().getSala().getDescricao() : ""
                )
        );

        colPaciente.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getPacienteNome() != null ? c.getValue().getPacienteNome() : ""
                )
        );

        colStatus.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getStatus() != null ? c.getValue().getStatus().name() : ""
                )
        );

        aplicarRegraDeVisaoPorPerfil();
        carregarAgendaDoDia();
    }

    // ==========================
    // MÁSCARAS DE HORÁRIO
    // ==========================
    private void configurarMascarasHorario() {
        txtHoraInicio.setTextFormatter(criarTextFormatterHora());
        txtHoraFim.setTextFormatter(criarTextFormatterHora());

        txtHoraInicio.textProperty().addListener((obs, oldV, newV) -> {
            String f = autoFormatarHora(oldV, newV);
            if (!f.equals(newV)) txtHoraInicio.setText(f);
        });
        txtHoraFim.textProperty().addListener((obs, oldV, newV) -> {
            String f = autoFormatarHora(oldV, newV);
            if (!f.equals(newV)) txtHoraFim.setText(f);
        });

        txtHoraInicio.focusedProperty().addListener((obs, was, is) -> { if (!is) validarHoraCampo(txtHoraInicio); });
        txtHoraFim.focusedProperty().addListener((obs, was, is) -> { if (!is) validarHoraCampo(txtHoraFim); });
    }

    private TextFormatter<String> criarTextFormatterHora() {
        return new TextFormatter<>(change -> {
            String novo = change.getControlNewText();
            if (novo == null) return change;

            if (!novo.matches("[0-9:]*")) return null;
            if (novo.length() > 5) return null;

            int idx = novo.indexOf(':');
            if (idx >= 0 && idx != 2) return null;
            if (novo.indexOf(':', idx + 1) >= 0) return null;

            return change;
        });
    }

    private String autoFormatarHora(String oldV, String newV) {
        if (newV == null) return "";
        String v = newV.trim();

        if (oldV != null && v.length() < oldV.length()) return v;

        if (v.length() == 4 && v.matches("\\d{4}")) return v.substring(0, 2) + ":" + v.substring(2);
        if (v.length() == 2 && v.matches("\\d{2}") && !v.contains(":")) return v + ":";

        return v;
    }

    private LocalTime parseHora(String texto) {
        if (texto == null) return null;
        String t = texto.trim();
        if (t.isEmpty()) return null;

        if (t.matches("\\d{1}:\\d{2}")) t = "0" + t;
        if (!t.matches("\\d{2}:\\d{2}")) return null;

        try {
            return LocalTime.parse(t, HORA_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private void validarHoraCampo(TextField field) {
        String v = field.getText();
        if (v == null || v.isBlank()) return;

        LocalTime lt = parseHora(v);
        if (lt == null) {
            field.setText("");
            lblMensagem.setText("Hora inválida. Use HH:mm (ex: 08:00).");
        } else {
            field.setText(lt.format(HORA_FORMATTER));
        }
    }

    // ✅ Validação central de horários permitidos
    private boolean horarioPermitido(LocalTime inicio, LocalTime fim) {
        // dentro de 07:00 a 19:00
        if (inicio.isBefore(HORA_ABERTURA)) return false;
        if (fim.isAfter(HORA_FECHAMENTO)) return false;

        // não pode interseccionar almoço [12:00, 13:00)
        boolean intersectaAlmoco = inicio.isBefore(ALMOCO_FIM) && fim.isAfter(ALMOCO_INICIO);
        return !intersectaAlmoco;
    }

    // ==========================
    // SUGESTÕES DO PROCEDIMENTO
    // ==========================
    private void configurarSugestaoProcedimento() {
        sugestoesMenu.setAutoHide(true);

        cbSala.valueProperty().addListener((obs, old, novaSala) -> atualizarSugestoes());
        txtProcedimento.textProperty().addListener((obs, old, txt) -> atualizarSugestoes());

        txtProcedimento.setOnAction(e -> {
            String norm = procedimentoDAO.normalizar(txtProcedimento.getText());
            txtProcedimento.setText(norm);
            sugestoesMenu.hide();
        });

        txtProcedimento.focusedProperty().addListener((obs, old, focado) -> {
            if (!Boolean.TRUE.equals(focado)) {
                String norm = procedimentoDAO.normalizar(txtProcedimento.getText());
                txtProcedimento.setText(norm);
                sugestoesMenu.hide();
            }
        });
    }

    private void atualizarSugestoes() {
        String digitado = txtProcedimento.getText() == null ? "" : txtProcedimento.getText().trim();

        if (digitado.isEmpty()) {
            sugestoesMenu.hide();
            return;
        }

        SalaAtendimento sala = cbSala.getValue();
        List<String> sugestoes = procedimentoDAO.sugerir(digitado, sala, 8);

        if (sugestoes.isEmpty()) {
            sugestoesMenu.hide();
            return;
        }

        sugestoesMenu.getItems().clear();
        for (String s : sugestoes) {
            MenuItem mi = new MenuItem(s);
            mi.setOnAction(e -> {
                txtProcedimento.setText(s);
                txtProcedimento.positionCaret(s.length());
                sugestoesMenu.hide();
            });
            sugestoesMenu.getItems().add(mi);
        }

        if (!sugestoesMenu.isShowing()) {
            Platform.runLater(() -> sugestoesMenu.show(txtProcedimento, Side.BOTTOM, 0, 0));
        }
    }

    // ==========================
    // PERFIL
    // ==========================
    private boolean podeVerTodos() {
        Usuario logado = Session.getUsuario();
        if (logado == null || logado.getPerfil() == null || logado.getPerfil().getNome() == null) return true;

        String perfil = logado.getPerfil().getNome();
        return Perfis.ADMIN.equals(perfil) || Perfis.RECEPCIONISTA.equals(perfil);
    }

    private void aplicarRegraDeVisaoPorPerfil() {
        if (!podeVerTodos()) {
            Usuario logado = Session.getUsuario();
            if (logado != null) {
                cbProfissional.setItems(FXCollections.observableArrayList(logado));
                cbProfissional.getSelectionModel().select(logado);
                cbProfissional.setDisable(true);
            }
        } else {
            cbProfissional.setDisable(false);
            cbProfissional.getSelectionModel().clearSelection();
        }
    }

    // ==========================
    // AÇÕES
    // ==========================
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

            LocalTime horaInicio = parseHora(txtHoraInicio.getText());
            LocalTime horaFim = parseHora(txtHoraFim.getText());

            if (horaInicio == null || horaFim == null) {
                lblMensagem.setText("Informe hora início e hora fim em HH:mm (ex: 08:00).");
                return;
            }
            if (!horaFim.isAfter(horaInicio)) {
                lblMensagem.setText("Hora fim deve ser depois da hora início.");
                return;
            }

            // ✅ aplica regra de horário + almoço
            if (!horarioPermitido(horaInicio, horaFim)) {
                lblMensagem.setText("Horário inválido: permitido 07:00–19:00 e almoço 12:00–13:00.");
                return;
            }

            SalaAtendimento sala = cbSala.getValue();
            if (sala == null) {
                lblMensagem.setText("Selecione a sala.");
                return;
            }

            Paciente paciente = cbPaciente.getValue();

            Usuario profissional = podeVerTodos() ? cbProfissional.getValue() : Session.getUsuario();
            if (profissional == null) {
                lblMensagem.setText("Selecione o profissional.");
                return;
            }

            String nomeProf = (profissional.getPessoaNome() != null && !profissional.getPessoaNome().isBlank())
                    ? profissional.getPessoaNome()
                    : profissional.getNome();

            String procedimentoNorm = procedimentoDAO.normalizar(txtProcedimento.getText());
            txtProcedimento.setText(procedimentoNorm);

            Agendamento ag = new Agendamento();
            ag.setData(data);
            ag.setHoraInicio(horaInicio);
            ag.setHoraFim(horaFim);
            ag.setProfissionalId(profissional.getId());
            ag.setProfissionalNome(nomeProf);
            ag.setSala(sala);
            ag.setProcedimento(procedimentoNorm);
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
            procedimentoDAO.registrarUso(procedimentoNorm, sala);

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
            Usuario escolhido = cbProfissional.getValue();
            lista = (escolhido != null)
                    ? agendamentoDAO.listarPorDataEProfissional(data, escolhido.getId())
                    : agendamentoDAO.listarPorData(data);
        } else {
            Usuario logado = Session.getUsuario();
            lista = (logado != null)
                    ? agendamentoDAO.listarPorDataEProfissional(data, logado.getId())
                    : agendamentoDAO.listarPorData(data);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/anamnese-view.fxml"));
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