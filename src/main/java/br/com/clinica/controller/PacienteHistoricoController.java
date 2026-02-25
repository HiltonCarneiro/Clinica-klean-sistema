package br.com.clinica.controller;

import br.com.clinica.dao.AnamneseDAO;
import br.com.clinica.dao.AnexoPacienteDAO;
import br.com.clinica.dao.AnexoPacienteDAO.AnexoPacienteItem;
import br.com.clinica.model.Anamnese;
import br.com.clinica.model.Paciente;
import br.com.clinica.util.ValidationUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PacienteHistoricoController {

    // ======= CABEÇALHO =======
    @FXML private Label lblNome;
    @FXML private Label lblCpf;
    @FXML private Label lblTelefone;
    @FXML private Label lblDataNascimento;
    @FXML private Label lblAtivo;

    // ======= ABA ANEXOS =======
    @FXML private TableView<AnexoPacienteItem> tableAnexos;
    @FXML private TableColumn<AnexoPacienteItem, String> colAnexoNome;
    @FXML private TableColumn<AnexoPacienteItem, String> colAnexoDescricao;
    @FXML private TableColumn<AnexoPacienteItem, String> colAnexoDataHora;
    @FXML private Button btnAbrirAnexo;
    @FXML private Button btnAtualizarAnexos;

    // ======= ABA ANAMNESE / EVOLUÇÃO =======
    @FXML private TableView<Anamnese> tableAnamnese;
    @FXML private TableColumn<Anamnese, String> colAnaDataHora;
    @FXML private TableColumn<Anamnese, String> colAnaTipo;

    @FXML private TextArea txtObservacoes;
    @FXML private TextArea txtResumo;
    @FXML private TextArea txtJson;
    @FXML private TitledPane tpJson;

    @FXML private Button btnAtualizarAnamnese;

    // ======= DADOS =======
    private final AnexoPacienteDAO anexoDAO = new AnexoPacienteDAO();
    private final AnamneseDAO anamneseDAO = new AnamneseDAO();

    private final ObservableList<AnexoPacienteItem> anexos = FXCollections.observableArrayList();
    private final ObservableList<Anamnese> anamneses = FXCollections.observableArrayList();

    private Paciente paciente;

    private final DateTimeFormatter fmtBr = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        // ---- Anexos
        if (colAnexoNome != null) colAnexoNome.setCellValueFactory(c ->
                new SimpleStringProperty(safe(c.getValue().getNomeArquivo()))
        );
        if (colAnexoDescricao != null) colAnexoDescricao.setCellValueFactory(c ->
                new SimpleStringProperty(safe(c.getValue().getDescricao()))
        );
        if (colAnexoDataHora != null) colAnexoDataHora.setCellValueFactory(c ->
                new SimpleStringProperty(safe(c.getValue().getDataHora()))
        );

        if (tableAnexos != null) tableAnexos.setItems(anexos);

        if (btnAbrirAnexo != null) btnAbrirAnexo.setDisable(true);

        if (tableAnexos != null) {
            tableAnexos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (btnAbrirAnexo != null) btnAbrirAnexo.setDisable(newSel == null);
            });
        }

        // ---- Anamnese/Evolução
        if (colAnaDataHora != null) colAnaDataHora.setCellValueFactory(c ->
                new SimpleStringProperty(safe(c.getValue().getDataHora()))
        );
        if (colAnaTipo != null) colAnaTipo.setCellValueFactory(c ->
                new SimpleStringProperty(formatTipo(safe(c.getValue().getTipo())))
        );

        if (tableAnamnese != null) tableAnamnese.setItems(anamneses);

        if (tableAnamnese != null) {
            tableAnamnese.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                preencherDetalheAnamnese(newSel);
            });
        }

        if (txtObservacoes != null) txtObservacoes.setEditable(false);
        if (txtResumo != null) txtResumo.setEditable(false);
        if (txtJson != null) txtJson.setEditable(false);
        if (tpJson != null) tpJson.setExpanded(false);
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
        preencherCabecalho();
        carregarAnexos();
        carregarAnamneses();
    }

    // ===============================
    // CABEÇALHO
    // ===============================
    private void preencherCabecalho() {
        if (paciente == null) return;

        if (lblNome != null) lblNome.setText(safe(paciente.getNome()));
        if (lblCpf != null) lblCpf.setText(ValidationUtils.formatCpf(safe(paciente.getCpf())));
        if (lblTelefone != null) lblTelefone.setText(ValidationUtils.formatPhoneBr(safe(paciente.getTelefone())));

        LocalDate dn = paciente.getDataNascimento();
        if (lblDataNascimento != null) lblDataNascimento.setText(dn == null ? "" : dn.format(fmtBr));

        if (lblAtivo != null) lblAtivo.setText(paciente.isAtivo() ? "Sim" : "Não");
    }

    // ===============================
    // ANEXOS
    // ===============================
    @FXML
    private void onAtualizarAnexos() {
        carregarAnexos();
    }

    private void carregarAnexos() {
        anexos.clear();
        if (paciente == null || paciente.getId() == null) return;

        List<AnexoPacienteItem> list = anexoDAO.listarPorPaciente(paciente.getId());
        anexos.addAll(list);

        if (btnAbrirAnexo != null) btnAbrirAnexo.setDisable(true);
        if (tableAnexos != null) tableAnexos.getSelectionModel().clearSelection();
    }

    @FXML
    private void onAbrirAnexo() {
        AnexoPacienteItem item = (tableAnexos == null) ? null : tableAnexos.getSelectionModel().getSelectedItem();
        if (item == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um anexo para abrir.").showAndWait();
            return;
        }

        File f = item.getFile();
        if (f == null || !f.exists()) {
            new Alert(Alert.AlertType.ERROR, "Arquivo não encontrado no caminho salvo.").showAndWait();
            return;
        }

        try {
            anexoDAO.abrirNoSistema(f);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Não foi possível abrir o arquivo.").showAndWait();
        }
    }

    // ===============================
    // ANAMNESE & EVOLUÇÕES
    // ===============================
    @FXML
    private void onAtualizarAnamnese() {
        carregarAnamneses();
    }

    private void carregarAnamneses() {
        anamneses.clear();
        limparDetalheAnamnese();

        if (paciente == null || paciente.getId() == null) return;

        List<Anamnese> list = anamneseDAO.listarPorPaciente(paciente.getId());
        anamneses.addAll(list);

        if (tableAnamnese != null) tableAnamnese.getSelectionModel().clearSelection();
    }

    private void preencherDetalheAnamnese(Anamnese a) {
        if (a == null) {
            limparDetalheAnamnese();
            return;
        }

        String obs = safe(a.getObservacoes());
        String rawJson = safe(a.getDadosJson());

        if (txtObservacoes != null) {
            txtObservacoes.setText(obs);
        }

        if (txtResumo != null) {
            txtResumo.setText(montarResumoBonito(a, rawJson, obs));
        }

        if (txtJson != null) {
            txtJson.setText(prettyJson(rawJson));
        }

        // JSON recolhido por padrão
        if (tpJson != null) tpJson.setExpanded(false);
    }

    private void limparDetalheAnamnese() {
        if (txtObservacoes != null) txtObservacoes.clear();
        if (txtResumo != null) txtResumo.clear();
        if (txtJson != null) txtJson.clear();
        if (tpJson != null) tpJson.setExpanded(false);
    }

    private String montarResumoBonito(Anamnese a, String rawJson, String obs) {
        String tipo = formatTipo(safe(a.getTipo()));
        String dataHora = safe(a.getDataHora());

        String queixa = getJsonField(rawJson, "queixa");
        String evolucao = getJsonField(rawJson, "evolucao");

        String pa = getJsonField(rawJson, "pa");
        String fc = getJsonField(rawJson, "fc");
        String fr = getJsonField(rawJson, "fr");
        String temp = getJsonField(rawJson, "temp");
        String peso = getJsonField(rawJson, "peso");
        String altura = getJsonField(rawJson, "altura");
        String spo2 = getJsonField(rawJson, "spo2");

        String antecedentes = getJsonField(rawJson, "antecedentes");
        String medicacoes = getJsonField(rawJson, "medicacoes");
        String alergias = getJsonField(rawJson, "alergias");
        String cirurgias = getJsonField(rawJson, "cirurgias");

        String tabagismo = getJsonField(rawJson, "tabagismo");
        String alcool = getJsonField(rawJson, "alcool");
        String sono = getJsonField(rawJson, "sono");
        String atividade = getJsonField(rawJson, "atividade_fisica");
        String alimentacao = getJsonField(rawJson, "alimentacao");

        String exameGeral = getJsonField(rawJson, "exame_geral");
        String exameSeg = getJsonField(rawJson, "exame_segmentar");

        StringBuilder sb = new StringBuilder();

        sb.append(tipo);
        if (!dataHora.isBlank()) sb.append(" — ").append(dataHora);
        sb.append("\n\n");

        if (!queixa.isBlank()) {
            sb.append("Queixa principal\n");
            sb.append(queixa).append("\n\n");
        }
        if (!evolucao.isBlank()) {
            sb.append("Evolução\n");
            sb.append(evolucao).append("\n\n");
        }

        String sinais = montarLinhaSinaisVitais(pa, fc, fr, temp, peso, altura, spo2);
        if (!sinais.isBlank()) {
            sb.append("Sinais vitais\n");
            sb.append(sinais).append("\n\n");
        }

        boolean temHist = !antecedentes.isBlank() || !medicacoes.isBlank() || !alergias.isBlank() || !cirurgias.isBlank();
        if (temHist) {
            sb.append("Histórico clínico\n");
            if (!antecedentes.isBlank()) sb.append("• Antecedentes: ").append(antecedentes).append("\n");
            if (!medicacoes.isBlank()) sb.append("• Medicações: ").append(medicacoes).append("\n");
            if (!alergias.isBlank()) sb.append("• Alergias: ").append(alergias).append("\n");
            if (!cirurgias.isBlank()) sb.append("• Cirurgias: ").append(cirurgias).append("\n");
            sb.append("\n");
        }

        boolean temHab = !tabagismo.isBlank() || !alcool.isBlank() || !sono.isBlank() || !atividade.isBlank() || !alimentacao.isBlank();
        if (temHab) {
            sb.append("Hábitos\n");
            if (!tabagismo.isBlank()) sb.append("• Tabagismo: ").append(tabagismo).append("\n");
            if (!alcool.isBlank()) sb.append("• Álcool: ").append(alcool).append("\n");
            if (!sono.isBlank()) sb.append("• Sono: ").append(sono).append("\n");
            if (!atividade.isBlank()) sb.append("• Atividade física: ").append(atividade).append("\n");
            if (!alimentacao.isBlank()) sb.append("• Alimentação: ").append(alimentacao).append("\n");
            sb.append("\n");
        }

        boolean temEx = !exameGeral.isBlank() || !exameSeg.isBlank();
        if (temEx) {
            sb.append("Exame físico\n");
            if (!exameGeral.isBlank()) sb.append("• Geral: ").append(exameGeral).append("\n");
            if (!exameSeg.isBlank()) sb.append("• Segmentar: ").append(exameSeg).append("\n");
            sb.append("\n");
        }

        if (!obs.isBlank()) {
            sb.append("Observações\n");
            sb.append(obs).append("\n");
        }

        String out = sb.toString().trim();
        if (out.isBlank()) return "Sem detalhes para exibir neste registro.";
        return out;
    }

    private String montarLinhaSinaisVitais(String pa, String fc, String fr, String temp, String peso, String altura, String spo2) {
        StringBuilder s = new StringBuilder();
        appendCampo(s, "PA", pa);
        appendCampo(s, "FC", fc);
        appendCampo(s, "FR", fr);
        appendCampo(s, "Temp", temp, "°C");
        appendCampo(s, "Peso", peso, "kg");
        appendCampo(s, "Altura", altura, "m");
        appendCampo(s, "SpO₂", spo2, "%");
        return s.toString().trim();
    }

    private void appendCampo(StringBuilder sb, String label, String valor) {
        appendCampo(sb, label, valor, "");
    }

    private void appendCampo(StringBuilder sb, String label, String valor, String sufixo) {
        String v = safe(valor).trim();
        if (v.isBlank()) return;

        if (sb.length() > 0) sb.append("   ");
        sb.append(label).append(": ").append(v);
        if (sufixo != null && !sufixo.isBlank()) sb.append(" ").append(sufixo);
    }

    private String formatTipo(String tipo) {
        if (tipo == null) return "";
        String t = tipo.trim().toUpperCase();
        return switch (t) {
            case "ANAMNESE_INICIAL" -> "Anamnese inicial";
            case "EVOLUCAO" -> "Evolução";
            default -> tipo;
        };
    }

    // ===============================
    // FECHAR
    // ===============================
    @FXML
    private void onFechar() {
        if (lblNome != null && lblNome.getScene() != null) {
            lblNome.getScene().getWindow().hide();
        }
    }

    // ===============================
    // UTIL
    // ===============================
    private String safe(String s) {
        return s == null ? "" : s;
    }

    /**
     * Extrai um campo de um JSON do tipo {"campo":"valor"} sem depender de libs.
     * Se não encontrar, retorna "".
     */
    private String getJsonField(String json, String field) {
        if (json == null || json.isBlank() || field == null || field.isBlank()) return "";
        String t = json.trim();

        String key = "\"" + field + "\":\"";
        int i = t.indexOf(key);
        if (i < 0) return "";

        int start = i + key.length();
        int end = start;
        boolean escape = false;

        while (end < t.length()) {
            char c = t.charAt(end);
            if (escape) {
                escape = false;
                end++;
                continue;
            }
            if (c == '\\') {
                escape = true;
                end++;
                continue;
            }
            if (c == '"') break;
            end++;
        }

        if (end <= start || end >= t.length()) return "";

        String raw = t.substring(start, end);
        return raw.replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trim();
    }

    /**
     * "Pretty print" simples de JSON sem depender de libs.
     * Se não parecer JSON, retorna o texto original.
     */
    private String prettyJson(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (!(t.startsWith("{") || t.startsWith("["))) return s;

        StringBuilder out = new StringBuilder();
        int indent = 0;
        boolean inString = false;

        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);

            if (c == '"' && (i == 0 || t.charAt(i - 1) != '\\')) {
                inString = !inString;
                out.append(c);
                continue;
            }

            if (inString) {
                out.append(c);
                continue;
            }

            switch (c) {
                case '{', '[' -> {
                    out.append(c).append('\n');
                    indent++;
                    out.append("  ".repeat(indent));
                }
                case '}', ']' -> {
                    out.append('\n');
                    indent = Math.max(0, indent - 1);
                    out.append("  ".repeat(indent)).append(c);
                }
                case ',' -> {
                    out.append(c).append('\n');
                    out.append("  ".repeat(indent));
                }
                case ':' -> out.append(": ");
                default -> {
                    if (!Character.isWhitespace(c)) out.append(c);
                }
            }
        }
        return out.toString();
    }
}