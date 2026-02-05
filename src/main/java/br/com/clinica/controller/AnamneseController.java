package br.com.clinica.controller;

import br.com.clinica.dao.AnamneseDAO;
import br.com.clinica.dao.AnexoPacienteDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Anamnese;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.session.Session;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AnamneseController {

    @FXML private Label lblPaciente;
    @FXML private Label lblProfissional;
    @FXML private Label lblAgendamento;
    @FXML private Label lblInfo;

    @FXML private ComboBox<String> cbTipo;

    @FXML private Button btnNovaEvolucao;
    @FXML private Button btnSalvarInicial;
    @FXML private Button btnSalvarEvolucao;

    @FXML private TextArea taQueixa;
    @FXML private TextArea taEvolucao;
    @FXML private TextArea taObservacoes;

    // Histórico
    @FXML private TableView<Anamnese> tvHistorico;
    @FXML private TableColumn<Anamnese, String> colData;
    @FXML private TableColumn<Anamnese, String> colTipo;

    // Anexos
    @FXML private TextField tfDescricaoAnexo;

    @FXML private TableView<AnexoPacienteDAO.AnexoPacienteItem> tvAnexos;
    @FXML private TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colAnexoData;
    @FXML private TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colAnexoArquivo;
    @FXML private TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colAnexoDescricao;

    @FXML private Button btnAnexarPdf;
    @FXML private Button btnAbrirPdf;
    @FXML private Button btnRemoverPdf;

    private final AnamneseDAO anamneseDAO = new AnamneseDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final AnexoPacienteDAO anexoDAO = new AnexoPacienteDAO();

    private Agendamento agendamento;
    private Paciente paciente;

    private boolean inicialJaSalva = false;

    private static final DateTimeFormatter DB_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", new Locale("pt", "BR"));

    @FXML
    public void initialize() {
        cbTipo.setItems(FXCollections.observableArrayList("ANAMNESE_INICIAL", "EVOLUCAO"));
        cbTipo.getSelectionModel().select("ANAMNESE_INICIAL");

        // Histórico
        colData.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getDataHora()));
        colTipo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getTipo()));

        tvHistorico.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) carregarSelecionada(sel);
        });

        cbTipo.valueProperty().addListener((obs, old, tipo) -> atualizarVisibilidadeBotoes(tipo));
        atualizarVisibilidadeBotoes(cbTipo.getValue());

        // ✅ ANEXOS: aqui é o que faltava bater com o seu FXML
        colAnexoData.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getDataHora()));

        // ✅ Mostra o nome do arquivo
        colAnexoArquivo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNomeArquivo()));

        colAnexoDescricao.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getDescricao() == null ? "" : c.getValue().getDescricao()
                ));

        tvAnexos.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> atualizarBotoesAnexos());
        atualizarBotoesAnexos();
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;

        Integer pacienteIdInt = (agendamento != null) ? agendamento.getPacienteId() : null;
        if (pacienteIdInt != null) {
            this.paciente = pacienteDAO.buscarPorId(Long.valueOf(pacienteIdInt));
        } else {
            this.paciente = null;
        }

        preencherCabecalho();
        recarregarHistorico();

        inicialJaSalva = tvHistorico.getItems().stream()
                .anyMatch(a -> "ANAMNESE_INICIAL".equalsIgnoreCase(a.getTipo()));

        cbTipo.getSelectionModel().select(inicialJaSalva ? "EVOLUCAO" : "ANAMNESE_INICIAL");
        atualizarVisibilidadeBotoes(cbTipo.getValue());

        limparFormulario();
        carregarAnexosDoPaciente();
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
        setInfo("");
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
        setInfo("");

        if (paciente == null || paciente.getId() == null) {
            setInfo("Paciente não definido.");
            return;
        }

        Usuario u = Session.getUsuario();
        if (u == null || u.getId() == null) {
            setInfo("Usuário não está logado.");
            return;
        }

        String queixa = safe(taQueixa);
        String evolucao = safe(taEvolucao);

        if ("ANAMNESE_INICIAL".equals(tipo) && queixa.isBlank()) {
            setInfo("Na anamnese inicial, preencha pelo menos a queixa principal.");
            return;
        }

        if ("EVOLUCAO".equals(tipo) && queixa.isBlank() && evolucao.isBlank()) {
            setInfo("Na evolução, preencha pelo menos Queixa ou Evolução.");
            return;
        }

        Anamnese a = new Anamnese();
        a.setPacienteId(paciente.getId());
        a.setAgendamentoId(agendamento != null ? agendamento.getId() : null);
        a.setProfissionalId(u.getId());
        a.setDataHora(LocalDateTime.now().format(DB_FMT));
        a.setTipo(tipo);
        a.setDadosJson("{\"queixa\":\"" + escapeJson(queixa) + "\",\"evolucao\":\"" + escapeJson(evolucao) + "\"}");
        a.setObservacoes(safe(taObservacoes));

        int id = anamneseDAO.inserir(a);
        if (id > 0) {
            recarregarHistorico();
            limparFormulario();

            if ("ANAMNESE_INICIAL".equals(tipo)) {
                inicialJaSalva = true;
                cbTipo.getSelectionModel().select("EVOLUCAO");
                atualizarVisibilidadeBotoes("EVOLUCAO");
                setInfo("Anamnese inicial salva. Evoluções liberadas.");
            } else {
                setInfo("Evolução salva com sucesso.");
            }
        } else {
            setInfo("Não foi possível salvar.");
        }
    }

    private void carregarSelecionada(Anamnese a) {
        cbTipo.getSelectionModel().select(a.getTipo());

        String json = a.getDadosJson() == null ? "" : a.getDadosJson();
        taQueixa.setText(extrairCampoJson(json, "queixa"));
        taEvolucao.setText(extrairCampoJson(json, "evolucao"));

        taObservacoes.setText(a.getObservacoes() == null ? "" : a.getObservacoes());

        atualizarVisibilidadeBotoes(cbTipo.getValue());
    }

    private void limparFormulario() {
        taQueixa.clear();
        taEvolucao.clear();
        taObservacoes.clear();
    }

    // ================= ANEXOS =================

    @FXML
    private void onAnexarPdf() {
        setInfo("");

        if (paciente == null || paciente.getId() == null) {
            setInfo("Paciente não definido.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Selecionar PDF para anexar");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo PDF", "*.pdf"));

        File pdf = fc.showOpenDialog(lblPaciente.getScene().getWindow());
        if (pdf == null) return;

        Integer anamneseId = null;
        if (tvHistorico.getSelectionModel().getSelectedItem() != null) {
            Anamnese sel = tvHistorico.getSelectionModel().getSelectedItem();
            if (sel.getId() != null) anamneseId = sel.getId();
        }

        String desc = tfDescricaoAnexo != null ? tfDescricaoAnexo.getText() : null;

        try {
            anexoDAO.anexarPdf(paciente.getId(), anamneseId, pdf, desc);
            if (tfDescricaoAnexo != null) tfDescricaoAnexo.clear();
            carregarAnexosDoPaciente();
            setInfo("PDF anexado com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erro ao anexar PDF: " + e.getMessage());
        }
    }

    @FXML
    private void onAbrirPdf() {
        setInfo("");

        var sel = tvAnexos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            setInfo("Selecione um anexo.");
            return;
        }

        try {
            anexoDAO.abrirNoSistema(sel.getFile());
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Não foi possível abrir: " + e.getMessage());
        }
    }

    @FXML
    private void onRemoverPdf() {
        setInfo("");

        var sel = tvAnexos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            setInfo("Selecione um anexo.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remover anexo");
        confirm.setHeaderText("Deseja remover este anexo?");
        confirm.setContentText(sel.getNomeArquivo());

        var res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        try {
            anexoDAO.remover(sel.getId());
            carregarAnexosDoPaciente();
            setInfo("Anexo removido.");
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erro ao remover: " + e.getMessage());
        }
    }

    private void carregarAnexosDoPaciente() {
        if (paciente == null || paciente.getId() == null) {
            tvAnexos.setItems(FXCollections.observableArrayList());
            atualizarBotoesAnexos();
            return;
        }

        try {
            var list = anexoDAO.listarPorPaciente(paciente.getId());
            tvAnexos.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
            tvAnexos.setItems(FXCollections.observableArrayList());
        }

        atualizarBotoesAnexos();
    }

    private void atualizarBotoesAnexos() {
        boolean temSel = tvAnexos.getSelectionModel().getSelectedItem() != null;
        btnAbrirPdf.setDisable(!temSel);
        btnRemoverPdf.setDisable(!temSel);
    }

    // ================= HELPERS =================

    private void setInfo(String msg) {
        lblInfo.setText(msg == null ? "" : msg);
    }

    private String safe(TextArea ta) {
        return ta == null || ta.getText() == null ? "" : ta.getText().trim();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private String extrairCampoJson(String json, String campo) {
        if (json == null || json.isBlank()) return "";
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
}