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
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AgendaController {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", LOCALE_PT_BR);
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_PT_BR);

    // Regras fixas
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
    @FXML private Button btnFinalizarConsulta;

    @FXML private TableView<Agendamento> tbAgenda;
    @FXML private TableColumn<Agendamento, String> colHora;
    @FXML private TableColumn<Agendamento, String> colProfissional;
    @FXML private TableColumn<Agendamento, String> colSala;
    @FXML private TableColumn<Agendamento, String> colPaciente;
    @FXML private TableColumn<Agendamento, String> colStatus;

    private final ContextMenu menuSugestoes = new ContextMenu();

    private LocalDate ultimaDataValida = null;

    @FXML
    public void initialize() {
        dpData.setValue(null);
        dpData.getEditor().clear();
        dpData.setEditable(true);
        configurarDatePickerData();

        cbProfissional.setItems(FXCollections.observableArrayList(usuarioDAO.listarProfissionaisAtivos()));
        configurarPesquisaProfissional();

        cbPaciente.setItems(FXCollections.observableArrayList(pacienteDAO.listarTodos()));
        configurarPesquisaPaciente(); // ✅ NOVO: pesquisa no paciente

        cbSala.setItems(FXCollections.observableArrayList(SalaAtendimento.values()));

        txtHoraInicio.setTextFormatter(criarTextFormatterHora());
        txtHoraFim.setTextFormatter(criarTextFormatterHora());

        configurarSugestoesProcedimento();

        colHora.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        (c.getValue().getHoraInicio() != null ? c.getValue().getHoraInicio().format(HORA_FORMATTER) : "") +
                                " - " +
                                (c.getValue().getHoraFim() != null ? c.getValue().getHoraFim().format(HORA_FORMATTER) : "")
                )
        );
        colProfissional.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(safe(c.getValue().getProfissionalNome()))
        );
        colSala.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getSala() != null ? c.getValue().getSala().getDescricao() : ""
                )
        );
        colPaciente.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(safe(c.getValue().getPacienteNome()))
        );
        colStatus.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getStatus() != null ? c.getValue().getStatus().name() : ""
                )
        );

        tbAgenda.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) preencherFormulario(sel);
        });

        aplicarRegraDeVisaoPorPerfil();

        tbAgenda.getItems().clear();
        setMsg("Selecione uma data para visualizar a agenda.");
    }

    // =========================
    // DatePicker: máscara + bloqueio passado
    // =========================
    private void configurarDatePickerData() {
        dpData.getEditor().setPromptText("dd/MM/aaaa");

        dpData.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date.format(DATA_BR);
            }

            @Override
            public LocalDate fromString(String str) {
                if (str == null) return null;
                String s = str.trim();
                if (s.isEmpty()) return null;
                try {
                    return LocalDate.parse(s, DATA_BR);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        dpData.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) return;
                setDisable(item.isBefore(LocalDate.now()));
            }
        });

        dpData.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                ultimaDataValida = val;
                if (!dpData.getEditor().isFocused()) {
                    dpData.getEditor().setText(dpData.getConverter().toString(val));
                }
            }
        });

        dpData.getEditor().setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.isContentChange()) return change;

            String newText = change.getControlNewText();
            if (newText == null) return change;

            String rawDigits = newText.replaceAll("\\D", "");
            if (rawDigits.length() > 8) return null;

            if (rawDigits.isEmpty()) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                change.selectRange(0, 0);
                return change;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rawDigits.length(); i++) {
                sb.append(rawDigits.charAt(i));
                if (i == 1 && rawDigits.length() > 2) sb.append('/');
                if (i == 3 && rawDigits.length() > 4) sb.append('/');
            }
            String formatted = sb.toString();

            int oldLen = change.getControlText() == null ? 0 : change.getControlText().length();
            change.setText(formatted);
            change.setRange(0, oldLen);
            change.selectRange(formatted.length(), formatted.length());
            return change;
        }));

        dpData.getEditor().focusedProperty().addListener((obs, old, focused) -> {
            if (focused) return;

            String texto = dpData.getEditor().getText() == null ? "" : dpData.getEditor().getText().trim();

            if (texto.isEmpty()) {
                dpData.setValue(null);
                return;
            }

            String digits = texto.replaceAll("\\D", "");
            if (digits.length() < 8) {
                setMsg("Digite a data completa (dd/MM/aaaa). ");
                return;
            }

            LocalDate parsed = dpData.getConverter().fromString(texto);
            if (parsed == null) {
                setMsg("Data inválida. Use o formato dd/MM/aaaa.");
                dpData.setValue(null);
                return;
            }

            if (parsed.isBefore(LocalDate.now())) {
                setMsg("❌ Não é permitido selecionar data no passado.");
                dpData.setValue(null);
                Platform.runLater(() -> dpData.getEditor().requestFocus());
                tbAgenda.getItems().clear();
                return;
            }

            dpData.setValue(parsed);
            dpData.getEditor().setText(dpData.getConverter().toString(parsed));
            carregarAgendaDoDia();
        });
    }

    // =========================
    // Pesquisa: Profissional e Paciente
    // =========================
    private void configurarPesquisaProfissional() {
        if (cbProfissional == null) return;

        cbProfissional.setEditable(true);

        var orig = FXCollections.observableArrayList(cbProfissional.getItems());
        FilteredList<Usuario> filtrados = new FilteredList<>(orig, u -> true);
        cbProfissional.setItems(filtrados);

        StringConverter<Usuario> conv = new StringConverter<>() {
            @Override
            public String toString(Usuario u) {
                return u == null ? "" : textoProfissional(u);
            }
            @Override
            public Usuario fromString(String s) {
                return cbProfissional.getValue();
            }
        };

        cbProfissional.setConverter(conv);

        cbProfissional.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : conv.toString(item));
            }
        });
        cbProfissional.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : conv.toString(item));
            }
        });

        TextField editor = cbProfissional.getEditor();

        editor.textProperty().addListener((obs, old, texto) -> {
            Usuario sel = cbProfissional.getSelectionModel().getSelectedItem();
            if (sel != null && conv.toString(sel).equals(texto)) return;

            String t = texto == null ? "" : texto.trim().toLowerCase();

            filtrados.setPredicate(u -> t.isBlank() || textoProfissional(u).toLowerCase().contains(t));

            if (!cbProfissional.isShowing()) cbProfissional.show();
        });

        cbProfissional.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                Platform.runLater(() -> {
                    cbProfissional.getEditor().setText(conv.toString(novo));
                    cbProfissional.hide();
                });
            }
        });

        editor.focusedProperty().addListener((obs, old, focado) -> {
            if (Boolean.TRUE.equals(focado)) return;

            String digitado = editor.getText() == null ? "" : editor.getText().trim();
            if (digitado.isBlank()) {
                cbProfissional.getSelectionModel().clearSelection();
                editor.clear();
                filtrados.setPredicate(u -> true);
                return;
            }

            Usuario exato = null;
            for (Usuario u : orig) {
                if (textoProfissional(u).equalsIgnoreCase(digitado)) {
                    exato = u;
                    break;
                }
            }
            if (exato == null && !filtrados.isEmpty()) exato = filtrados.get(0);

            if (exato != null) {
                cbProfissional.getSelectionModel().select(exato);
                editor.setText(conv.toString(exato));
            }

            filtrados.setPredicate(u -> true);
            cbProfissional.hide();
        });

        cbProfissional.setOnHidden(e -> filtrados.setPredicate(u -> true));
    }

    // ✅ NOVO: Pesquisa no Paciente
    private void configurarPesquisaPaciente() {
        if (cbPaciente == null) return;

        cbPaciente.setEditable(true);

        var orig = FXCollections.observableArrayList(cbPaciente.getItems());
        FilteredList<Paciente> filtrados = new FilteredList<>(orig, p -> true);
        cbPaciente.setItems(filtrados);

        StringConverter<Paciente> conv = new StringConverter<>() {
            @Override
            public String toString(Paciente p) {
                return p == null ? "" : safe(p.getNome());
            }
            @Override
            public Paciente fromString(String s) {
                return cbPaciente.getValue();
            }
        };

        cbPaciente.setConverter(conv);

        cbPaciente.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Paciente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : conv.toString(item));
            }
        });
        cbPaciente.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Paciente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : conv.toString(item));
            }
        });

        TextField editor = cbPaciente.getEditor();

        editor.textProperty().addListener((obs, old, texto) -> {
            Paciente sel = cbPaciente.getSelectionModel().getSelectedItem();
            if (sel != null && conv.toString(sel).equals(texto)) return;

            String t = texto == null ? "" : texto.trim().toLowerCase();

            filtrados.setPredicate(p -> t.isBlank() || safe(p.getNome()).toLowerCase().contains(t));

            if (!cbPaciente.isShowing()) cbPaciente.show();
        });

        cbPaciente.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                Platform.runLater(() -> {
                    cbPaciente.getEditor().setText(conv.toString(novo));
                    cbPaciente.hide();
                });
            }
        });

        editor.focusedProperty().addListener((obs, old, focado) -> {
            if (Boolean.TRUE.equals(focado)) return;

            String digitado = editor.getText() == null ? "" : editor.getText().trim();
            if (digitado.isBlank()) {
                cbPaciente.getSelectionModel().clearSelection();
                editor.clear();
                filtrados.setPredicate(p -> true);
                return;
            }

            Paciente exato = null;
            for (Paciente p : orig) {
                if (safe(p.getNome()).equalsIgnoreCase(digitado)) {
                    exato = p;
                    break;
                }
            }
            if (exato == null && !filtrados.isEmpty()) exato = filtrados.get(0);

            if (exato != null) {
                cbPaciente.getSelectionModel().select(exato);
                editor.setText(conv.toString(exato));
            }

            filtrados.setPredicate(p -> true);
            cbPaciente.hide();
        });

        cbPaciente.setOnHidden(e -> filtrados.setPredicate(p -> true));
    }

    private String textoProfissional(Usuario u) {
        if (u == null) return "";
        String pessoa = safe(u.getPessoaNome());
        String cargo = safe(u.getNome());
        if (pessoa.isBlank() && !safe(u.getLogin()).isBlank()) pessoa = u.getLogin();
        if (cargo.isBlank()) return pessoa;
        return pessoa + " (" + cargo + ")";
    }

    // =========================
    // Sugestões de procedimento
    // =========================
    private void configurarSugestoesProcedimento() {
        txtProcedimento.textProperty().addListener((obs, old, novo) -> {
            menuSugestoes.getItems().clear();

            String termo = (novo == null) ? "" : novo.trim();
            if (termo.length() < 2) {
                menuSugestoes.hide();
                return;
            }

            SalaAtendimento sala = cbSala.getValue();
            List<String> sugestoes = procedimentoDAO.sugerir(termo, sala, 8);

            if (sugestoes == null || sugestoes.isEmpty()) {
                menuSugestoes.hide();
                return;
            }

            for (String s : sugestoes) {
                MenuItem item = new MenuItem(s);
                item.setOnAction(e -> {
                    txtProcedimento.setText(s);
                    txtProcedimento.positionCaret(s.length());
                    menuSugestoes.hide();
                });
                menuSugestoes.getItems().add(item);
            }

            if (!menuSugestoes.isShowing()) {
                menuSugestoes.show(txtProcedimento, Side.BOTTOM, 0, 0);
            }
        });

        txtProcedimento.focusedProperty().addListener((obs, old, foc) -> {
            if (!foc) menuSugestoes.hide();
        });
    }

    // =========================
    // Hora: máscara HH:mm sem prender ':'
    // =========================
    private TextFormatter<String> criarTextFormatterHora() {
        return new TextFormatter<>(change -> {
            if (!change.isContentChange()) return change;

            String txt = change.getControlNewText();
            if (txt == null) return change;
            if (txt.isEmpty()) return change;

            String rawDigits = txt.replaceAll("\\D", "");
            if (rawDigits.length() > 4) return null;

            String formatted;
            if (rawDigits.length() <= 2) {
                formatted = rawDigits;
            } else {
                String hh = rawDigits.substring(0, 2);
                String mm = rawDigits.substring(2);
                formatted = hh + ":" + mm;
            }

            int oldLen = change.getControlText() == null ? 0 : change.getControlText().length();
            change.setText(formatted);
            change.setRange(0, oldLen);
            change.selectRange(formatted.length(), formatted.length());
            return change;
        });
    }

    // =========================
    // PERFIL
    // =========================
    private boolean podeVerTodos() {
        Usuario logado = Session.getUsuario();
        if (logado == null || logado.getPerfil() == null || logado.getPerfil().getNome() == null) return true;

        String perfil = logado.getPerfil().getNome();
        return Perfis.ADMIN.equals(perfil) || Perfis.RECEPCIONISTA.equals(perfil);
    }

    private boolean podeFinalizarConsulta() {
        Usuario logado = Session.getUsuario();
        if (logado == null || logado.getPerfil() == null || logado.getPerfil().getNome() == null) return false;

        String perfil = logado.getPerfil().getNome();
        return Perfis.ADMIN.equals(perfil) || !Perfis.RECEPCIONISTA.equals(perfil);
    }

    private void aplicarRegraDeVisaoPorPerfil() {
        if (!podeVerTodos()) {
            Usuario logado = Session.getUsuario();
            if (logado != null) cbProfissional.setValue(logado);
            cbProfissional.setDisable(true);
        }

        if (btnFinalizarConsulta != null) {
            boolean pode = podeFinalizarConsulta();
            btnFinalizarConsulta.setVisible(pode);
            btnFinalizarConsulta.setManaged(pode);
        }
    }

    // =========================
    // HANDLERS DO FXML
    // =========================
    @FXML
    private void onNovo() {
        limparFormulario();

        dpData.setValue(null);
        dpData.getEditor().clear();

        tbAgenda.getItems().clear();
        setMsg("");
    }

    @FXML
    private void onSalvar() {
        try {
            LocalDate data = dpData.getValue();

            // ✅ ÚNICA MUDANÇA DO PROBLEMA: pega profissional de forma confiável (value OU texto do editor)
            Usuario profissional = obterProfissionalSelecionado();

            Paciente paciente = cbPaciente.getValue();
            SalaAtendimento sala = cbSala.getValue();

            LocalTime horaInicio = parseHora(txtHoraInicio.getText());
            LocalTime horaFim = parseHora(txtHoraFim.getText());

            if (data == null) { setMsg("Selecione uma data."); return; }

            if (data.isBefore(LocalDate.now())) {
                setMsg("❌ Não é permitido agendar no passado.");
                return;
            }

            if (profissional == null) { setMsg("Selecione um profissional."); return; }
            if (sala == null) { setMsg("Selecione uma sala."); return; }
            if (horaInicio == null || horaFim == null) { setMsg("Informe horários válidos (HH:mm)."); return; }
            if (!horaFim.isAfter(horaInicio)) { setMsg("Hora fim deve ser maior que hora início."); return; }

            if (!horarioPermitido(horaInicio, horaFim)) {
                setMsg("Horário inválido: fora do expediente ou durante almoço (12:00-13:00).");
                return;
            }

            String procedimento = procedimentoDAO.normalizar(txtProcedimento.getText());
            txtProcedimento.setText(procedimento);

            if (procedimento == null || procedimento.isBlank()) {
                setMsg("Informe o procedimento.");
                return;
            }

            Agendamento ag = new Agendamento();
            ag.setData(data);
            ag.setHoraInicio(horaInicio);
            ag.setHoraFim(horaFim);

            ag.setProfissionalId(Math.toIntExact(profissional.getId()));
            ag.setProfissionalNome(
                    (profissional.getPessoaNome() != null && !profissional.getPessoaNome().isBlank())
                            ? profissional.getPessoaNome()
                            : profissional.getNome()
            );

            if (paciente != null) {
                ag.setPacienteId(Math.toIntExact(paciente.getId()));
                ag.setPacienteNome(paciente.getNome());
            }

            ag.setSala(sala);
            ag.setProcedimento(procedimento);
            ag.setObservacoes(txtObservacoes.getText() != null ? txtObservacoes.getText().trim() : "");
            ag.setStatus(StatusAgendamento.AGENDADO);

            if (agendamentoDAO.existeConflito(ag)) {
                setMsg("Conflito de agenda: já existe agendamento nesse horário (profissional/sala/paciente).");
                return;
            }

            agendamentoDAO.salvar(ag);
            procedimentoDAO.registrarUso(procedimento, sala);

            setMsg("Agendamento salvo com sucesso.");
            limparFormulario();
            carregarAgendaDoDia();

        } catch (Exception e) {
            e.printStackTrace();
            setMsg("Erro ao salvar agendamento: " + e.getMessage());
        }
    }

    @FXML
    private void onIniciarAtendimento() {
        Agendamento sel = tbAgenda.getSelectionModel().getSelectedItem();
        if (sel == null) {
            setMsg("Selecione um agendamento para iniciar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/anamnese-view.fxml"));
            Parent root = loader.load();

            AnamneseController controller = loader.getController();
            controller.setAgendamento(sel);

            Stage stage = new Stage();
            stage.setTitle("Anamnese / Evolução");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            carregarAgendaDoDia();

        } catch (Exception e) {
            e.printStackTrace();
            setMsg("Erro ao iniciar atendimento: " + e.getMessage());
        }
    }

    @FXML
    private void onFinalizarConsulta() {
        Agendamento sel = tbAgenda.getSelectionModel().getSelectedItem();
        if (sel == null) {
            setMsg("Selecione um agendamento para finalizar.");
            return;
        }

        try {
            agendamentoDAO.finalizarConsulta(sel.getId());
            setMsg("Consulta finalizada.");
            carregarAgendaDoDia();
        } catch (Exception e) {
            e.printStackTrace();
            setMsg("Erro ao finalizar: " + e.getMessage());
        }
    }

    @FXML
    private void onAtualizarLista() {
        carregarAgendaDoDia();
    }

    @FXML
    private void onDataAlterada() {
        LocalDate data = dpData.getValue();
        if (data == null) return;

        if (data.isBefore(LocalDate.now())) {
            setMsg("❌ Não é permitido selecionar data no passado.");
            dpData.setValue(null);
            dpData.getEditor().clear();
            tbAgenda.getItems().clear();
            return;
        }

        carregarAgendaDoDia();
    }

    private void carregarAgendaDoDia() {
        LocalDate data = dpData.getValue();
        if (data == null) {
            tbAgenda.getItems().clear();
            setMsg("Selecione uma data para visualizar a agenda.");
            return;
        }

        if (data.isBefore(LocalDate.now())) {
            setMsg("❌ Não é permitido selecionar data no passado.");
            tbAgenda.getItems().clear();
            return;
        }

        List<Agendamento> lista;

        if (podeVerTodos()) {
            Usuario escolhido = cbProfissional.getValue();
            if (escolhido != null) {
                lista = agendamentoDAO.listarPorDataEProfissional(data, Math.toIntExact(escolhido.getId()));
            } else {
                lista = agendamentoDAO.listarPorData(data);
            }
        } else {
            Usuario logado = Session.getUsuario();
            if (logado != null) {
                lista = agendamentoDAO.listarPorDataEProfissional(data, Math.toIntExact(logado.getId()));
            } else {
                lista = agendamentoDAO.listarPorData(data);
            }
        }

        tbAgenda.setItems(FXCollections.observableArrayList(lista));
        setMsg("Agenda carregada.");
    }

    private void preencherFormulario(Agendamento ag) {
        dpData.setValue(ag.getData());
        txtHoraInicio.setText(ag.getHoraInicio() != null ? ag.getHoraInicio().format(HORA_FORMATTER) : "");
        txtHoraFim.setText(ag.getHoraFim() != null ? ag.getHoraFim().format(HORA_FORMATTER) : "");
        txtProcedimento.setText(ag.getProcedimento() != null ? ag.getProcedimento() : "");
        txtObservacoes.setText(ag.getObservacoes() != null ? ag.getObservacoes() : "");

        if (ag.getSala() != null) cbSala.setValue(ag.getSala());

        if (ag.getPacienteId() != null) {
            cbPaciente.getItems().stream()
                    .filter(p -> p.getId() != null && p.getId().intValue() == ag.getPacienteId())
                    .findFirst()
                    .ifPresent(p -> cbPaciente.setValue(p));
        }

        if (ag.getProfissionalId() != null) {
            cbProfissional.getItems().stream()
                    .filter(u -> u.getId() != null && u.getId().intValue() == ag.getProfissionalId())
                    .findFirst()
                    .ifPresent(u -> cbProfissional.setValue(u));
        }
    }

    private LocalTime parseHora(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            return LocalTime.parse(t, HORA_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean horarioPermitido(LocalTime ini, LocalTime fim) {
        if (ini.isBefore(HORA_ABERTURA) || fim.isAfter(HORA_FECHAMENTO)) return false;
        boolean sobrepoeAlmoco = !(fim.isBefore(ALMOCO_INICIO) || ini.isAfter(ALMOCO_FIM));
        return !sobrepoeAlmoco;
    }

    private void limparFormulario() {
        txtHoraInicio.clear();
        txtHoraFim.clear();
        txtProcedimento.clear();
        txtObservacoes.clear();
        cbPaciente.getSelectionModel().clearSelection();
        cbSala.getSelectionModel().clearSelection();
    }

    private void setMsg(String s) {
        if (lblMensagem != null) lblMensagem.setText(s == null ? "" : s);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // =========================================================
    // ✅ ADICIONADO (mínimo): resolver profissional quando o ComboBox é editável
    // =========================================================
    private Usuario obterProfissionalSelecionado() {
        if (cbProfissional == null) return null;

        // 1) Se já tem value, ok
        Usuario v = cbProfissional.getValue();
        if (v != null) return v;

        // 2) Se não tem value, tenta resolver pelo texto visível no editor
        if (!cbProfissional.isEditable() || cbProfissional.getEditor() == null) return null;

        String digitado = cbProfissional.getEditor().getText();
        if (digitado == null || digitado.trim().isBlank()) return null;

        Usuario achado = buscarProfissionalPorTexto(digitado.trim());
        if (achado != null) {
            cbProfissional.getSelectionModel().select(achado);
            cbProfissional.setValue(achado);
        }
        return achado;
    }

    private Usuario buscarProfissionalPorTexto(String texto) {
        if (texto == null) return null;
        String alvo = texto.trim();
        if (alvo.isBlank()) return null;

        // Primeiro tenta nos itens atuais (filtrados)
        if (cbProfissional != null && cbProfissional.getItems() != null) {
            for (Usuario u : cbProfissional.getItems()) {
                if (u == null) continue;
                if (textoProfissional(u).equalsIgnoreCase(alvo)) return u;
            }
        }

        // Fallback: tenta na lista completa (se o filtro ocultou algo)
        try {
            List<Usuario> todos = usuarioDAO.listarProfissionaisAtivos();
            for (Usuario u : todos) {
                if (u == null) continue;
                if (textoProfissional(u).equalsIgnoreCase(alvo)) return u;
            }
        } catch (Exception ignored) { }

        return null;
    }
}