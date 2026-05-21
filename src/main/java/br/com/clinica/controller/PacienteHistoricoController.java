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
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PacienteHistoricoController {

    @FXML private Label lblNome;
    @FXML private Label lblCpf;
    @FXML private Label lblRg;
    @FXML private Label lblTelefone;
    @FXML private Label lblDataNascimento;
    @FXML private Label lblIdade;
    @FXML private Label lblEndereco;
    @FXML private Label lblAtivo;

    @FXML private TableView<AnexoPacienteItem> tableAnexos;
    @FXML private TableColumn<AnexoPacienteItem, String> colAnexoNome;
    @FXML private TableColumn<AnexoPacienteItem, String> colAnexoDescricao;
    @FXML private TableColumn<AnexoPacienteItem, String> colAnexoDataHora;
    @FXML private Button btnAbrirAnexo;
    @FXML private Button btnAtualizarAnexos;

    @FXML private TableView<Anamnese> tableAnamnese;
    @FXML private TableColumn<Anamnese, String> colAnaDataHora;
    @FXML private TableColumn<Anamnese, String> colAnaTipo;

    @FXML private TextArea txtObservacoes;
    @FXML private TextArea txtResumo;
    @FXML private TextArea txtJson;
    @FXML private TitledPane tpJson;

    @FXML private Button btnAtualizarAnamnese;

    private final AnexoPacienteDAO anexoDAO = new AnexoPacienteDAO();
    private final AnamneseDAO anamneseDAO = new AnamneseDAO();

    private final ObservableList<AnexoPacienteItem> anexos = FXCollections.observableArrayList();
    private final ObservableList<Anamnese> anamneses = FXCollections.observableArrayList();

    private Paciente paciente;

    private final DateTimeFormatter fmtBr = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        if (colAnexoNome != null) {
            colAnexoNome.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getNomeArquivo()))
            );
        }

        if (colAnexoDescricao != null) {
            colAnexoDescricao.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getDescricao()))
            );
        }

        if (colAnexoDataHora != null) {
            colAnexoDataHora.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getDataHora()))
            );
        }

        if (tableAnexos != null) {
            tableAnexos.setItems(anexos);
        }

        if (btnAbrirAnexo != null) {
            btnAbrirAnexo.setDisable(true);
        }

        if (tableAnexos != null) {
            tableAnexos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (btnAbrirAnexo != null) {
                    btnAbrirAnexo.setDisable(newSel == null);
                }
            });
        }

        if (colAnaDataHora != null) {
            colAnaDataHora.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getDataHora()))
            );
        }

        if (colAnaTipo != null) {
            colAnaTipo.setCellValueFactory(c ->
                    new SimpleStringProperty(formatTipo(safe(c.getValue().getTipo())))
            );
        }

        if (tableAnamnese != null) {
            tableAnamnese.setItems(anamneses);
        }

        if (tableAnamnese != null) {
            tableAnamnese.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) ->
                    preencherDetalheAnamnese(newSel)
            );
        }

        if (txtObservacoes != null) {
            txtObservacoes.setEditable(false);
        }

        if (txtResumo != null) {
            txtResumo.setEditable(false);
        }

        if (txtJson != null) {
            txtJson.setEditable(false);
        }

        if (tpJson != null) {
            tpJson.setExpanded(false);
            tpJson.setVisible(false);
            tpJson.setManaged(false);
        }
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
        preencherCabecalho();
        carregarAnexos();
        carregarAnamneses();
    }

    private void preencherCabecalho() {
        if (paciente == null) {
            return;
        }

        if (lblNome != null) {
            lblNome.setText(safe(paciente.getNome()));
        }

        if (lblCpf != null) {
            lblCpf.setText(ValidationUtils.formatCpf(safe(paciente.getCpf())));
        }

        if (lblRg != null) {
            lblRg.setText(formatRg(safe(paciente.getRg())));
        }

        if (lblTelefone != null) {
            lblTelefone.setText(ValidationUtils.formatPhoneBr(safe(paciente.getTelefone())));
        }

        LocalDate dn = paciente.getDataNascimento();

        if (lblDataNascimento != null) {
            lblDataNascimento.setText(dn == null ? "" : dn.format(fmtBr));
        }

        if (lblIdade != null) {
            lblIdade.setText(calcularIdadeTexto(dn));
        }

        if (lblEndereco != null) {
            lblEndereco.setText(montarEnderecoCompleto(paciente));
        }

        if (lblAtivo != null) {
            lblAtivo.setText(paciente.isAtivo() ? "Sim" : "Não");
        }
    }

    private String calcularIdadeTexto(LocalDate dn) {
        if (dn == null) {
            return "";
        }

        int idade = Period.between(dn, LocalDate.now()).getYears();

        if (idade < 0) {
            idade = 0;
        }

        return idade + " anos";
    }

    private String montarEnderecoCompleto(Paciente p) {
        if (p == null) {
            return "";
        }

        String rua = safe(p.getRua()).trim();
        String numero = safe(p.getNumero()).trim();
        String complemento = safe(p.getComplemento()).trim();
        String bairro = safe(p.getBairro()).trim();
        String cidade = safe(p.getCidade()).trim();
        String cep = safe(p.getCep()).trim();
        String uf = safe(p.getUf()).trim();

        StringBuilder sb = new StringBuilder();

        if (!rua.isBlank()) {
            sb.append(rua);
        }

        if (!numero.isBlank()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            sb.append(numero);
        }

        if (!complemento.isBlank()) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }

            sb.append(complemento);
        }

        if (!bairro.isBlank()) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }

            sb.append(bairro);
        }

        if (!cidade.isBlank()) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }

            sb.append(cidade);
        }

        if (!uf.isBlank()) {
            if (sb.length() > 0) {
                sb.append("/").append(uf);
            } else {
                sb.append(uf);
            }
        }

        if (!cep.isBlank()) {
            String cepFmt = ValidationUtils.formatCep(cep);

            if (!cepFmt.isBlank()) {
                if (sb.length() > 0) {
                    sb.append(" - CEP: ").append(cepFmt);
                } else {
                    sb.append("CEP: ").append(cepFmt);
                }
            }
        }

        if (sb.length() == 0) {
            return safe(p.getEndereco());
        }

        return sb.toString();
    }

    private String formatRg(String rg) {
        String digits = safe(rg).replaceAll("\\D", "");

        if (digits.isBlank()) {
            return "";
        }

        if (digits.length() > 7) {
            digits = digits.substring(0, 7);
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < digits.length(); i++) {
            if (i == 1 || i == 4) {
                sb.append('.');
            }

            sb.append(digits.charAt(i));
        }

        return sb.toString();
    }

    @FXML
    private void onAtualizarAnexos() {
        carregarAnexos();
    }

    private void carregarAnexos() {
        anexos.clear();

        if (paciente == null || paciente.getId() == null) {
            return;
        }

        List<AnexoPacienteItem> list = anexoDAO.listarPorPaciente(paciente.getId());
        anexos.addAll(list);

        if (btnAbrirAnexo != null) {
            btnAbrirAnexo.setDisable(true);
        }

        if (tableAnexos != null) {
            tableAnexos.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void onAbrirAnexo() {
        AnexoPacienteItem item =
                tableAnexos == null
                        ? null
                        : tableAnexos.getSelectionModel().getSelectedItem();

        if (item == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um anexo para abrir.").showAndWait();
            return;
        }

        try {
            if (item.isNuvem()) {
                anexoDAO.abrirNoNavegadorSignedUrl(item.getStoragePath());
                return;
            }

            File f = item.getFileLegado();

            if (f == null || !f.exists()) {
                new Alert(Alert.AlertType.ERROR, "Arquivo não encontrado. (anexo antigo/local)").showAndWait();
                return;
            }

            anexoDAO.abrirNoSistema(f);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Não foi possível abrir o arquivo.").showAndWait();
        }
    }

    @FXML
    private void onAtualizarAnamnese() {
        carregarAnamneses();
    }

    private void carregarAnamneses() {
        anamneses.clear();
        limparDetalheAnamnese();

        if (paciente == null || paciente.getId() == null) {
            return;
        }

        List<Anamnese> list = anamneseDAO.listarPorPaciente(paciente.getId());
        anamneses.addAll(list);

        if (tableAnamnese != null) {
            tableAnamnese.getSelectionModel().clearSelection();
        }
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
            txtJson.setText("");
        }

        if (tpJson != null) {
            tpJson.setExpanded(false);
            tpJson.setVisible(false);
            tpJson.setManaged(false);
        }
    }

    private void limparDetalheAnamnese() {
        if (txtObservacoes != null) {
            txtObservacoes.clear();
        }

        if (txtResumo != null) {
            txtResumo.clear();
        }

        if (txtJson != null) {
            txtJson.clear();
        }

        if (tpJson != null) {
            tpJson.setExpanded(false);
            tpJson.setVisible(false);
            tpJson.setManaged(false);
        }
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

        sb.append("Registro clínico\n");
        sb.append(tipo);

        if (!dataHora.isBlank()) {
            sb.append(" — ").append(dataHora);
        }

        sb.append("\n\n");

        appendSecaoTexto(sb, "Queixa principal", queixa);
        appendSecaoTexto(sb, "Evolução", evolucao);

        String sinais = montarLinhaSinaisVitais(pa, fc, fr, temp, peso, altura, spo2);
        appendSecaoTexto(sb, "Sinais vitais", sinais);

        boolean temHistorico =
                !antecedentes.isBlank()
                        || !medicacoes.isBlank()
                        || !alergias.isBlank()
                        || !cirurgias.isBlank();

        if (temHistorico) {
            sb.append("Histórico clínico\n");

            appendItem(sb, "Antecedentes", antecedentes);
            appendItem(sb, "Medicações", medicacoes);
            appendItem(sb, "Alergias", alergias);
            appendItem(sb, "Cirurgias", cirurgias);

            sb.append("\n");
        }

        boolean temHabitos =
                !tabagismo.isBlank()
                        || !alcool.isBlank()
                        || !sono.isBlank()
                        || !atividade.isBlank()
                        || !alimentacao.isBlank();

        if (temHabitos) {
            sb.append("Hábitos\n");

            appendItem(sb, "Tabagismo", tabagismo);
            appendItem(sb, "Álcool", alcool);
            appendItem(sb, "Sono", sono);
            appendItem(sb, "Atividade física", atividade);
            appendItem(sb, "Alimentação", alimentacao);

            sb.append("\n");
        }

        boolean temExame =
                !exameGeral.isBlank()
                        || !exameSeg.isBlank();

        if (temExame) {
            sb.append("Exame físico\n");

            appendItem(sb, "Geral", exameGeral);
            appendItem(sb, "Segmentar", exameSeg);

            sb.append("\n");
        }

        appendSecaoTexto(sb, "Observações", obs);

        String out = sb.toString().trim();

        if (out.isBlank()) {
            return "Sem detalhes para exibir neste registro.";
        }

        return out;
    }

    private void appendSecaoTexto(StringBuilder sb, String titulo, String texto) {
        String valor = safe(texto).trim();

        if (valor.isBlank()) {
            return;
        }

        sb.append(titulo).append("\n");
        sb.append(valor).append("\n\n");
    }

    private void appendItem(StringBuilder sb, String label, String valor) {
        String v = safe(valor).trim();

        if (v.isBlank()) {
            return;
        }

        sb.append("• ").append(label).append(": ").append(v).append("\n");
    }

    private String montarLinhaSinaisVitais(
            String pa,
            String fc,
            String fr,
            String temp,
            String peso,
            String altura,
            String spo2
    ) {
        StringBuilder s = new StringBuilder();

        appendCampo(s, "PA", pa, "mmHg");
        appendCampo(s, "FC", fc, "bpm");
        appendCampo(s, "FR", fr, "irpm");
        appendCampo(s, "Temp", temp, "°C");
        appendCampo(s, "Peso", peso, "kg");
        appendCampo(s, "Altura", altura, "m");
        appendCampo(s, "SpO₂", spo2, "%");

        return s.toString().trim();
    }

    private void appendCampo(StringBuilder sb, String label, String valor, String sufixo) {
        String v = safe(valor).trim();

        if (v.isBlank()) {
            return;
        }

        if (sb.length() > 0) {
            sb.append("   ");
        }

        sb.append(label).append(": ").append(v);

        if (sufixo != null && !sufixo.isBlank()) {
            sb.append(" ").append(sufixo);
        }
    }

    private String formatTipo(String tipo) {
        if (tipo == null) {
            return "";
        }

        String t = tipo.trim().toUpperCase();

        return switch (t) {
            case "ANAMNESE_INICIAL" -> "Anamnese inicial";
            case "EVOLUCAO" -> "Evolução";
            default -> tipo;
        };
    }

    @FXML
    private void onFechar() {
        if (lblNome != null && lblNome.getScene() != null) {
            lblNome.getScene().getWindow().hide();
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String getJsonField(String json, String field) {
        if (json == null || json.isBlank() || field == null || field.isBlank()) {
            return "";
        }

        String t = json.trim();

        String key = "\"" + field + "\":\"";
        int i = t.indexOf(key);

        if (i < 0) {
            return "";
        }

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

            if (c == '"') {
                break;
            }

            end++;
        }

        if (end <= start || end >= t.length()) {
            return "";
        }

        String raw = t.substring(start, end);

        return raw.replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trim();
    }
}