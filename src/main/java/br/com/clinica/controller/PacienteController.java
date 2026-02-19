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
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

public class PacienteController {

    // ====== NAVEGAÇÃO (3 telas no StackPane) ======
    @FXML private VBox boxEscolha;
    @FXML private ScrollPane scrollCadastro;
    @FXML private VBox boxBusca;

    // ====== BUSCA ======
    @FXML private TextField txtBusca;
    @FXML private TableView<Paciente> tableBusca;
    @FXML private TableColumn<Paciente, String> colBuscaNome;
    @FXML private TableColumn<Paciente, String> colBuscaCpf;
    @FXML private TableColumn<Paciente, String> colBuscaTelefone;
    @FXML private TableColumn<Paciente, String> colBuscaDataNascimento;
    @FXML private TableColumn<Paciente, String> colBuscaAtivo;
    @FXML private CheckBox chkMostrarInativosBusca;

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

    // ====== DADOS ======
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final ObservableList<Paciente> pacientes = FXCollections.observableArrayList();
    private Paciente pacienteSelecionado;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private boolean atualizandoEditorData = false;

    // flags para máscara com caret
    private boolean atualizandoCpf = false;
    private boolean atualizandoCep = false;
    private boolean atualizandoTelefone = false;

    @FXML
    private void initialize() {
        // começa na tela de escolha
        mostrarEscolha();

        // cadastro: máscaras/validações
        configurarMascaraData();
        configurarMascarasERegrasDeDigitacao();

        // busca: tabela + listeners
        configurarTabelaBusca();

        // botões ativar/inativar
        if (btnInativar != null) btnInativar.setDisable(true);
        if (btnAtivar != null) btnAtivar.setDisable(true);
    }

    // =========================================================
    //                  NAVEGAÇÃO ENTRE TELAS
    // =========================================================

    @FXML
    private void voltarEscolha() {
        mostrarEscolha();
        if (lblMensagem != null) lblMensagem.setText("");
    }

    @FXML
    private void irParaCadastro() {
        if (boxEscolha != null) { boxEscolha.setVisible(false); boxEscolha.setManaged(false); }
        if (boxBusca != null) { boxBusca.setVisible(false); boxBusca.setManaged(false); }
        if (scrollCadastro != null) { scrollCadastro.setVisible(true); scrollCadastro.setManaged(true); }
    }

    @FXML
    private void irParaBusca() {
        if (boxEscolha != null) { boxEscolha.setVisible(false); boxEscolha.setManaged(false); }
        if (scrollCadastro != null) { scrollCadastro.setVisible(false); scrollCadastro.setManaged(false); }
        if (boxBusca != null) { boxBusca.setVisible(true); boxBusca.setManaged(true); }

        onBuscarPaciente(); // carrega lista
    }

    private void mostrarEscolha() {
        if (scrollCadastro != null) { scrollCadastro.setVisible(false); scrollCadastro.setManaged(false); }
        if (boxBusca != null) { boxBusca.setVisible(false); boxBusca.setManaged(false); }
        if (boxEscolha != null) { boxEscolha.setVisible(true); boxEscolha.setManaged(true); }
    }

    // =========================================================
    //                          BUSCA
    // =========================================================

