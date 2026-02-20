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
import javafx.scene.control.*;

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
    @FXML private TextArea txtJson;

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
        if (txtJson != null) txtJson.setEditable(false);
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

        if (txtObservacoes != null) {
            txtObservacoes.setText(safe(a.getObservacoes()));
        }

        if (txtJson != null) {
            String raw = safe(a.getDadosJson());
            txtJson.setText(prettyJson(raw));
        }
    }

    private void limparDetalheAnamnese() {
        if (txtObservacoes != null) txtObservacoes.clear();
        if (txtJson != null) txtJson.clear();
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