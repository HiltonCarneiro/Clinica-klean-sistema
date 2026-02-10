package br.com.clinica.controller;

import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Paciente;
import br.com.clinica.util.ValidationUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PacienteController {

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

    @FXML private TableView<Paciente> tablePacientes;
    @FXML private TableColumn<Paciente, String> colNome;
    @FXML private TableColumn<Paciente, String> colCpf;
    @FXML private TableColumn<Paciente, String> colTelefone;
    @FXML private TableColumn<Paciente, String> colDataNascimento;
    @FXML private TableColumn<Paciente, String> colAtivo;

    @FXML private Button btnInativar;
    @FXML private Button btnAtivar;
    @FXML private CheckBox chkMostrarInativos;

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final ObservableList<Paciente> pacientes = FXCollections.observableArrayList();

    private Paciente pacienteSelecionado;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private boolean atualizandoEditorData = false;

    private boolean atualizandoCpf = false;
    private boolean atualizandoCep = false;
    private boolean atualizandoTelefone = false;

    @FXML
    private void initialize() {
        configurarTabela();
        configurarMascaraData();
        configurarMascarasERegrasDeDigitacao();
        carregarPacientes();

        if (btnInativar != null) btnInativar.setDisable(true);
        if (btnAtivar != null) btnAtivar.setDisable(true);

        tablePacientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            pacienteSelecionado = newSel;
            if (newSel != null) {
                preencherFormulario(newSel);
                atualizarBotoesAtivo(newSel);
            } else {
                atualizarBotoesAtivo(null);
            }
        });
    }

    private void configurarTabela() {
        if (colNome != null) colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colCpf != null) colCpf.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().getCpf())));
        if (colTelefone != null) colTelefone.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().getTelefone())));

        if (colDataNascimento != null) {
            colDataNascimento.setCellValueFactory(c -> {
                LocalDate dt = c.getValue().getDataNascimento();
                return new SimpleStringProperty(dt == null ? "" : dt.format(formatter));
            });
        }

        if (colAtivo != null) {
            colAtivo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isAtivo() ? "Sim" : "Não"));
        }

        tablePacientes.setItems(pacientes);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void atualizarBotoesAtivo(Paciente p) {
        if (btnInativar == null || btnAtivar == null) return;

        if (p == null) {
            btnInativar.setDisable(true);
            btnAtivar.setDisable(true);
            return;
        }

        if (p.isAtivo()) {
            btnInativar.setDisable(false);
            btnAtivar.setDisable(true);
        } else {
            btnInativar.setDisable(true);
            btnAtivar.setDisable(false);
        }
    }

    private void configurarMascaraData() {
        if (dpDataNascimento == null) return;

        var editor = dpDataNascimento.getEditor();

        editor.textProperty().addListener((obs, oldText, newText) -> {
            if (atualizandoEditorData) return;

            String digits = newText == null ? "" : newText.replaceAll("\\D", "");
            if (digits.length() > 8) digits = digits.substring(0, 8);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                sb.append(digits.charAt(i));
                if ((i == 1 || i == 3) && i < digits.length() - 1) sb.append('/');
            }

            String formatado = sb.toString();

            atualizandoEditorData = true;
            editor.setText(formatado);
            editor.positionCaret(formatado.length());
            atualizandoEditorData = false;

            if (formatado.length() == 10) {
                try {
                    LocalDate dt = LocalDate.parse(formatado, formatter);
                    dpDataNascimento.setValue(dt);
                } catch (DateTimeParseException ignored) {
                }
            } else {
                dpDataNascimento.setValue(null);
            }
        });
    }

    private void configurarMascarasERegrasDeDigitacao() {
        // Nome: só letras + acentos + espaço + hífen + apóstrofo
        if (txtNome != null) {
            txtNome.setTextFormatter(new TextFormatter<String>(change -> {
                String newText = change.getControlNewText();
                if (newText.isEmpty()) return change;
                return newText.matches("^[A-Za-zÀ-ÿ'\\- ]*$") ? change : null;
            }));
        }

        // UF: 2 letras e uppercase
        if (txtUf != null) {
            txtUf.setTextFormatter(new TextFormatter<String>(change -> {
                String t = change.getControlNewText();
                if (t.length() > 2) return null;
                if (!t.matches("^[A-Za-z]*$")) return null;
                change.setText(change.getText().toUpperCase());
                return change;
            }));
        }

        // CPF máscara
        if (txtCpf != null) {
            txtCpf.textProperty().addListener((obs, o, n) -> {
                if (atualizandoCpf) return;
                atualizandoCpf = true;
                String formatted = ValidationUtils.formatCpf(n);
                txtCpf.setText(formatted);
                txtCpf.positionCaret(formatted.length());
                atualizandoCpf = false;
            });
        }

        // CEP máscara
        if (txtCep != null) {
            txtCep.textProperty().addListener((obs, o, n) -> {
                if (atualizandoCep) return;
                atualizandoCep = true;
                String formatted = ValidationUtils.formatCep(n);
                txtCep.setText(formatted);
                txtCep.positionCaret(formatted.length());
                atualizandoCep = false;
            });
        }

        // Telefone máscara
        if (txtTelefone != null) {
            txtTelefone.textProperty().addListener((obs, o, n) -> {
                if (atualizandoTelefone) return;
                atualizandoTelefone = true;
                String formatted = ValidationUtils.formatPhoneBr(n);
                txtTelefone.setText(formatted);
                txtTelefone.positionCaret(formatted.length());
                atualizandoTelefone = false;
            });
        }

        // Número: só números quando NÃO estiver "Sem número"
        if (txtNumero != null) {
            txtNumero.setTextFormatter(new TextFormatter<String>(change -> {
                if (chkSemNumero != null && chkSemNumero.isSelected()) {
                    return null; // bloqueia edição quando estiver "Sem número"
                }
                String t = change.getControlNewText();
                if (t.isEmpty()) return change;
                return t.matches("^\\d*$") ? change : null;
            }));
        }
    }

    @FXML
    private void onToggleSemNumero() {
        if (chkSemNumero == null || txtNumero == null) return;

        if (chkSemNumero.isSelected()) {
            txtNumero.setText("S/N");
            txtNumero.setDisable(true);
        } else {
            txtNumero.clear();
            txtNumero.setDisable(false);
        }
    }

    private void carregarPacientes() {
        boolean incluirInativos = chkMostrarInativos != null && chkMostrarInativos.isSelected();
        pacientes.setAll(pacienteDAO.listarTodos(incluirInativos));
        tablePacientes.refresh();
    }

    private void preencherFormulario(Paciente p) {
        txtNome.setText(p.getNome());
        txtCpf.setText(p.getCpf());
        dpDataNascimento.setValue(p.getDataNascimento());
        dpDataNascimento.getEditor().setText(p.getDataNascimento() == null ? "" : p.getDataNascimento().format(formatter));
        txtTelefone.setText(p.getTelefone());

        txtRua.setText(p.getRua());

        // Sem número
        String numero = safe(p.getNumero()).trim();
        if (chkSemNumero != null && txtNumero != null) {
            if ("S/N".equalsIgnoreCase(numero)) {
                chkSemNumero.setSelected(true);
                txtNumero.setText("S/N");
                txtNumero.setDisable(true);
            } else {
                chkSemNumero.setSelected(false);
                txtNumero.setDisable(false);
                txtNumero.setText(numero);
            }
        } else {
            txtNumero.setText(numero);
        }

        txtBairro.setText(p.getBairro());
        txtCidade.setText(p.getCidade());
        txtCep.setText(p.getCep());
        txtUf.setText(p.getUf());
        txtResponsavelLegal.setText(p.getResponsavelLegal());

        lblMensagem.setText("");
    }

    @FXML
    private void onNovo() {
        pacienteSelecionado = null;
        limparFormulario();
        atualizarBotoesAtivo(null);
        lblMensagem.setText("Novo paciente. Preencha os dados e clique em Salvar.");
        tablePacientes.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSalvar() {
        lblMensagem.setText("");

        String nome = safe(txtNome.getText()).trim();
        String cpf = safe(txtCpf.getText()).trim();
        LocalDate dataNascimento = dpDataNascimento.getValue();
        String telefone = safe(txtTelefone.getText()).trim();

        String rua = safe(txtRua.getText()).trim();
        String numero = safe(txtNumero.getText()).trim();
        boolean semNumero = chkSemNumero != null && chkSemNumero.isSelected();

        String bairro = safe(txtBairro.getText()).trim();
        String cidade = safe(txtCidade.getText()).trim();
        String cep = safe(txtCep.getText()).trim();
        String uf = safe(txtUf.getText()).trim();
        String responsavel = safe(txtResponsavelLegal.getText()).trim();

        // ===== Validações =====
        if (!ValidationUtils.isValidName(nome)) {
            lblMensagem.setText("Nome inválido. Use apenas letras e espaços (sem números/símbolos).");
            return;
        }

        if (!ValidationUtils.isValidCpf(cpf)) {
            lblMensagem.setText("CPF inválido.");
            return;
        }

        if (dataNascimento == null) {
            lblMensagem.setText("Data de nascimento é obrigatória.");
            return;
        }

        if (!ValidationUtils.isValidPhoneBr(telefone)) {
            lblMensagem.setText("Telefone inválido. Informe DDD + número (10 ou 11 dígitos).");
            return;
        }

        if (rua.isBlank() || cidade.isBlank()) {
            lblMensagem.setText("Endereço incompleto. Informe pelo menos Rua e Cidade.");
            return;
        }

        if (!semNumero && numero.isBlank()) {
            lblMensagem.setText("Informe o número do endereço ou marque 'Sem número'.");
            return;
        }

        if (semNumero) {
            numero = "S/N";
        }

        if (!cep.isBlank() && !ValidationUtils.isValidCep(cep)) {
            lblMensagem.setText("CEP inválido. Informe 8 dígitos.");
            return;
        }

        if (!uf.isBlank() && uf.length() != 2) {
            lblMensagem.setText("UF inválida. Use 2 letras (ex: CE, SP).");
            return;
        }

        if (responsavel.isBlank()) {
            lblMensagem.setText("Responsável legal é obrigatório.");
            return;
        }

        if (pacienteSelecionado == null) {
            Paciente p = new Paciente();
            p.setNome(nome);
            p.setCpf(ValidationUtils.formatCpf(cpf));
            p.setDataNascimento(dataNascimento);
            p.setTelefone(ValidationUtils.formatPhoneBr(telefone));

            p.setRua(rua);
            p.setNumero(numero);
            p.setBairro(bairro);
            p.setCidade(cidade);
            p.setCep(ValidationUtils.formatCep(cep));
            p.setUf(uf);
            p.setResponsavelLegal(responsavel);
            p.setAtivo(true);

            pacienteDAO.salvar(p);
            lblMensagem.setText("Paciente salvo com sucesso.");
        } else {
            pacienteSelecionado.setNome(nome);
            pacienteSelecionado.setCpf(ValidationUtils.formatCpf(cpf));
            pacienteSelecionado.setDataNascimento(dataNascimento);
            pacienteSelecionado.setTelefone(ValidationUtils.formatPhoneBr(telefone));

            pacienteSelecionado.setRua(rua);
            pacienteSelecionado.setNumero(numero);
            pacienteSelecionado.setBairro(bairro);
            pacienteSelecionado.setCidade(cidade);
            pacienteSelecionado.setCep(ValidationUtils.formatCep(cep));
            pacienteSelecionado.setUf(uf);
            pacienteSelecionado.setResponsavelLegal(responsavel);

            pacienteDAO.atualizar(pacienteSelecionado);
            lblMensagem.setText("Paciente atualizado com sucesso.");
        }

        limparFormulario();
        carregarPacientes();
        atualizarBotoesAtivo(null);
        tablePacientes.getSelectionModel().clearSelection();
    }

    @FXML
    private void onInativar() {
        if (pacienteSelecionado == null) {
            lblMensagem.setText("Selecione um paciente na tabela para inativar.");
            return;
        }

        if (!pacienteSelecionado.isAtivo()) {
            lblMensagem.setText("Paciente já está inativo.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Inativar paciente");
        alert.setHeaderText("Confirma inativação?");
        alert.setContentText("Paciente: " + pacienteSelecionado.getNome());

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                pacienteDAO.inativar(pacienteSelecionado.getId());
                lblMensagem.setText("Paciente inativado.");
                carregarPacientes();
                limparFormulario();
                pacienteSelecionado = null;
                atualizarBotoesAtivo(null);
            }
        });
    }

    @FXML
    private void onAtivar() {
        if (pacienteSelecionado == null) {
            lblMensagem.setText("Selecione um paciente na tabela para reativar.");
            return;
        }

        if (pacienteSelecionado.isAtivo()) {
            lblMensagem.setText("Paciente já está ativo.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reativar paciente");
        alert.setHeaderText("Confirma reativação?");
        alert.setContentText("Paciente: " + pacienteSelecionado.getNome());

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                pacienteDAO.ativar(pacienteSelecionado.getId());
                lblMensagem.setText("Paciente reativado.");
                carregarPacientes();
                limparFormulario();
                pacienteSelecionado = null;
                atualizarBotoesAtivo(null);
            }
        });
    }

    @FXML
    private void onLimpar() {
        limparFormulario();
        lblMensagem.setText("");
        tablePacientes.getSelectionModel().clearSelection();
        pacienteSelecionado = null;
        atualizarBotoesAtivo(null);
    }

    @FXML
    private void onAtualizarLista() {
        carregarPacientes();
        tablePacientes.getSelectionModel().clearSelection();
        pacienteSelecionado = null;
        atualizarBotoesAtivo(null);
    }

    private void limparFormulario() {
        txtNome.clear();
        txtCpf.clear();
        dpDataNascimento.setValue(null);
        dpDataNascimento.getEditor().clear();
        txtTelefone.clear();
        txtRua.clear();
        txtNumero.clear();

        if (chkSemNumero != null) chkSemNumero.setSelected(false);
        if (txtNumero != null) txtNumero.setDisable(false);

        txtBairro.clear();
        txtCidade.clear();
        txtCep.clear();
        txtUf.clear();
        txtResponsavelLegal.clear();
    }

    // =========================
    //     BUSCA CEP (ViaCEP)
    // =========================

    @FXML
    private void onBuscarCep() {
        lblMensagem.setText("");

        String cep = safe(txtCep.getText()).replaceAll("\\D", "");
        if (cep.length() != 8) {
            lblMensagem.setText("Informe um CEP com 8 dígitos.");
            return;
        }

        String url = "https://viacep.com.br/ws/" + cep + "/json/";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                lblMensagem.setText("Erro ao consultar CEP (HTTP " + response.statusCode() + ").");
                return;
            }

            String body = response.body();
            if (body.contains("\"erro\": true")) {
                lblMensagem.setText("CEP não encontrado.");
                return;
            }

            String logradouro = extrairCampoJson(body, "logradouro");
            String bairro = extrairCampoJson(body, "bairro");
            String cidade = extrairCampoJson(body, "localidade");
            String uf = extrairCampoJson(body, "uf");

            if (logradouro != null && !logradouro.isBlank()) txtRua.setText(logradouro);
            if (bairro != null && !bairro.isBlank()) txtBairro.setText(bairro);
            if (cidade != null && !cidade.isBlank()) txtCidade.setText(cidade);
            if (uf != null && !uf.isBlank()) txtUf.setText(uf);

            lblMensagem.setText("Endereço preenchido a partir do CEP.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            lblMensagem.setText("Erro ao consultar CEP.");
        }
    }

    private String extrairCampoJson(String json, String campo) {
        String chave = "\"" + campo + "\":";
        int idx = json.indexOf(chave);
        if (idx == -1) return null;

        int aspas1 = json.indexOf('"', idx + chave.length());
        if (aspas1 == -1) return null;
        int aspas2 = json.indexOf('"', aspas1 + 1);
        if (aspas2 == -1) return null;

        return json.substring(aspas1 + 1, aspas2);
    }
}