    private void configurarTabelaBusca() {
        if (tableBusca == null) return;

        if (colBuscaNome != null) colBuscaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colBuscaCpf != null) colBuscaCpf.setCellValueFactory(c -> new SimpleStringProperty(
                ValidationUtils.formatCpf(safe(c.getValue().getCpf()))
        ));
        if (colBuscaTelefone != null) colBuscaTelefone.setCellValueFactory(c -> new SimpleStringProperty(
                ValidationUtils.formatPhoneBr(safe(c.getValue().getTelefone()))
        ));
        if (colBuscaDataNascimento != null) {
            colBuscaDataNascimento.setCellValueFactory(c -> {
                LocalDate dt = c.getValue().getDataNascimento();
                return new SimpleStringProperty(dt == null ? "" : dt.format(formatter));
            });
        }
        if (colBuscaAtivo != null) {
            colBuscaAtivo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isAtivo() ? "Sim" : "Não"));
        }

        tableBusca.setItems(pacientes);

        // ao clicar em alguém -> vai pro cadastro e preenche
        tableBusca.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            pacienteSelecionado = newSel;
            if (newSel != null) {
                irParaCadastro();
                preencherFormulario(newSel);
                atualizarBotoesAtivo(newSel);
            } else {
                atualizarBotoesAtivo(null);
            }
        });
    }

    @FXML
    private void onBuscarPaciente() {
        String q = txtBusca != null ? txtBusca.getText().trim() : "";
        boolean incluirInativos = chkMostrarInativosBusca != null && chkMostrarInativosBusca.isSelected();

        var lista = pacienteDAO.listarTodos(incluirInativos);

        String qDigits = apenasDigitos(q);
        String qLower = q.toLowerCase();

        if (q.isBlank()) {
            pacientes.setAll(lista);
        } else {
            pacientes.setAll(lista.stream().filter(p ->
                    safe(p.getNome()).toLowerCase().contains(qLower) ||
                            safe(p.getCpf()).contains(qDigits) ||
                            safe(p.getTelefone()).contains(qDigits)
            ).toList());
        }

        if (tableBusca != null) tableBusca.refresh();
    }

    @FXML
    private void onLimparBusca() {
        if (txtBusca != null) txtBusca.clear();
        if (chkMostrarInativosBusca != null) chkMostrarInativosBusca.setSelected(false);
        onBuscarPaciente();
    }

    // =========================================================
    //                        CADASTRO
    // =========================================================

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
                } catch (DateTimeParseException ignored) { }
            } else {
                dpDataNascimento.setValue(null);
            }
        });
    }

    private void configurarMascarasERegrasDeDigitacao() {
        // Nome
        if (txtNome != null) {
            txtNome.setTextFormatter(new TextFormatter<String>(change -> {
                String newText = change.getControlNewText();
                if (newText.isEmpty()) return change;
                return newText.matches("^[A-Za-zÀ-ÿ'\\- ]*$") ? change : null;
            }));
        }

        // UF (2 letras)
        if (txtUf != null) {
            txtUf.setTextFormatter(new TextFormatter<String>(change -> {
                String t = change.getControlNewText();
                if (t.length() > 2) return null;
                if (!t.matches("^[A-Za-z]*$")) return null;
                change.setText(change.getText().toUpperCase());
                return change;
            }));
        }

        // CPF / CEP / Telefone
        aplicarMascaraComCaret(txtCpf, ValidationUtils::formatCpf);
        aplicarMascaraComCaret(txtCep, ValidationUtils::formatCep);
        aplicarMascaraComCaret(txtTelefone, ValidationUtils::formatPhoneBr);

        // Número
        if (txtNumero != null) {
            txtNumero.setTextFormatter(new TextFormatter<String>(change -> {
                if (chkSemNumero != null && chkSemNumero.isSelected()) {
                    return null;
                }
                String t = change.getControlNewText();
                if (t.isEmpty()) return change;
                return t.matches("^\\d*$") ? change : null;
            }));
        }
    }

    private void aplicarMascaraComCaret(TextField field, Function<String, String> formatterFn) {
        if (field == null) return;

        field.setTextFormatter(new TextFormatter<String>(change -> {
            if (field == txtCpf && atualizandoCpf) return change;
            if (field == txtCep && atualizandoCep) return change;
            if (field == txtTelefone && atualizandoTelefone) return change;

            String oldText = change.getControlText();
            String newText = change.getControlNewText();

            int oldDigitsBeforeCaret = contarDigitos(oldText, change.getCaretPosition());
            int digitsBeforeCaret = contarDigitos(newText, change.getCaretPosition());

            boolean isDeletion = change.isDeleted() && change.getText().isEmpty();

            if (isDeletion && digitsBeforeCaret == oldDigitsBeforeCaret && oldDigitsBeforeCaret > 0) {
                String rawDigits = apenasDigitos(newText);
                if (rawDigits.length() >= oldDigitsBeforeCaret) {
                    rawDigits = rawDigits.substring(0, oldDigitsBeforeCaret - 1) + rawDigits.substring(oldDigitsBeforeCaret);
                    newText = rawDigits;
                    digitsBeforeCaret = oldDigitsBeforeCaret - 1;
                }
            }

            String formatted = formatterFn.apply(newText);
            if (formatted == null) formatted = "";

            int caret = posicaoDoCaretPorQtdDigitos(formatted, digitsBeforeCaret);

            change.setText(formatted);
            change.setRange(0, oldText.length());
            change.setCaretPosition(caret);
            change.setAnchor(caret);
            return change;
        }));
    }

    private int contarDigitos(String text, int upToIndexExclusive) {
        if (text == null) return 0;
        int end = Math.max(0, Math.min(upToIndexExclusive, text.length()));
        int c = 0;
        for (int i = 0; i < end; i++) {
            if (Character.isDigit(text.charAt(i))) c++;
        }
        return c;
    }

    private int posicaoDoCaretPorQtdDigitos(String formatted, int digitsBeforeCaret) {
        if (formatted == null || formatted.isEmpty() || digitsBeforeCaret <= 0) return 0;

        int digits = 0;
        for (int i = 0; i < formatted.length(); i++) {
            if (Character.isDigit(formatted.charAt(i))) {
                digits++;
                if (digits >= digitsBeforeCaret) {
                    return i + 1;
                }
            }
        }
        return formatted.length();
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
        if (tableBusca != null) tableBusca.refresh();
    }

    private void preencherFormulario(Paciente p) {
        if (p == null) return;

        txtNome.setText(p.getNome());
        txtCpf.setText(p.getCpf());
        dpDataNascimento.setValue(p.getDataNascimento());
        dpDataNascimento.getEditor().setText(p.getDataNascimento() == null ? "" : p.getDataNascimento().format(formatter));
        txtTelefone.setText(p.getTelefone());

        txtRua.setText(p.getRua());

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
        txtResponsavelLegal.setText(safe(p.getResponsavelLegal()));

        if (lblMensagem != null) lblMensagem.setText("");
    }

    @FXML
    private void onNovo() {
        pacienteSelecionado = null;
        limparFormulario();
        atualizarBotoesAtivo(null);
        if (lblMensagem != null) lblMensagem.setText("Novo paciente. Preencha os dados e clique em Salvar.");
        if (tableBusca != null) tableBusca.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSalvar() {
        if (lblMensagem != null) lblMensagem.setText("");

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

        if (!ValidationUtils.isValidName(nome)) {
            if (lblMensagem != null) lblMensagem.setText("Nome inválido. Use apenas letras e espaços.");
            return;
        }

        if (!cpf.isBlank() && !ValidationUtils.isValidCpf(cpf)) {
            if (lblMensagem != null) lblMensagem.setText("CPF inválido.");
            return;
        }

        if (dataNascimento == null) {
            if (lblMensagem != null) lblMensagem.setText("Data de nascimento é obrigatória.");
            return;
        }

        if (!telefone.isBlank() && !ValidationUtils.isValidPhoneBr(telefone)) {
            if (lblMensagem != null) lblMensagem.setText("Telefone inválido. Informe DDD + número.");
            return;
        }

        if (rua.isBlank() || cidade.isBlank()) {
            if (lblMensagem != null) lblMensagem.setText("Endereço incompleto. Informe pelo menos Rua e Cidade.");
            return;
        }

        if (!semNumero && numero.isBlank()) {
            if (lblMensagem != null) lblMensagem.setText("Informe o número do endereço ou marque 'Sem número'.");
            return;
        }

        if (semNumero) numero = "S/N";

        if (!cep.isBlank() && !ValidationUtils.isValidCep(cep)) {
            if (lblMensagem != null) lblMensagem.setText("CEP inválido. Informe 8 dígitos.");
            return;
        }

        if (!uf.isBlank() && uf.length() != 2) {
            if (lblMensagem != null) lblMensagem.setText("UF inválida. Use 2 letras (ex: CE, SP).");
            return;
        }

        int idade = Period.between(dataNascimento, LocalDate.now()).getYears();
        boolean menorDeIdade = idade < 18;
        if (menorDeIdade && responsavel.isBlank()) {
            if (lblMensagem != null) lblMensagem.setText("Responsável legal é obrigatório para menores de 18 anos.");
            return;
        }

        try {
            if (pacienteSelecionado == null) {
                Paciente p = new Paciente();
                p.setNome(nome);
                p.setCpf(apenasDigitosOuNull(cpf));
                p.setDataNascimento(dataNascimento);
                p.setTelefone(apenasDigitosOuNull(telefone));

                p.setRua(rua);
                p.setNumero(numero);
                p.setBairro(bairro);
                p.setCidade(cidade);
                p.setCep(apenasDigitosOuNull(cep));
                p.setUf(uf);
                p.setResponsavelLegal(responsavel.isBlank() ? null : responsavel);
                p.setAtivo(true);

                pacienteDAO.salvar(p);
                if (lblMensagem != null) lblMensagem.setText("Paciente salvo com sucesso.");
            } else {
                pacienteSelecionado.setNome(nome);
                pacienteSelecionado.setCpf(apenasDigitosOuNull(cpf));
                pacienteSelecionado.setDataNascimento(dataNascimento);
                pacienteSelecionado.setTelefone(apenasDigitosOuNull(telefone));

                pacienteSelecionado.setRua(rua);
                pacienteSelecionado.setNumero(numero);
                pacienteSelecionado.setBairro(bairro);
                pacienteSelecionado.setCidade(cidade);
                pacienteSelecionado.setCep(apenasDigitosOuNull(cep));
                pacienteSelecionado.setUf(uf);
                pacienteSelecionado.setResponsavelLegal(responsavel.isBlank() ? null : responsavel);

                pacienteDAO.atualizar(pacienteSelecionado);
                if (lblMensagem != null) lblMensagem.setText("Paciente atualizado com sucesso.");
            }

            limparFormulario();
            carregarPacientes();
            atualizarBotoesAtivo(null);
            if (tableBusca != null) tableBusca.getSelectionModel().clearSelection();

        } catch (RuntimeException ex) {
            String msg = ex.getMessage();
            if (lblMensagem != null) lblMensagem.setText(msg != null && !msg.isBlank() ? msg : "Não foi possível salvar o paciente.");
        }
    }

    @FXML
    private void onInativar() {
        if (pacienteSelecionado == null) {
            if (lblMensagem != null) lblMensagem.setText("Selecione um paciente para inativar.");
            return;
        }
        if (!pacienteSelecionado.isAtivo()) {
            if (lblMensagem != null) lblMensagem.setText("Paciente já está inativo.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Inativar paciente");
        alert.setHeaderText("Confirma inativação?");
        alert.setContentText("Paciente: " + pacienteSelecionado.getNome());

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                pacienteDAO.inativar(pacienteSelecionado.getId());
                if (lblMensagem != null) lblMensagem.setText("Paciente inativado.");
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
            if (lblMensagem != null) lblMensagem.setText("Selecione um paciente para reativar.");
            return;
        }
        if (pacienteSelecionado.isAtivo()) {
            if (lblMensagem != null) lblMensagem.setText("Paciente já está ativo.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reativar paciente");
        alert.setHeaderText("Confirma reativação?");
        alert.setContentText("Paciente: " + pacienteSelecionado.getNome());

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                pacienteDAO.ativar(pacienteSelecionado.getId());
                if (lblMensagem != null) lblMensagem.setText("Paciente reativado.");
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
        if (lblMensagem != null) lblMensagem.setText("");
        if (tableBusca != null) tableBusca.getSelectionModel().clearSelection();
        pacienteSelecionado = null;
        atualizarBotoesAtivo(null);
    }

    @FXML
    private void onAtualizarLista() {
        carregarPacientes();
        if (tableBusca != null) tableBusca.getSelectionModel().clearSelection();
        pacienteSelecionado = null;
        atualizarBotoesAtivo(null);
    }

    private void limparFormulario() {
        if (txtNome != null) txtNome.clear();
        if (txtCpf != null) txtCpf.clear();
        if (dpDataNascimento != null) {
            dpDataNascimento.setValue(null);
            dpDataNascimento.getEditor().clear();
        }
        if (txtTelefone != null) txtTelefone.clear();
        if (txtRua != null) txtRua.clear();
        if (txtNumero != null) txtNumero.clear();

        if (chkSemNumero != null) chkSemNumero.setSelected(false);
        if (txtNumero != null) txtNumero.setDisable(false);

        if (txtBairro != null) txtBairro.clear();
        if (txtCidade != null) txtCidade.clear();
        if (txtCep != null) txtCep.clear();
        if (txtUf != null) txtUf.clear();
        if (txtResponsavelLegal != null) txtResponsavelLegal.clear();
    }

    // =========================================================
    //                     BUSCA CEP (ViaCEP)
    // =========================================================

    @FXML
    private void onBuscarCep() {
        if (lblMensagem != null) lblMensagem.setText("");

        String cep = safe(txtCep.getText()).replaceAll("\\D", "");
        if (cep.length() != 8) {
            if (lblMensagem != null) lblMensagem.setText("Informe um CEP com 8 dígitos.");
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
                if (lblMensagem != null) lblMensagem.setText("Erro ao consultar CEP (HTTP " + response.statusCode() + ").");
                return;
            }

            String body = response.body();
            if (body.contains("\"erro\": true")) {
                if (lblMensagem != null) lblMensagem.setText("CEP não encontrado.");
                return;
            }

            String logradouro = extrairCampoJson(body, "logradouro");
            String bairro = extrairCampoJson(body, "bairro");
            String cidade = extrairCampoJson(body, "localidade");
            String uf = extrairCampoJson(body, "uf");

            if (logradouro != null && !logradouro.isBlank() && txtRua != null) txtRua.setText(logradouro);
            if (bairro != null && !bairro.isBlank() && txtBairro != null) txtBairro.setText(bairro);
            if (cidade != null && !cidade.isBlank() && txtCidade != null) txtCidade.setText(cidade);
            if (uf != null && !uf.isBlank() && txtUf != null) txtUf.setText(uf);

            if (lblMensagem != null) lblMensagem.setText("Endereço preenchido a partir do CEP.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            if (lblMensagem != null) lblMensagem.setText("Erro ao consultar CEP.");
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

    // Helpers
    private static String apenasDigitos(String s) {
        if (s == null) return "";
        return s.replaceAll("\\D", "");
    }

    private static String apenasDigitosOuNull(String s) {
        String d = apenasDigitos(s);
        return (d == null || d.isBlank()) ? null : d;
    }
}
