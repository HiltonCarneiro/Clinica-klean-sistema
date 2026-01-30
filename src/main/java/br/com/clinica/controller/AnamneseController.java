package br.com.clinica.controller;

import br.com.clinica.dao.AnamneseDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Anamnese;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.session.Session;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AnamneseController {

    @FXML private Label lblPaciente;
    @FXML private Label lblProfissional;
    @FXML private Label lblAgendamento;
    @FXML private Label lblInfo;

    @FXML private ComboBox<String> cbTipo;
    @FXML private TabPane tabPane;

    // Botões
    @FXML private Button btnNovaEvolucao;
    @FXML private Button btnSalvarInicial;
    @FXML private Button btnSalvarEvolucao;

    // Campos principais
    @FXML private TextArea taQueixa;
    @FXML private TextArea taEvolucao;
    @FXML private TextArea taObservacoes;

    // Sinais vitais
    @FXML private TextField tfPA;
    @FXML private TextField tfFC;
    @FXML private TextField tfFR;
    @FXML private TextField tfTemp;
    @FXML private TextField tfSat;
    @FXML private TextField tfPeso;
    @FXML private TextField tfAltura;

    // Histórico
    @FXML private CheckBox ckAlergia;
    @FXML private TextField tfAlergiaQual;
    @FXML private TextArea taMedicamentos;
    @FXML private TextArea taComorbidades;
    @FXML private TextArea taHistoricoFamiliar;

    // Hábitos
    @FXML private ComboBox<String> cbTabagismo;
    @FXML private ComboBox<String> cbEtilismo;
    @FXML private TextArea taHabitos;

    // Exame físico
    @FXML private TextArea taExameFisico;

    // Tabela histórico
    @FXML private TableView<Anamnese> tvHistorico;
    @FXML private TableColumn<Anamnese, String> colData;
    @FXML private TableColumn<Anamnese, String> colTipo;

    private final AnamneseDAO anamneseDAO = new AnamneseDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();

    private Agendamento agendamento;
    private Paciente paciente;

    private boolean inicialJaSalva = false;

    private static final DateTimeFormatter DB_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        cbTipo.setItems(FXCollections.observableArrayList("ANAMNESE_INICIAL", "EVOLUCAO"));
        cbTipo.getSelectionModel().select("ANAMNESE_INICIAL");

        cbTabagismo.setItems(FXCollections.observableArrayList("Nunca", "Ex-fumante", "Sim"));
        cbEtilismo.setItems(FXCollections.observableArrayList("Não", "Social", "Sim"));

        colData.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDataHora()));
        colTipo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTipo()));

        tvHistorico.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) carregarSelecionada(sel);
        });

        cbTipo.valueProperty().addListener((obs, old, tipo) -> atualizarVisibilidadeBotoes(tipo));
        atualizarVisibilidadeBotoes(cbTipo.getValue());
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;

        Integer pacienteIdInt = (agendamento != null) ? agendamento.getPacienteId() : null;
        if (pacienteIdInt != null) {
            Long pacienteIdLong = Long.valueOf(pacienteIdInt);
            this.paciente = pacienteDAO.buscarPorId(pacienteIdLong);
        } else {
            this.paciente = null;
        }

        preencherCabecalho();
        recarregarHistorico();

        // Se já existir ANAMNESE_INICIAL nesse agendamento, libera evoluções
        inicialJaSalva = tvHistorico.getItems().stream().anyMatch(a -> "ANAMNESE_INICIAL".equalsIgnoreCase(a.getTipo()));
        if (inicialJaSalva) {
            cbTipo.getSelectionModel().select("EVOLUCAO");
        } else {
            cbTipo.getSelectionModel().select("ANAMNESE_INICIAL");
        }

        limparFormulario();
        atualizarVisibilidadeBotoes(cbTipo.getValue());
    }

    private void preencherCabecalho() {
        lblPaciente.setText("Paciente: " + (paciente != null ? paciente.getNome() : "(não definido)"));

        Usuario u = Session.getUsuario();
        String prof = "(não logado)";
        if (u != null) {
            String pessoa = (u.getPessoaNome() != null && !u.getPessoaNome().isBlank())
                    ? u.getPessoaNome()
                    : u.getNome();
            prof = pessoa;
        }
        lblProfissional.setText("Profissional: " + prof);
        lblAgendamento.setText("Agendamento ID: " + (agendamento != null ? agendamento.getId() : "(não definido)"));
    }

    private void recarregarHistorico() {
        if (paciente == null || paciente.getId() == null) {
            tvHistorico.setItems(FXCollections.observableArrayList());
            return;
        }

        List<Anamnese> list;
        if (agendamento != null && agendamento.getId() != null) {
            list = anamneseDAO.listarPorAgendamento(agendamento.getId());
        } else {
            list = anamneseDAO.listarPorPaciente(paciente.getId());
        }

        tvHistorico.setItems(FXCollections.observableArrayList(list));
    }

    private void atualizarVisibilidadeBotoes(String tipo) {
        boolean isInicial = "ANAMNESE_INICIAL".equalsIgnoreCase(tipo);
        boolean isEvolucao = "EVOLUCAO".equalsIgnoreCase(tipo);

        // Se anamnese inicial ainda não foi salva, não mostra controles de evolução
        if (!inicialJaSalva && isEvolucao) {
            cbTipo.getSelectionModel().select("ANAMNESE_INICIAL");
            isEvolucao = false;
            isInicial = true;
        }

        btnSalvarInicial.setVisible(isInicial);
        btnSalvarInicial.setManaged(isInicial);

        btnSalvarEvolucao.setVisible(isEvolucao);
        btnSalvarEvolucao.setManaged(isEvolucao);

        btnNovaEvolucao.setVisible(isEvolucao);
        btnNovaEvolucao.setManaged(isEvolucao);
    }

    @FXML
    private void onNovaEvolucao() {
        tvHistorico.getSelectionModel().clearSelection();
        cbTipo.getSelectionModel().select("EVOLUCAO");
        limparFormulario();
        lblInfo.setText("");
    }

    @FXML
    private void onSalvarInicial() {
        salvarRegistro("ANAMNESE_INICIAL");
    }

    @FXML
    private void onSalvarEvolucao() {
        salvarRegistro("EVOLUCAO");
    }

    private void salvarRegistro(String tipo) {
        lblInfo.setText("");

        if (paciente == null || paciente.getId() == null) {
            lblInfo.setText("Paciente não definido.");
            return;
        }

        Usuario u = Session.getUsuario();
        if (u == null || u.getId() == null) {
            lblInfo.setText("Usuário não está logado.");
            return;
        }

        String queixa = safe(taQueixa.getText());
        String evolucao = safe(taEvolucao.getText());

        if (tipo.equals("ANAMNESE_INICIAL") && queixa.isBlank()) {
            lblInfo.setText("Na anamnese inicial, preencha pelo menos a queixa principal.");
            return;
        }

        if (tipo.equals("EVOLUCAO") && queixa.isBlank() && evolucao.isBlank()) {
            lblInfo.setText("Na evolução, preencha pelo menos Queixa ou Evolução.");
            return;
        }

        Anamnese a = new Anamnese();
        a.setPacienteId(paciente.getId()); // Long
        a.setAgendamentoId(agendamento != null ? agendamento.getId() : null);
        a.setProfissionalId(u.getId()); // Integer
        a.setDataHora(LocalDateTime.now().format(DB_FMT));
        a.setTipo(tipo);
        a.setDadosJson(montarJsonCompleto());
        a.setObservacoes(safe(taObservacoes.getText()));

        int id = anamneseDAO.inserir(a);
        if (id > 0) {
            recarregarHistorico();
            limparFormulario();

            if ("ANAMNESE_INICIAL".equals(tipo)) {
                inicialJaSalva = true;
                cbTipo.getSelectionModel().select("EVOLUCAO");
                atualizarVisibilidadeBotoes("EVOLUCAO");
                lblInfo.setText("Anamnese inicial salva. Evoluções liberadas.");
            } else {
                lblInfo.setText("Evolução salva com sucesso.");
            }
        } else {
            lblInfo.setText("Não foi possível salvar.");
        }
    }

    private void carregarSelecionada(Anamnese a) {
        // Visualização do histórico
        cbTipo.getSelectionModel().select(a.getTipo());

        String json = a.getDadosJson() != null ? a.getDadosJson() : "";

        taQueixa.setText(extrairCampoJson(json, "queixa"));
        taEvolucao.setText(extrairCampoJson(json, "evolucao"));
        taObservacoes.setText(a.getObservacoes());

        tfPA.setText(extrairCampoJson(json, "pa"));
        tfFC.setText(extrairCampoJson(json, "fc"));
        tfFR.setText(extrairCampoJson(json, "fr"));
        tfTemp.setText(extrairCampoJson(json, "temp"));
        tfSat.setText(extrairCampoJson(json, "sat"));
        tfPeso.setText(extrairCampoJson(json, "peso"));
        tfAltura.setText(extrairCampoJson(json, "altura"));

        ckAlergia.setSelected("true".equalsIgnoreCase(extrairCampoJson(json, "alergia")));
        tfAlergiaQual.setText(extrairCampoJson(json, "alergia_qual"));
        taMedicamentos.setText(extrairCampoJson(json, "medicamentos"));
        taComorbidades.setText(extrairCampoJson(json, "comorbidades"));
        taHistoricoFamiliar.setText(extrairCampoJson(json, "historico_familiar"));

        cbTabagismo.getSelectionModel().select(extrairCampoJson(json, "tabagismo"));
        cbEtilismo.getSelectionModel().select(extrairCampoJson(json, "etilismo"));
        taHabitos.setText(extrairCampoJson(json, "habitos"));

        taExameFisico.setText(extrairCampoJson(json, "exame_fisico"));

        atualizarVisibilidadeBotoes(cbTipo.getValue());
    }

    private void limparFormulario() {
        taQueixa.clear();
        taEvolucao.clear();
        taObservacoes.clear();

        tfPA.clear(); tfFC.clear(); tfFR.clear(); tfTemp.clear(); tfSat.clear(); tfPeso.clear(); tfAltura.clear();

        ckAlergia.setSelected(false);
        tfAlergiaQual.clear();
        taMedicamentos.clear();
        taComorbidades.clear();
        taHistoricoFamiliar.clear();

        cbTabagismo.getSelectionModel().clearSelection();
        cbEtilismo.getSelectionModel().clearSelection();
        taHabitos.clear();

        taExameFisico.clear();
    }

    private String montarJsonCompleto() {
        // JSON simples (sem libs), mas completo.
        return "{"
                + kv("queixa", taQueixa.getText()) + ","
                + kv("evolucao", taEvolucao.getText()) + ","

                + kv("pa", tfPA.getText()) + ","
                + kv("fc", tfFC.getText()) + ","
                + kv("fr", tfFR.getText()) + ","
                + kv("temp", tfTemp.getText()) + ","
                + kv("sat", tfSat.getText()) + ","
                + kv("peso", tfPeso.getText()) + ","
                + kv("altura", tfAltura.getText()) + ","

                + "\"alergia\":\"" + (ckAlergia.isSelected() ? "true" : "false") + "\","
                + kv("alergia_qual", tfAlergiaQual.getText()) + ","
                + kv("medicamentos", taMedicamentos.getText()) + ","
                + kv("comorbidades", taComorbidades.getText()) + ","
                + kv("historico_familiar", taHistoricoFamiliar.getText()) + ","

                + kv("tabagismo", cbTabagismo.getValue()) + ","
                + kv("etilismo", cbEtilismo.getValue()) + ","
                + kv("habitos", taHabitos.getText()) + ","

                + kv("exame_fisico", taExameFisico.getText())
                + "}";
    }

    private String kv(String key, String val) {
        return "\"" + key + "\":\"" + escapeJson(val) + "\"";
    }

    private String escapeJson(String s) {
        String v = safe(s);
        return v.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private String extrairCampoJson(String json, String campo) {
        String key = "\"" + campo + "\":\"";
        int i = json.indexOf(key);
        if (i < 0) return "";
        int start = i + key.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return "";
        return json.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}
