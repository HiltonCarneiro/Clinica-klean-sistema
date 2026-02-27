package br.com.clinica.controller;

import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Paciente;
import br.com.clinica.util.ValidationUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PacienteController {

    // ====== NAVEGAÇÃO ======
    @FXML private VBox boxEscolha;
    @FXML private VBox boxBusca;
    @FXML private VBox boxCadastro;
    @FXML private ScrollPane scrollCadastro;

    // ====== BUSCA ======
    @FXML private ComboBox<String> cbFiltro;
    @FXML private TextField txtBusca;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimparBusca;

    @FXML private TableView<Paciente> tableBusca;
    @FXML private TableColumn<Paciente, String> colBuscaNome;
    @FXML private TableColumn<Paciente, String> colBuscaCpf;
    @FXML private TableColumn<Paciente, String> colBuscaTelefone;
    @FXML private TableColumn<Paciente, String> colBuscaDataNascimento;
    @FXML private TableColumn<Paciente, String> colBuscaAtivo;

    @FXML private CheckBox chkMostrarInativosBusca;

    // ✅ botões da busca
    @FXML private Button btnEditarSelecionado;
    @FXML private Button btnHistoricoSelecionado;

    // ====== CADASTRO ======
    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private DatePicker dpDataNascimento;
    @FXML private TextField txtTelefone;

    @FXML private TextField txtRua;
    @FXML private TextField txtNumero;
    @FXML private CheckBox chkSemNumero;

    @FXML private TextField txtBairro;
    @FXML private TextField txtCidade;
    @FXML private TextField txtCep;
    @FXML private TextField txtUf;
    @FXML private TextField txtResponsavelLegal;

    @FXML private Label lblMensagem;

    @FXML private Button btnInativar;
    @FXML private Button btnAtivar;
    @FXML private CheckBox chkMostrarInativos;

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final ObservableList<Paciente> pacientes = FXCollections.observableArrayList();

    private Paciente pacienteSelecionado = null;

    private final DateTimeFormatter fmtBr = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private boolean updatingDateEditor = false;

    @FXML
    private void initialize() {
        showTelaEscolha();

        setupTabelaBusca();
        setupMasksAndRules();
        setupDatePickerMaskFix();

        setMensagem("");
        setBotoesAtivo(null);

        if (btnEditarSelecionado != null) btnEditarSelecionado.setDisable(true);
        if (btnHistoricoSelecionado != null) btnHistoricoSelecionado.setDisable(true);
    }

    // =========================================================
    // NAVEGAÇÃO
    // =========================================================

    @FXML
    private void voltarEscolha() {
        showTelaEscolha();
        setMensagem("");
        clearSelectionBusca();
    }

    @FXML
    private void irParaCadastro() {
        show(boxEscolha, false);
        show(boxBusca, false);
        show(scrollCadastro, true);
    }

    @FXML
    private void irParaBusca() {
        show(boxEscolha, false);
        show(scrollCadastro, false);
        show(boxBusca, true);
        onBuscarPaciente();
    }

    private void showTelaEscolha() {
        show(scrollCadastro, false);
        show(boxBusca, false);
        show(boxEscolha, true);
    }

    private void show(Node node, boolean visible) {
        if (node == null) return;
        node.setVisible(visible);
        node.setManaged(visible);
    }

    // =========================================================
    // BUSCA
    // =========================================================

    private void setupTabelaBusca() {
        if (tableBusca == null) return;

        tableBusca.setItems(pacientes);

        if (colBuscaNome != null) colBuscaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colBuscaCpf != null) colBuscaCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));

        if (colBuscaTelefone != null) {
            colBuscaTelefone.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().getTelefone())));
        }

        if (colBuscaDataNascimento != null) {
            colBuscaDataNascimento.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getDataNascimento() == null ? "" : c.getValue().getDataNascimento().format(fmtBr)
            ));
        }

        if (colBuscaAtivo != null) {
            colBuscaAtivo.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().isAtivo() ? "SIM" : "NÃO"
            ));
        }

        tableBusca.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            pacienteSelecionado = sel;
            boolean tem = sel != null;

            if (btnEditarSelecionado != null) btnEditarSelecionado.setDisable(!tem);
            if (btnHistoricoSelecionado != null) btnHistoricoSelecionado.setDisable(!tem);

            setBotoesAtivo(sel);
        });

        if (cbFiltro != null) {
            cbFiltro.setItems(FXCollections.observableArrayList("Nome", "CPF", "Telefone"));
            cbFiltro.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onBuscarPaciente() {
        String query = txtBusca != null && txtBusca.getText() != null ? txtBusca.getText().trim() : "";
        boolean incluirInativos = chkMostrarInativosBusca != null && chkMostrarInativosBusca.isSelected();

        List<Paciente> lista = pacienteDAO.listarTodos(incluirInativos);

        if (query.isBlank()) {
            pacientes.setAll(lista);
        } else {
            String qLower = query.toLowerCase();
            String qDigits = digits(query);

            pacientes.setAll(lista.stream().filter(p ->
                    safe(p.getNome()).toLowerCase().contains(qLower)
                            || safe(p.getCpf()).contains(qDigits)
                            || safe(p.getTelefone()).contains(qDigits)
            ).toList());
        }

        if (tableBusca != null) tableBusca.refresh();
        clearSelectionBusca();
    }

    @FXML
    private void onLimparBusca() {
        if (txtBusca != null) txtBusca.clear();
        onBuscarPaciente();
    }

    @FXML
    private void onAtualizarLista() {
        onBuscarPaciente();
    }

    private void clearSelectionBusca() {
        pacienteSelecionado = null;
        if (tableBusca != null) tableBusca.getSelectionModel().clearSelection();
        if (btnEditarSelecionado != null) btnEditarSelecionado.setDisable(true);
        if (btnHistoricoSelecionado != null) btnHistoricoSelecionado.setDisable(true);
        setBotoesAtivo(null);
    }

    // =========================================================
    // MÁSCARAS / REGRAS
    // =========================================================

    private void setupMasksAndRules() {
        if (txtBusca != null) {
            txtBusca.setOnAction(e -> onBuscarPaciente());
        }

        if (txtCpf != null) {
            txtCpf.textProperty().addListener((obs, old, val) -> {
                if (val == null) return;
                String dig = digits(val);
                if (dig.length() > 11) dig = dig.substring(0, 11);
                String fmt = ValidationUtils.formatCpf(dig);
                if (!fmt.equals(val)) {
                    txtCpf.setText(fmt);
                    txtCpf.positionCaret(fmt.length());
                }
            });
        }

        if (txtTelefone != null) {
            // ✅ ALTERAÇÃO NECESSÁRIA: máscara sem travar ao apagar "(", ")" e "-"
            txtTelefone.setTextFormatter(new TextFormatter<String>(change -> {
                if (!change.isContentChange()) return change;

                String oldText = change.getControlText() == null ? "" : change.getControlText();
                String newText = change.getControlNewText() == null ? "" : change.getControlNewText();

                String oldDigits = digits(oldText);
                String newDigits = digits(newText);

                // Limite: DDD + 9 dígitos
                if (newDigits.length() > 11) return null;

                boolean isDeletion = (change.getText() != null && change.getText().isEmpty());

                // Se tentou apagar um caractere da máscara, os dígitos não mudam.
                // Então removemos um dígito "real" para o usuário conseguir apagar.
                if (isDeletion && oldDigits.equals(newDigits)) {
                    int pos = change.getRangeStart(); // posição no texto antigo onde ocorreu a deleção

                    // índice do dígito imediatamente antes de 'pos'
                    int digitIndex = 0;
                    for (int i = 0; i < Math.min(pos, oldText.length()); i++) {
                        if (Character.isDigit(oldText.charAt(i))) digitIndex++;
                    }
                    int removeAt = Math.max(0, digitIndex - 1);

                    if (!oldDigits.isEmpty() && removeAt < oldDigits.length()) {
                        newDigits = oldDigits.substring(0, removeAt) + oldDigits.substring(removeAt + 1);
                    }
                }

                String formatted = ValidationUtils.formatPhoneBr(newDigits);

                int oldLen = oldText.length();
                change.setText(formatted);
                change.setRange(0, oldLen);

                // cursor no fim (evita comportamento estranho)
                change.selectRange(formatted.length(), formatted.length());
                return change;
            }));
        }

        if (txtCep != null) {
            txtCep.textProperty().addListener((obs, old, val) -> {
                if (val == null) return;
                String dig = digits(val);
                if (dig.length() > 8) dig = dig.substring(0, 8);
                String fmt = ValidationUtils.formatCep(dig);
                if (!fmt.equals(val)) {
                    txtCep.setText(fmt);
                    txtCep.positionCaret(fmt.length());
                }
            });

            txtCep.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    String cepDigits = digits(safe(txtCep.getText()));
                    if (cepDigits.length() == 8) buscarCep(cepDigits);
                }
            });
        }
    }

    private void setupDatePickerMaskFix() {
        if (dpDataNascimento == null) return;

        dpDataNascimento.setEditable(true);

        TextField editor = dpDataNascimento.getEditor();
        if (editor == null) return;

        editor.setPromptText("dd/MM/aaaa");

        // ✅ ALTERAÇÃO NECESSÁRIA: máscara dd/MM/aaaa + limite + apagar sem travar
        editor.setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.isContentChange()) return change;

            String oldText = change.getControlText() == null ? "" : change.getControlText();
            String newText = change.getControlNewText() == null ? "" : change.getControlNewText();

            String digits = newText.replaceAll("\\D", "");
            if (digits.length() > 8) return null;

            if (digits.isEmpty()) {
                int oldLen = oldText.length();
                change.setText("");
                change.setRange(0, oldLen);
                change.selectRange(0, 0);
                return change;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                sb.append(digits.charAt(i));
                if (i == 1 && digits.length() > 2) sb.append('/');
                if (i == 3 && digits.length() > 4) sb.append('/');
            }

            String formatted = sb.toString();
            int oldLen = oldText.length();
            change.setText(formatted);
            change.setRange(0, oldLen);

            change.selectRange(formatted.length(), formatted.length());
            return change;
        }));

        editor.focusedProperty().addListener((obs, was, is) -> {
            if (!is) {
                String t = editor.getText() == null ? "" : editor.getText().trim();

                if (t.isBlank()) {
                    dpDataNascimento.setValue(null);
                    return;
                }

                String digits = t.replaceAll("\\D", "");
                if (digits.length() < 8) {
                    setMensagem("Data de nascimento incompleta. Use dd/MM/aaaa.");
                    dpDataNascimento.setValue(null);
                    return;
                }

                try {
                    LocalDate dt = LocalDate.parse(t, fmtBr);

                    if (dt.isAfter(LocalDate.now())) {
                        setMensagem("Data de nascimento não pode ser no futuro.");
                        dpDataNascimento.setValue(null);
                        return;
                    }

                    dpDataNascimento.setValue(dt);
                    editor.setText(dt.format(fmtBr));
                } catch (DateTimeParseException e) {
                    setMensagem("Data de nascimento inválida.");
                    dpDataNascimento.setValue(null);
                }
            }
        });
    }

    private void buscarCep(String cepDigits) {
        Platform.runLater(() -> setMensagem("Buscando CEP..."));

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://viacep.com.br/ws/" + cepDigits + "/json/"))
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::preencherEnderecoViaCep)
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        Platform.runLater(() -> setMensagem("Não foi possível buscar o CEP."));
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
            setMensagem("Erro ao buscar CEP.");
        }
    }

    private void preencherEnderecoViaCep(String json) {
        // mantém seu comportamento original (não alterei)
        Platform.runLater(() -> setMensagem(""));
    }

    // =========================================================
    // AÇÕES CADASTRO
    // =========================================================

    @FXML
    private void onNovo() {
        limparFormulario();
        pacienteSelecionado = null;
        setBotoesAtivo(null);
        setMensagem("");
        irParaCadastro();
    }

    @FXML
    private void onEditarSelecionado() {
        if (pacienteSelecionado == null) {
            setMensagem("Selecione um paciente na lista.");
            return;
        }
        preencherFormulario(pacienteSelecionado);
        irParaCadastro();
    }

    @FXML
    private void onSalvar() {
        try {
            Paciente p = (pacienteSelecionado != null) ? pacienteSelecionado : new Paciente();

            p.setNome(safe(txtNome != null ? txtNome.getText() : "").trim());
            p.setCpf(safe(txtCpf != null ? txtCpf.getText() : "").trim());
            p.setTelefone(safe(txtTelefone != null ? txtTelefone.getText() : "").trim());
            p.setDataNascimento(dpDataNascimento != null ? dpDataNascimento.getValue() : null);

            p.setRua(safe(txtRua != null ? txtRua.getText() : "").trim());
            p.setNumero(safe(txtNumero != null ? txtNumero.getText() : "").trim());
            p.setBairro(safe(txtBairro != null ? txtBairro.getText() : "").trim());
            p.setCidade(safe(txtCidade != null ? txtCidade.getText() : "").trim());
            p.setCep(safe(txtCep != null ? txtCep.getText() : "").trim());
            p.setUf(safe(txtUf != null ? txtUf.getText() : "").trim());
            p.setResponsavelLegal(safe(txtResponsavelLegal != null ? txtResponsavelLegal.getText() : "").trim());

            if (p.getNome() == null || p.getNome().isBlank()) {
                setMensagem("Nome é obrigatório.");
                return;
            }

            pacienteDAO.salvar(p);

            setMensagem("Paciente salvo com sucesso.");
            limparFormulario();
            pacienteSelecionado = null;
            setBotoesAtivo(null);
            irParaBusca();

        } catch (Exception e) {
            e.printStackTrace();
            setMensagem("Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    private void onInativar() {
        if (pacienteSelecionado == null || pacienteSelecionado.getId() == null) {
            setMensagem("Selecione um paciente.");
            return;
        }
        pacienteDAO.inativar(pacienteSelecionado.getId());
        setMensagem("Paciente inativado.");
        onBuscarPaciente();
    }

    @FXML
    private void onAtivar() {
        if (pacienteSelecionado == null || pacienteSelecionado.getId() == null) {
            setMensagem("Selecione um paciente.");
            return;
        }
        pacienteDAO.ativar(pacienteSelecionado.getId());
        setMensagem("Paciente ativado.");
        onBuscarPaciente();
    }

    @FXML
    private void onVerHistoricoSelecionado() {
        if (pacienteSelecionado == null) {
            setMensagem("Selecione um paciente.");
            return;
        }

        // mantém seu comportamento original (não alterei)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/historico-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Histórico do paciente");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            setMensagem("Erro ao abrir histórico.");
        }
    }

    // =========================================================
    // UTIL
    // =========================================================

    private void preencherFormulario(Paciente p) {
        if (p == null) return;

        if (txtNome != null) txtNome.setText(safe(p.getNome()));
        if (txtCpf != null) txtCpf.setText(safe(p.getCpf()));
        if (txtTelefone != null) txtTelefone.setText(safe(p.getTelefone()));
        if (dpDataNascimento != null) dpDataNascimento.setValue(p.getDataNascimento());

        if (txtRua != null) txtRua.setText(safe(p.getRua()));
        if (txtNumero != null) txtNumero.setText(safe(p.getNumero()));
        if (txtBairro != null) txtBairro.setText(safe(p.getBairro()));
        if (txtCidade != null) txtCidade.setText(safe(p.getCidade()));
        if (txtCep != null) txtCep.setText(safe(p.getCep()));
        if (txtUf != null) txtUf.setText(safe(p.getUf()));
        if (txtResponsavelLegal != null) txtResponsavelLegal.setText(safe(p.getResponsavelLegal()));
    }

    private void limparFormulario() {
        if (txtNome != null) txtNome.clear();
        if (txtCpf != null) txtCpf.clear();
        if (txtTelefone != null) txtTelefone.clear();

        if (dpDataNascimento != null) {
            dpDataNascimento.setValue(null);
            if (dpDataNascimento.getEditor() != null) dpDataNascimento.getEditor().clear();
        }

        if (txtRua != null) txtRua.clear();
        if (txtNumero != null) txtNumero.clear();
        if (txtBairro != null) txtBairro.clear();
        if (txtCidade != null) txtCidade.clear();
        if (txtCep != null) txtCep.clear();
        if (txtUf != null) txtUf.clear();
        if (txtResponsavelLegal != null) txtResponsavelLegal.clear();
    }

    private void setBotoesAtivo(Paciente p) {
        boolean temSelecionado = p != null && p.getId() != null;
        if (btnInativar != null) btnInativar.setDisable(!temSelecionado || (p != null && !p.isAtivo()));
        if (btnAtivar != null) btnAtivar.setDisable(!temSelecionado || (p != null && p.isAtivo()));
    }

    private void setMensagem(String msg) {
        if (lblMensagem != null) lblMensagem.setText(msg == null ? "" : msg);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String digits(String s) {
        if (s == null) return "";
        return s.replaceAll("\\D", "");
    }

    private int idade(LocalDate nascimento) {
        if (nascimento == null) return 0;
        return Period.between(nascimento, LocalDate.now()).getYears();
    }
}