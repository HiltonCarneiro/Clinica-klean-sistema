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

    // Aba principal
    @FXML private TextArea taQueixa;
    @FXML private TextArea taEvolucao;
    @FXML private TextArea taObservacoes;

    // Sinais vitais
    @FXML private TextField tfPA;
    @FXML private TextField tfFC;
    @FXML private TextField tfFR;
    @FXML private TextField tfTemp;
    @FXML private TextField tfPeso;
    @FXML private TextField tfAltura;
    @FXML private TextField tfSpO2;

    // Histórico clínico
    @FXML private TextArea taAntecedentes;
    @FXML private TextArea taMedicacoes;
    @FXML private TextArea taAlergias;
    @FXML private TextArea taCirurgias;

    // Hábitos
    @FXML private ComboBox<String> cbTabagismo;
    @FXML private ComboBox<String> cbAlcool;
    @FXML private TextArea taSono;
    @FXML private TextArea taAtividadeFisica;
    @FXML private TextArea taAlimentacao;

    // Exame físico
    @FXML private TextArea taExameGeral;
    @FXML private TextArea taExameSegmentar;

    // Histórico (lista)
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

    private Anamnese anamneseInicialAtual;  // (única)
    private Anamnese selecionada;           // item selecionado no histórico (pode ser evolução)

    private boolean inicialJaSalva = false;

    private static final DateTimeFormatter DB_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.forLanguageTag("pt-BR"));

    @FXML
    public void initialize() {
        cbTipo.setItems(FXCollections.observableArrayList("ANAMNESE_INICIAL", "EVOLUCAO"));
        cbTipo.getSelectionModel().select("ANAMNESE_INICIAL");

        // combos hábitos (padrão mercado)
        cbTabagismo.setItems(FXCollections.observableArrayList("Não", "Sim", "Ex-tabagista"));
        cbAlcool.setItems(FXCollections.observableArrayList("Não", "Social", "Frequente"));
        cbTabagismo.getSelectionModel().select("Não");
        cbAlcool.getSelectionModel().select("Não");

        colData.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getDataHora()));
        colTipo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getTipo()));

        tvHistorico.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                selecionada = sel;
                carregarSelecionada(sel);
            }
        });

        cbTipo.valueProperty().addListener((obs, old, tipo) -> atualizarVisibilidadeBotoes(tipo));
        atualizarVisibilidadeBotoes(cbTipo.getValue());

        // ANEXOS
        colAnexoData.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getDataHora()));
        colAnexoArquivo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNomeArquivo()));
        colAnexoDescricao.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getDescricao() == null ? "" : c.getValue().getDescricao()));

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

        // carrega inicial (única) e evoluções
        carregarInicialEvolucoes();

        // UI padrão: se não existe inicial, fica no tipo inicial; se existe, fica em evolução
        cbTipo.getSelectionModel().select(inicialJaSalva ? "EVOLUCAO" : "ANAMNESE_INICIAL");
        atualizarVisibilidadeBotoes(cbTipo.getValue());

        // limpa campos e anexos
        limparFormulario();
        carregarAnexosDoPaciente();
        setInfo("");
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

    private void carregarInicialEvolucoes() {
        tvHistorico.setItems(FXCollections.observableArrayList());
        selecionada = null;

        if (paciente == null || paciente.getId() == null) {
            inicialJaSalva = false;
            anamneseInicialAtual = null;
            return;
        }

        Integer agId = (agendamento != null ? agendamento.getId() : null);

        // inicial única
        anamneseInicialAtual = (agId != null)
                ? anamneseDAO.buscarInicialPorAgendamento(agId)
                : anamneseDAO.buscarInicialPorPaciente(paciente.getId());

        inicialJaSalva = anamneseInicialAtual != null;

        // histórico: inicial (se existir) + evoluções
        List<Anamnese> lista;
        if (agId != null) {
            lista = anamneseDAO.listarPorAgendamento(agId);
        } else {
            lista = anamneseDAO.listarPorPaciente(paciente.getId());
        }

        tvHistorico.setItems(FXCollections.observableArrayList(lista));
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
        selecionada = null;
        cbTipo.getSelectionModel().select("EVOLUCAO");
        limparFormulario();
        setInfo("Nova evolução: preencha e clique em Salvar evolução.");
    }

    @FXML
    private void onSalvarInicial() {
        salvarOuAtualizar("ANAMNESE_INICIAL");
    }

    @FXML
    private void onSalvarEvolucao() {
        salvarOuAtualizar("EVOLUCAO");
    }

    /**
     * Regras:
     * - ANAMNESE_INICIAL: sempre UPSERT (atualiza se existir).
     * - EVOLUCAO:
     *      - se usuário selecionou uma evolução no histórico -> UPDATE dessa evolução + atualiza arquivo TXT
     *      - se não selecionou -> INSERT nova evolução + cria arquivo TXT
     */
    private void salvarOuAtualizar(String tipo) {
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
        a.setDadosJson(montarJsonCompleto());
        a.setObservacoes(safe(taObservacoes));

        try {
            if ("ANAMNESE_INICIAL".equals(tipo)) {
                int id = anamneseDAO.salvarOuAtualizarInicial(a);
                a.setId(id);
                anamneseInicialAtual = a;
                inicialJaSalva = true;

                carregarInicialEvolucoes();
                setInfo("Anamnese inicial salva/atualizada com sucesso. Evoluções liberadas.");
                cbTipo.getSelectionModel().select("EVOLUCAO");
                atualizarVisibilidadeBotoes("EVOLUCAO");
                return;
            }

            // EVOLUÇÃO
            boolean editandoEvolucaoSelecionada = (selecionada != null
                    && "EVOLUCAO".equalsIgnoreCase(selecionada.getTipo())
                    && selecionada.getId() != null);

            if (editandoEvolucaoSelecionada) {
                a.setId(selecionada.getId());
                anamneseDAO.atualizar(a);

                // atualiza arquivo TXT da evolução
                anexoDAO.criarOuAtualizarArquivoEvolucao(paciente.getId(), a.getId(), gerarTextoEvolucaoParaArquivo(a));

                carregarInicialEvolucoes();
                setInfo("Evolução atualizada (registro e arquivo).");
                return;
            }

            // nova evolução (INSERT + cria arquivo TXT)
            int id = anamneseDAO.inserir(a);
            a.setId(id);

            anexoDAO.criarOuAtualizarArquivoEvolucao(paciente.getId(), a.getId(), gerarTextoEvolucaoParaArquivo(a));

            carregarInicialEvolucoes();
            limparFormulario();
            setInfo("Evolução salva com sucesso (registro + arquivo).");

        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erro ao salvar: " + e.getMessage());
        }
    }

    private void carregarSelecionada(Anamnese a) {
        cbTipo.getSelectionModel().select(a.getTipo());

        String json = a.getDadosJson() == null ? "" : a.getDadosJson();

        // principais
        taQueixa.setText(extrairCampoJson(json, "queixa"));
        taEvolucao.setText(extrairCampoJson(json, "evolucao"));
        taObservacoes.setText(a.getObservacoes() == null ? "" : a.getObservacoes());

        // vitais
        tfPA.setText(extrairCampoJson(json, "pa"));
        tfFC.setText(extrairCampoJson(json, "fc"));
        tfFR.setText(extrairCampoJson(json, "fr"));
        tfTemp.setText(extrairCampoJson(json, "temp"));
        tfPeso.setText(extrairCampoJson(json, "peso"));
        tfAltura.setText(extrairCampoJson(json, "altura"));
        tfSpO2.setText(extrairCampoJson(json, "spo2"));

        // histórico
        taAntecedentes.setText(extrairCampoJson(json, "antecedentes"));
        taMedicacoes.setText(extrairCampoJson(json, "medicacoes"));
        taAlergias.setText(extrairCampoJson(json, "alergias"));
        taCirurgias.setText(extrairCampoJson(json, "cirurgias"));

        // hábitos
        String tab = extrairCampoJson(json, "tabagismo");
        if (!tab.isBlank()) cbTabagismo.getSelectionModel().select(tab);
        String alc = extrairCampoJson(json, "alcool");
        if (!alc.isBlank()) cbAlcool.getSelectionModel().select(alc);

        taSono.setText(extrairCampoJson(json, "sono"));
        taAtividadeFisica.setText(extrairCampoJson(json, "atividade_fisica"));
        taAlimentacao.setText(extrairCampoJson(json, "alimentacao"));

        // exame físico
        taExameGeral.setText(extrairCampoJson(json, "exame_geral"));
        taExameSegmentar.setText(extrairCampoJson(json, "exame_segmentar"));

        atualizarVisibilidadeBotoes(cbTipo.getValue());

        if ("ANAMNESE_INICIAL".equalsIgnoreCase(a.getTipo())) {
            setInfo("Anamnese inicial carregada. Você pode editar e clicar em 'Salvar anamnese inicial' para atualizar (não cria novo).");
        } else {
            setInfo("Evolução carregada. Você pode editar e clicar em 'Salvar evolução' para atualizar esta evolução (arquivo será atualizado).");
        }
    }

    private void limparFormulario() {
        taQueixa.clear();
        taEvolucao.clear();
        taObservacoes.clear();

        tfPA.clear();
        tfFC.clear();
        tfFR.clear();
        tfTemp.clear();
        tfPeso.clear();
        tfAltura.clear();
        tfSpO2.clear();

        taAntecedentes.clear();
        taMedicacoes.clear();
        taAlergias.clear();
        taCirurgias.clear();

        cbTabagismo.getSelectionModel().select("Não");
        cbAlcool.getSelectionModel().select("Não");
        taSono.clear();
        taAtividadeFisica.clear();
        taAlimentacao.clear();

        taExameGeral.clear();
        taExameSegmentar.clear();
    }

    // =========================
    // ANEXOS (PDF)
    // =========================

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

    // =========================
    // JSON helpers / montagem
    // =========================

    private String montarJsonCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        put(sb, "queixa", safe(taQueixa)); sb.append(",");
        put(sb, "evolucao", safe(taEvolucao)); sb.append(",");

        put(sb, "pa", safe(tfPA)); sb.append(",");
        put(sb, "fc", safe(tfFC)); sb.append(",");
        put(sb, "fr", safe(tfFR)); sb.append(",");
        put(sb, "temp", safe(tfTemp)); sb.append(",");
        put(sb, "peso", safe(tfPeso)); sb.append(",");
        put(sb, "altura", safe(tfAltura)); sb.append(",");
        put(sb, "spo2", safe(tfSpO2)); sb.append(",");

        put(sb, "antecedentes", safe(taAntecedentes)); sb.append(",");
        put(sb, "medicacoes", safe(taMedicacoes)); sb.append(",");
        put(sb, "alergias", safe(taAlergias)); sb.append(",");
        put(sb, "cirurgias", safe(taCirurgias)); sb.append(",");

        put(sb, "tabagismo", cbTabagismo.getValue()); sb.append(",");
        put(sb, "alcool", cbAlcool.getValue()); sb.append(",");
        put(sb, "sono", safe(taSono)); sb.append(",");
        put(sb, "atividade_fisica", safe(taAtividadeFisica)); sb.append(",");
        put(sb, "alimentacao", safe(taAlimentacao)); sb.append(",");

        put(sb, "exame_geral", safe(taExameGeral)); sb.append(",");
        put(sb, "exame_segmentar", safe(taExameSegmentar));

        sb.append("}");
        return sb.toString();
    }

    private void put(StringBuilder sb, String key, String value) {
        sb.append("\"").append(escapeJson(key)).append("\":\"")
                .append(escapeJson(value == null ? "" : value))
                .append("\"");
    }

    private String gerarTextoEvolucaoParaArquivo(Anamnese a) {
        StringBuilder t = new StringBuilder();
        t.append("EVOLUÇÃO CLÍNICA").append("\n");
        t.append("Data/Hora: ").append(a.getDataHora()).append("\n");
        t.append("Paciente: ").append(paciente != null ? paciente.getNome() : "").append("\n");
        t.append("Agendamento ID: ").append(agendamento != null ? agendamento.getId() : "").append("\n");
        t.append("--------------------------------------------------").append("\n");
        t.append("Queixa:").append("\n").append(safe(taQueixa)).append("\n\n");
        t.append("Evolução:").append("\n").append(safe(taEvolucao)).append("\n\n");
        t.append("Observações:").append("\n").append(safe(taObservacoes)).append("\n");
        return t.toString();
    }

    private void setInfo(String msg) {
        lblInfo.setText(msg == null ? "" : msg);
    }

    private String safe(TextArea ta) {
        return ta == null || ta.getText() == null ? "" : ta.getText().trim();
    }

    private String safe(TextField tf) {
        return tf == null || tf.getText() == null ? "" : tf.getText().trim();
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