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

    // Guarda o último valor válido para ajudar a evitar “sumir” o texto ao digitar
    private LocalDate ultimaDataValida = null;

    @FXML
    public void initialize() {
        // ✅ Data não deve vir preenchida com hoje
        dpData.setValue(null);
        dpData.getEditor().clear();

        // garante que o usuário possa digitar no campo
        dpData.setEditable(true);

        // ✅ Máscara ao digitar + bloqueio no calendário (sem “sumir” ao perder foco)
        configurarDatePickerData();

        cbProfissional.setItems(FXCollections.observableArrayList(usuarioDAO.listarProfissionaisAtivos()));
        cbPaciente.setItems(FXCollections.observableArrayList(pacienteDAO.listarTodos()));
        cbSala.setItems(FXCollections.observableArrayList(SalaAtendimento.values()));

        // máscara HH:mm
        txtHoraInicio.setTextFormatter(criarTextFormatterHora());
        txtHoraFim.setTextFormatter(criarTextFormatterHora());

        // sugestão procedimento (mantém recurso do sistema)
        configurarSugestoesProcedimento();

        // colunas
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

        // Bloqueia datas passadas no popup do calendário
        dpData.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) return;
                setDisable(item.isBefore(LocalDate.now()));
            }
        });

        // Evita que o DatePicker “apague” o texto do editor quando value = null
        // (comum em alguns skins quando o usuário digita e sai do campo)
        dpData.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                ultimaDataValida = val;
                if (!dpData.getEditor().isFocused()) {
                    dpData.getEditor().setText(dpData.getConverter().toString(val));
                }
            }
        });

        // Máscara ao digitar: 01022026 -> 01/02/2026 (não apaga ao sair)
        dpData.getEditor().setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.isContentChange()) return change;

            String newText = change.getControlNewText();
            if (newText == null) return change;

            // pega só números
            String digits = newText.replaceAll("\\D", "");
            if (digits.length() > 8) digits = digits.substring(0, 8);

            // se apagou tudo, deixa vazio (sem barras)
            if (digits.isEmpty()) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                change.selectRange(0, 0);
                return change;
            }

            // monta dd/MM/yyyy conforme vai digitando
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                sb.append(digits.charAt(i));
                if (i == 1 && digits.length() > 2) sb.append('/');
                if (i == 3 && digits.length() > 4) sb.append('/');
            }
            String formatted = sb.toString();

            // tenta preservar o "sentido" do cursor
            int oldCaret = dpData.getEditor().getCaretPosition();
            int oldLen = change.getControlText() == null ? 0 : change.getControlText().length();

            change.setText(formatted);
            change.setRange(0, oldLen);

            int newCaret = Math.min(formatted.length(), oldCaret);
            change.selectRange(newCaret, newCaret);

            return change;
        }));

        // Ao perder foco: valida, normaliza e impede passado.
        // Se inválido, NÃO APAGA (usuário corrige) e mostra mensagem.
        dpData.getEditor().focusedProperty().addListener((obs, old, focused) -> {
            if (focused) return;

            String texto = dpData.getEditor().getText() == null ? "" : dpData.getEditor().getText().trim();

            // se está vazio, tudo bem
            if (texto.isEmpty()) {
                dpData.setValue(null);
                return;
            }

            // se ainda não completou dd/MM/aaaa, não força parse nem limpa
            // (evita o “sumiu” quando o usuário sai com data incompleta)
            String digits = texto.replaceAll("\\D", "");
            if (digits.length() < 8) {
                setMsg("Digite a data completa (dd/MM/aaaa). ");
                return;
            }

            LocalDate parsed = dpData.getConverter().fromString(texto);
            if (parsed == null) {
                setMsg("Data inválida. Use o formato dd/MM/aaaa.");
                // mantém o texto para o usuário corrigir
                dpData.setValue(null);
                return;
            }

            if (parsed.isBefore(LocalDate.now())) {
                setMsg("❌ Não é permitido selecionar data no passado.");
                dpData.setValue(null);
                // mantém o texto e volta o foco pro campo para corrigir
                Platform.runLater(() -> dpData.getEditor().requestFocus());
                tbAgenda.getItems().clear();
                return;
            }

            // válido: normaliza e carrega a agenda
            dpData.setValue(parsed);
            dpData.getEditor().setText(dpData.getConverter().toString(parsed));
            carregarAgendaDoDia();
        });
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

    private TextFormatter<String> criarTextFormatterHora() {
        return new TextFormatter<>(change -> {
            String txt = change.getControlNewText();
            if (txt == null || txt.isEmpty()) return change;

            String digits = txt.replaceAll("\\D", "");
            if (digits.length() > 4) digits = digits.substring(0, 4);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                sb.append(digits.charAt(i));
                if (i == 1) sb.append(':');
            }

            String formatted = sb.toString();
            change.setText(formatted);
            change.setRange(0, change.getControlText().length());
            change.selectRange(formatted.length(), formatted.length());
            return change;
        });
    }

    // =========================
    // PERFIL: mantém padrão do seu projeto (constantes Perfis)
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
        // admin e profissionais da saúde podem finalizar (recepção normalmente não)
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
    // HANDLERS DO FXML (todos)
    // =========================
    @FXML
    private void onNovo() {
        limparFormulario();

        // ✅ data deve ficar vazia ao criar novo agendamento
        dpData.setValue(null);
        dpData.getEditor().clear();

        tbAgenda.getItems().clear();
        setMsg("");
    }

    @FXML
    private void onSalvar() {
        try {
            LocalDate data = dpData.getValue();
            Usuario profissional = cbProfissional.getValue();
            Paciente paciente = cbPaciente.getValue();
            SalaAtendimento sala = cbSala.getValue();

            LocalTime horaInicio = parseHora(txtHoraInicio.getText());
            LocalTime horaFim = parseHora(txtHoraFim.getText());

            if (data == null) { setMsg("Selecione uma data."); return; }

            // ✅ mensagem/validação se usuário colocar data no passado
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/com/clinica/view/anamnese-view.fxml"));
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

        // ✅ mensagem ao selecionar data no passado (sem “sumir” durante digitação)
        if (data.isBefore(LocalDate.now())) {
            setMsg("❌ Não é permitido selecionar data no passado.");
            dpData.setValue(null);
            dpData.getEditor().clear();
            tbAgenda.getItems().clear();
            return;
        }

        carregarAgendaDoDia();
    }

    // =========================
    // Carregar / preencher
    // =========================
    private void carregarAgendaDoDia() {
        LocalDate data = dpData.getValue();
        if (data == null) {
            tbAgenda.getItems().clear();
            setMsg("Selecione uma data para visualizar a agenda.");
            return;
        }

        // segurança extra
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

    // =========================
    // Regras e util
    // =========================
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
        // profissional não limpa para não atrapalhar filtro
    }

    private void setMsg(String s) {
        if (lblMensagem != null) lblMensagem.setText(s == null ? "" : s);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}