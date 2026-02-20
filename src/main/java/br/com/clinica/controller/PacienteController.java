package br.com.clinica.controller;

import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Paciente;
import br.com.clinica.util.ValidationUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
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
import java.util.List;
import java.util.function.Function;

public class PacienteController {

    // ====== NAVEGAÇÃO ======
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

    private final DateTimeFormatter fmtBr = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private boolean updatingDateEditor = false;

    @FXML
    private void initialize() {
        showTelaEscolha();

        setupTabelaBusca();
        setupMasksAndRules();
        setupDatePickerMaskFix(); // ✅ corrige “backspace apaga tudo”

        setMensagem("");
        setBotoesAtivo(null);
    }

    // =========================================================
    // NAVEGAÇÃO (compatível com FXML)
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
    // BUSCA (compatível com FXML)
    // =========================================================

    private void setupTabelaBusca() {
        if (tableBusca == null) return;

        if (colBuscaNome != null) colBuscaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colBuscaCpf != null) colBuscaCpf.setCellValueFactory(c ->
                new SimpleStringProperty(ValidationUtils.formatCpf(safe(c.getValue().getCpf())))
        );
        if (colBuscaTelefone != null) colBuscaTelefone.setCellValueFactory(c ->
                new SimpleStringProperty(ValidationUtils.formatPhoneBr(safe(c.getValue().getTelefone())))
        );
        if (colBuscaDataNascimento != null) colBuscaDataNascimento.setCellValueFactory(c -> {
            LocalDate dt = c.getValue().getDataNascimento();
            return new SimpleStringProperty(dt == null ? "" : dt.format(fmtBr));
        });
        if (colBuscaAtivo != null) colBuscaAtivo.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().isAtivo() ? "Sim" : "Não")
        );

        tableBusca.setItems(pacientes);

        tableBusca.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            pacienteSelecionado = newSel;
            if (newSel != null) {
                irParaCadastro();
                fillForm(newSel);
                setBotoesAtivo(newSel);
            } else {
                setBotoesAtivo(null);
            }
        });
    }

    @FXML
    private void onBuscarPaciente() {
        String query = txtBusca != null ? txtBusca.getText().trim() : "";
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
    }

    @FXML
    private void onLimparBusca() {
        if (txtBusca != null) txtBusca.clear();
        if (chkMostrarInativosBusca != null) chkMostrarInativosBusca.setSelected(false);
        onBuscarPaciente();
    }

    @FXML
    private void onAtualizarLista() {
        // Atualiza a lista da busca respeitando checkboxes
        if (tableBusca == null) return;

        boolean incluir = false;
        if (chkMostrarInativosBusca != null && chkMostrarInativosBusca.isSelected()) incluir = true;
        if (chkMostrarInativos != null && chkMostrarInativos.isSelected()) incluir = true;

        List<Paciente> lista = pacienteDAO.listarTodos(incluir);
        pacientes.setAll(lista);
        tableBusca.refresh();
    }

    private void clearSelectionBusca() {
        if (tableBusca != null) tableBusca.getSelectionModel().clearSelection();
    }

    // =========================================================
    // CADASTRO (compatível com FXML)
    // =========================================================

    @FXML
    private void onNovo() {
        pacienteSelecionado = null;
        clearForm();
        clearSelectionBusca();
        setBotoesAtivo(null);
        setMensagem("Novo paciente. Preencha os dados e clique em Salvar.");
    }

    @FXML
    private void onLimpar() {
        setMensagem("");
        clearForm();
        clearSelectionBusca();
        setBotoesAtivo(pacienteSelecionado); // mantém estado se estiver editando alguém
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

    @FXML
    private void onSalvar() {
        setMensagem("");

        String nome = get(txtNome);
        String cpf = get(txtCpf);
        String telefone = get(txtTelefone);

        LocalDate dataNascimento = dpDataNascimento != null ? dpDataNascimento.getValue() : null;

        String rua = get(txtRua);
        String numero = get(txtNumero);
        boolean semNumero = chkSemNumero != null && chkSemNumero.isSelected();

        String bairro = get(txtBairro);
        String cidade = get(txtCidade);
        String cep = get(txtCep);
        String uf = get(txtUf);
        String responsavel = get(txtResponsavelLegal);

        // Validações (mantidas simples e “padrão mercado”)
        if (!ValidationUtils.isValidName(nome)) {
            setMensagem("Nome inválido. Use apenas letras e espaços.");
            return;
        }

        if (!cpf.isBlank() && !ValidationUtils.isValidCpf(cpf)) {
            setMensagem("CPF inválido.");
            return;
        }

        if (dataNascimento == null) {
            setMensagem("Data de nascimento é obrigatória.");
            return;
        }

        if (!telefone.isBlank() && !ValidationUtils.isValidPhoneBr(telefone)) {
            setMensagem("Telefone inválido. Informe DDD + número.");
            return;
        }

        if (rua.isBlank() || cidade.isBlank()) {
            setMensagem("Endereço incompleto. Informe pelo menos Rua e Cidade.");
            return;
        }

        if (!semNumero && numero.isBlank()) {
            setMensagem("Informe o número do endereço ou marque 'Sem número'.");
            return;
        }

        if (semNumero) numero = "S/N";

        if (!cep.isBlank() && !ValidationUtils.isValidCep(cep)) {
            setMensagem("CEP inválido. Informe 8 dígitos.");
            return;
        }

        if (!uf.isBlank() && uf.length() != 2) {
            setMensagem("UF inválida. Use 2 letras (ex: CE, SP).");
            return;
        }

        int idade = Period.between(dataNascimento, LocalDate.now()).getYears();
        if (idade < 18 && responsavel.isBlank()) {
            setMensagem("Responsável legal é obrigatório para menores de 18 anos.");
            return;
        }

        try {
            if (pacienteSelecionado == null) {
                Paciente p = new Paciente();
                applyFormToPaciente(p, nome, cpf, dataNascimento, telefone, rua, numero, bairro, cidade, cep, uf, responsavel);
                p.setAtivo(true);

                pacienteDAO.salvar(p);
                pacienteSelecionado = p;

                setMensagem("Paciente salvo com sucesso.");
            } else {
                applyFormToPaciente(pacienteSelecionado, nome, cpf, dataNascimento, telefone, rua, numero, bairro, cidade, cep, uf, responsavel);
                pacienteDAO.atualizar(pacienteSelecionado);

                setMensagem("Paciente atualizado com sucesso.");
            }

            setBotoesAtivo(pacienteSelecionado);
            onAtualizarLista();

        } catch (Exception e) {
            e.printStackTrace();
            setMensagem("Erro ao salvar paciente: " + e.getMessage());
        }
    }

    @FXML
    private void onInativar() {
        if (pacienteSelecionado == null || pacienteSelecionado.getId() == null) {
            setMensagem("Selecione um paciente para inativar.");
            return;
        }

        if (!confirmar("Inativar paciente", "Deseja inativar este paciente?", pacienteSelecionado.getNome())) return;

        try {
            pacienteDAO.inativar(pacienteSelecionado.getId());
            pacienteSelecionado.setAtivo(false);
            setMensagem("Paciente inativado.");
            setBotoesAtivo(pacienteSelecionado);
            onAtualizarLista();
        } catch (Exception e) {
            e.printStackTrace();
            setMensagem("Erro ao inativar: " + e.getMessage());
        }
    }

    @FXML
    private void onAtivar() {
        if (pacienteSelecionado == null || pacienteSelecionado.getId() == null) {
            setMensagem("Selecione um paciente para reativar.");
            return;
        }

        if (!confirmar("Reativar paciente", "Deseja reativar este paciente?", pacienteSelecionado.getNome())) return;

        try {
            pacienteDAO.ativar(pacienteSelecionado.getId());
            pacienteSelecionado.setAtivo(true);
            setMensagem("Paciente reativado.");
            setBotoesAtivo(pacienteSelecionado);
            onAtualizarLista();
        } catch (Exception e) {
            e.printStackTrace();
            setMensagem("Erro ao reativar: " + e.getMessage());
        }
    }

    // =========================================================
    // CEP (compatível com FXML: #onBuscarCep)
    // =========================================================

    @FXML
    private void onBuscarCep() {
        setMensagem("");

        String cep = get(txtCep);
        String cepDigits = digits(cep);

        if (cepDigits.length() != 8) {
            setMensagem("Informe um CEP válido (8 dígitos).");
            return;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://viacep.com.br/ws/" + cepDigits + "/json/"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                setMensagem("Erro ao consultar CEP.");
                return;
            }

            String body = response.body();
            if (body.contains("\"erro\": true")) {
                setMensagem("CEP não encontrado.");
                return;
            }

            set(txtRua, jsonField(body, "logradouro"));
            set(txtBairro, jsonField(body, "bairro"));
            set(txtCidade, jsonField(body, "localidade"));
            set(txtUf, jsonField(body, "uf"));

            setMensagem("Endereço preenchido a partir do CEP.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            setMensagem("Erro ao consultar CEP.");
        }
    }

    // =========================================================
    // FIX DATA NASCIMENTO (DatePicker seleciona tudo e Backspace apaga tudo)
    // =========================================================

    private void setupDatePickerMaskFix() {
        if (dpDataNascimento == null) return;

        dpDataNascimento.setEditable(true);
        final TextField editor = dpDataNascimento.getEditor();

        Runnable breakSelectAll = () -> {
            String t = editor.getText();
            if (t == null) t = "";
            if (t.length() > 0 && editor.getSelection().getLength() == t.length()) {
                editor.deselect();
                editor.positionCaret(t.length());
            }
        };

        // O DatePicker pode selecionar tudo DEPOIS do foco/click, então rodamos 2x
        editor.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                Platform.runLater(() -> {
                    breakSelectAll.run();
                    Platform.runLater(breakSelectAll);
                });
            }
        });

        editor.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            Platform.runLater(() -> {
                breakSelectAll.run();
                Platform.runLater(breakSelectAll);
            });
        });

        // Máscara dd/MM/yyyy com caret consistente
        editor.setTextFormatter(new TextFormatter<String>(change -> {
            if (updatingDateEditor) return change;

            String oldText = change.getControlText();
            String newText = change.getControlNewText();
            if (newText == null) newText = "";

            int digitsBeforeCaret = countDigits(newText, change.getCaretPosition());

            String raw = digits(newText);
            if (raw.length() > 8) raw = raw.substring(0, 8);

            String formatted = formatDateBr(raw);

            digitsBeforeCaret = Math.min(digitsBeforeCaret, raw.length());
            int caret = caretPosByDigitCount(formatted, digitsBeforeCaret);

            updatingDateEditor = true;
            change.setText(formatted);
            change.setRange(0, oldText.length());
            change.setCaretPosition(caret);
            change.setAnchor(caret);
            updatingDateEditor = false;

            return change;
        }));

        // Define value apenas quando estiver completo
        editor.textProperty().addListener((obs, old, text) -> {
            if (updatingDateEditor) return;
            if (text != null && text.length() == 10) {
                try {
                    dpDataNascimento.setValue(LocalDate.parse(text, fmtBr));
                } catch (DateTimeParseException ex) {
                    dpDataNascimento.setValue(null);
                }
            } else {
                dpDataNascimento.setValue(null);
            }
        });
    }

    // =========================================================
    // MÁSCARAS / REGRAS (telefone já resolvido)
    // =========================================================

    private void setupMasksAndRules() {
        // Nome: só letras e espaços (bem permissivo)
        if (txtNome != null) {
            txtNome.setTextFormatter(new TextFormatter<String>(ch -> {
                String t = ch.getControlNewText();
                if (t.isEmpty()) return ch;
                return t.matches("^[A-Za-zÀ-ÿ'\\- ]*$") ? ch : null;
            }));
        }

        // UF: 2 letras uppercase
        if (txtUf != null) {
            txtUf.setTextFormatter(new TextFormatter<String>(ch -> {
                String t = ch.getControlNewText();
                if (t.length() > 2) return null;
                if (!t.matches("^[A-Za-z]*$")) return null;
                ch.setText(ch.getText().toUpperCase());
                return ch;
            }));
        }

        applyMaskWithCaret(txtCpf, ValidationUtils::formatCpf);
        applyMaskWithCaret(txtCep, ValidationUtils::formatCep);
        applyMaskWithCaret(txtTelefone, ValidationUtils::formatPhoneBr);

        // Número: só dígitos quando não for S/N
        if (txtNumero != null) {
            txtNumero.setTextFormatter(new TextFormatter<String>(ch -> {
                if (chkSemNumero != null && chkSemNumero.isSelected()) return null;
                String t = ch.getControlNewText();
                if (t.isEmpty()) return ch;
                return t.matches("^\\d*$") ? ch : null;
            }));
        }
    }

    private void applyMaskWithCaret(TextField field, Function<String, String> formatterFn) {
        if (field == null) return;

        field.setTextFormatter(new TextFormatter<String>(change -> {
            String oldText = change.getControlText();
            String newText = change.getControlNewText();

            int digitsBeforeCaret = countDigits(newText, change.getCaretPosition());

            String formatted = formatterFn.apply(newText);
            if (formatted == null) formatted = "";

            int caret = caretPosByDigitCount(formatted, digitsBeforeCaret);

            change.setText(formatted);
            change.setRange(0, oldText.length());
            change.setCaretPosition(caret);
            change.setAnchor(caret);
            return change;
        }));
    }

    // =========================================================
    // HELPERS / FORM
    // =========================================================

    private void fillForm(Paciente p) {
        if (p == null) return;

        set(txtNome, p.getNome());
        set(txtCpf, p.getCpf());
        set(txtTelefone, p.getTelefone());

        if (dpDataNascimento != null) {
            dpDataNascimento.setValue(p.getDataNascimento());
            dpDataNascimento.getEditor().setText(p.getDataNascimento() == null ? "" : p.getDataNascimento().format(fmtBr));
        }

        set(txtRua, p.getRua());

        String numero = safe(p.getNumero()).trim();
        if (chkSemNumero != null && txtNumero != null) {
            boolean sn = "S/N".equalsIgnoreCase(numero);
            chkSemNumero.setSelected(sn);
            txtNumero.setDisable(sn);
            txtNumero.setText(sn ? "S/N" : numero);
        } else {
            set(txtNumero, numero);
        }

        set(txtBairro, p.getBairro());
        set(txtCidade, p.getCidade());
        set(txtCep, p.getCep());
        set(txtUf, p.getUf());
        set(txtResponsavelLegal, safe(p.getResponsavelLegal()));

        setMensagem("");
    }

    private void clearForm() {
        set(txtNome, "");
        set(txtCpf, "");
        set(txtTelefone, "");

        if (dpDataNascimento != null) {
            dpDataNascimento.setValue(null);
            dpDataNascimento.getEditor().clear();
        }

        set(txtRua, "");
        set(txtNumero, "");

        if (chkSemNumero != null) {
            chkSemNumero.setSelected(false);
            if (txtNumero != null) txtNumero.setDisable(false);
        }

        set(txtBairro, "");
        set(txtCidade, "");
        set(txtCep, "");
        set(txtUf, "");
        set(txtResponsavelLegal, "");
    }

    private void applyFormToPaciente(
            Paciente p,
            String nome, String cpf, LocalDate dataNascimento, String telefone,
            String rua, String numero, String bairro, String cidade, String cep, String uf, String responsavel
    ) {
        p.setNome(nome);
        p.setCpf(digitsOrNull(cpf));
        p.setDataNascimento(dataNascimento);
        p.setTelefone(digitsOrNull(telefone));

        p.setRua(rua);
        p.setNumero(numero);
        p.setBairro(bairro);
        p.setCidade(cidade);
        p.setCep(digitsOrNull(cep));
        p.setUf(uf);
        p.setResponsavelLegal(responsavel.isBlank() ? null : responsavel);
    }

    private void setBotoesAtivo(Paciente p) {
        if (btnInativar == null || btnAtivar == null) return;

        if (p == null || p.getId() == null) {
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

    private boolean confirmar(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        var r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }

    private void setMensagem(String msg) {
        if (lblMensagem != null) lblMensagem.setText(msg == null ? "" : msg);
    }

    private String get(TextField tf) {
        return tf == null || tf.getText() == null ? "" : tf.getText().trim();
    }

    private void set(TextField tf, String value) {
        if (tf == null) return;
        tf.setText(value == null ? "" : value);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private static String digits(String s) {
        if (s == null) return "";
        return s.replaceAll("\\D", "");
    }

    private static String digitsOrNull(String s) {
        String d = digits(s);
        return d.isBlank() ? null : d;
    }

    private static String jsonField(String json, String campo) {
        String key = "\"" + campo + "\":";
        int idx = json.indexOf(key);
        if (idx == -1) return "";
        int q1 = json.indexOf('"', idx + key.length());
        if (q1 == -1) return "";
        int q2 = json.indexOf('"', q1 + 1);
        if (q2 == -1) return "";
        return json.substring(q1 + 1, q2);
    }

    // ==== Date caret helpers ====
    private static String formatDateBr(String rawDigits) {
        if (rawDigits == null) rawDigits = "";
        if (rawDigits.length() > 8) rawDigits = rawDigits.substring(0, 8);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rawDigits.length(); i++) {
            sb.append(rawDigits.charAt(i));
            if (i == 1 && rawDigits.length() > 2) sb.append('/');
            if (i == 3 && rawDigits.length() > 4) sb.append('/');
        }
        return sb.toString();
    }

    private static int countDigits(String text, int upToExclusive) {
        if (text == null) return 0;
        int end = Math.max(0, Math.min(upToExclusive, text.length()));
        int c = 0;
        for (int i = 0; i < end; i++) if (Character.isDigit(text.charAt(i))) c++;
        return c;
    }

    private static int caretPosByDigitCount(String formatted, int digitCount) {
        if (formatted == null || formatted.isEmpty() || digitCount <= 0) return 0;

        int d = 0;
        for (int i = 0; i < formatted.length(); i++) {
            if (Character.isDigit(formatted.charAt(i))) {
                d++;
                if (d >= digitCount) return i + 1;
            }
        }
        return formatted.length();
    }
}