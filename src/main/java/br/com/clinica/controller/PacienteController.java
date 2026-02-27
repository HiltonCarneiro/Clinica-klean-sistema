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

    // ✅ NOVO: botões da busca
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
    private Paciente pacienteSelecionado;


    // ✅ Se entrou no cadastro vindo da busca (Editar), o Voltar deve retornar para a busca
    private boolean voltarParaBusca = false;
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

        // ✅ começa desabilitado até selecionar paciente
        if (btnEditarSelecionado != null) btnEditarSelecionado.setDisable(true);
        if (btnHistoricoSelecionado != null) btnHistoricoSelecionado.setDisable(true);
    }

    // =========================================================
    // NAVEGAÇÃO
    // =========================================================

    @FXML
    private void voltarEscolha() {
        // ✅ Se veio da busca e entrou no cadastro para editar, ao voltar retorna para a busca
        if (scrollCadastro != null && scrollCadastro.isVisible() && voltarParaBusca) {
            voltarParaBusca = false;
            irParaBusca();
            return;
        }

        showTelaEscolha();
        setMensagem("");
        clearSelectionBusca();
    }

    @FXML
    private void irParaCadastro() {
        // ✅ Se não estiver editando (novo cadastro), não deve voltar para a busca
        if (pacienteSelecionado == null) {
            voltarParaBusca = false;
        }

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
        clearSelectionBusca();
    }

    @FXML
    private void onLimparBusca() {
        if (txtBusca != null) txtBusca.clear();
        if (chkMostrarInativosBusca != null) chkMostrarInativosBusca.setSelected(false);
        onBuscarPaciente();
    }

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

        // ✅ Seleção apenas seleciona (não navega)
        tableBusca.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            pacienteSelecionado = newSel;

            boolean tem = newSel != null;
            if (btnEditarSelecionado != null) btnEditarSelecionado.setDisable(!tem);
            if (btnHistoricoSelecionado != null) btnHistoricoSelecionado.setDisable(!tem);
        });
    }

    private void clearSelectionBusca() {
        if (tableBusca != null) tableBusca.getSelectionModel().clearSelection();
        pacienteSelecionado = null;

        if (btnEditarSelecionado != null) btnEditarSelecionado.setDisable(true);
        if (btnHistoricoSelecionado != null) btnHistoricoSelecionado.setDisable(true);
    }

    // =========================================================
    // ✅ AÇÕES NOVAS: EDITAR / HISTÓRICO
    // =========================================================

    @FXML
    private void onEditarSelecionado() {
        Paciente p = (tableBusca == null) ? null : tableBusca.getSelectionModel().getSelectedItem();
        if (p == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um paciente para editar.").showAndWait();
            return;
        }
        pacienteSelecionado = p;
        voltarParaBusca = true;

        irParaCadastro();
        fillForm(p);
        setBotoesAtivo(p);
    }

    @FXML
    private void onVerHistoricoSelecionado() {
        Paciente p = (tableBusca == null) ? null : tableBusca.getSelectionModel().getSelectedItem();
        if (p == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um paciente para ver o histórico.").showAndWait();
            return;
        }
        abrirHistoricoPaciente(p);
    }

    // =========================================================
    // CADASTRO / EDIÇÃO (mantido)
    // =========================================================

    @FXML
    private void onNovo() {
        pacienteSelecionado = null;
        voltarParaBusca = false;
        clearForm();
        setBotoesAtivo(null);
        setMensagem("");
    }

    @FXML
    private void onSalvar() {
        setMensagem("");

        String nome = safe(txtNome.getText()).trim();
        String cpf = digits(safe(txtCpf.getText()));
        LocalDate dataNascimento = dpDataNascimento != null ? dpDataNascimento.getValue() : null;
        String telefone = digits(safe(txtTelefone.getText()));

        String rua = safe(txtRua.getText()).trim();
        String numero = safe(txtNumero.getText()).trim();
        boolean semNumero = chkSemNumero != null && chkSemNumero.isSelected();

        String bairro = safe(txtBairro.getText()).trim();
        String cidade = safe(txtCidade.getText()).trim();
        String cep = digits(safe(txtCep.getText()));
        String uf = safe(txtUf.getText()).trim().toUpperCase();
        String responsavel = safe(txtResponsavelLegal.getText()).trim();

        if (nome.isBlank()) { setMensagem("Nome é obrigatório."); return; }
        if (!ValidationUtils.isValidCpf(cpf)) { setMensagem("CPF inválido."); return; }
        if (dataNascimento == null) { setMensagem("Data de nascimento é obrigatória."); return; }
        if (telefone.length() < 10) { setMensagem("Telefone inválido. Informe DDD + número."); return; }
        if (rua.isBlank() || cidade.isBlank()) { setMensagem("Endereço incompleto. Informe pelo menos Rua e Cidade."); return; }
        if (!semNumero && numero.isBlank()) { setMensagem("Informe o número do endereço ou marque 'Sem número'."); return; }
        if (semNumero) numero = "S/N";
        if (!cep.isBlank() && !ValidationUtils.isValidCep(cep)) { setMensagem("CEP inválido. Informe 8 dígitos."); return; }
        if (!uf.isBlank() && uf.length() != 2) { setMensagem("UF inválida. Use 2 letras (ex: CE, SP)."); return; }

        int idade = Period.between(dataNascimento, LocalDate.now()).getYears();
        if (idade < 18 && responsavel.isBlank()) { setMensagem("Responsável legal é obrigatório para menores de 18 anos."); return; }

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
            onBuscarPaciente();

        } catch (Exception e) {
            e.printStackTrace();
            setMensagem("Erro ao salvar paciente.");
        }
    }

    @FXML
    private void onInativar() {
        if (pacienteSelecionado == null || pacienteSelecionado.getId() == null) { setMensagem("Selecione um paciente para inativar."); return; }
        pacienteDAO.inativar(pacienteSelecionado.getId());
        pacienteSelecionado.setAtivo(false);
        setBotoesAtivo(pacienteSelecionado);
        setMensagem("Paciente inativado com sucesso.");
        onAtualizarLista();
        onBuscarPaciente();
    }

    @FXML
    private void onAtivar() {
        if (pacienteSelecionado == null || pacienteSelecionado.getId() == null) { setMensagem("Selecione um paciente para ativar."); return; }
        pacienteDAO.ativar(pacienteSelecionado.getId());
        pacienteSelecionado.setAtivo(true);
        setBotoesAtivo(pacienteSelecionado);
        setMensagem("Paciente ativado com sucesso.");
        onAtualizarLista();
        onBuscarPaciente();
    }

    @FXML
    private void onAtualizarLista() {
        boolean incluir = chkMostrarInativos != null && chkMostrarInativos.isSelected();
        pacientes.setAll(pacienteDAO.listarTodos(incluir));
    }

    private void applyFormToPaciente(Paciente p,
                                     String nome, String cpf, LocalDate dataNascimento, String telefone,
                                     String rua, String numero, String bairro, String cidade,
                                     String cep, String uf, String responsavelLegal) {
        p.setNome(nome);
        p.setCpf(cpf);
        p.setDataNascimento(dataNascimento);
        p.setTelefone(telefone);

        p.setRua(rua);
        p.setNumero(numero);
        p.setBairro(bairro);
        p.setCidade(cidade);
        p.setCep(cep);
        p.setUf(uf);

        p.setResponsavelLegal(responsavelLegal);

        String enderecoCompleto = rua + ", " + numero + " - " + bairro + " - " + cidade;
        if (!cep.isBlank()) enderecoCompleto += " - CEP: " + ValidationUtils.formatCep(cep);
        if (!uf.isBlank()) enderecoCompleto += "/" + uf;
        p.setEndereco(enderecoCompleto);
    }

    private void fillForm(Paciente p) {
        if (p == null) return;

        if (txtNome != null) txtNome.setText(safe(p.getNome()));
        if (txtCpf != null) txtCpf.setText(ValidationUtils.formatCpf(safe(p.getCpf())));
        if (dpDataNascimento != null) dpDataNascimento.setValue(p.getDataNascimento());
        if (txtTelefone != null) txtTelefone.setText(ValidationUtils.formatPhoneBr(safe(p.getTelefone())));

        if (txtRua != null) txtRua.setText(safe(p.getRua()));
        if (txtNumero != null) txtNumero.setText(safe(p.getNumero()));
        if (txtBairro != null) txtBairro.setText(safe(p.getBairro()));
        if (txtCidade != null) txtCidade.setText(safe(p.getCidade()));
        if (txtCep != null) txtCep.setText(ValidationUtils.formatCep(safe(p.getCep())));
        if (txtUf != null) txtUf.setText(safe(p.getUf()));
        if (txtResponsavelLegal != null) txtResponsavelLegal.setText(safe(p.getResponsavelLegal()));

        if (chkSemNumero != null) chkSemNumero.setSelected("S/N".equalsIgnoreCase(safe(p.getNumero())));

        setMensagem("");
    }

    private void clearForm() {
        if (txtNome != null) txtNome.clear();
        if (txtCpf != null) txtCpf.clear();
        if (dpDataNascimento != null) dpDataNascimento.setValue(null);
        if (txtTelefone != null) txtTelefone.clear();

        if (txtRua != null) txtRua.clear();
        if (txtNumero != null) txtNumero.clear();
        if (chkSemNumero != null) chkSemNumero.setSelected(false);

        if (txtBairro != null) txtBairro.clear();
        if (txtCidade != null) txtCidade.clear();
        if (txtCep != null) txtCep.clear();
        if (txtUf != null) txtUf.clear();
        if (txtResponsavelLegal != null) txtResponsavelLegal.clear();
    }

    private void setBotoesAtivo(Paciente p) {
        boolean temPaciente = p != null && p.getId() != null;
        boolean ativo = temPaciente && p.isAtivo();

        if (btnInativar != null) btnInativar.setDisable(!temPaciente || !ativo);
        if (btnAtivar != null) btnAtivar.setDisable(!temPaciente || ativo);
    }

    // =========================================================
    // MÁSCARAS / CEP / DATAPICKER
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
            txtTelefone.textProperty().addListener((obs, old, val) -> {
                if (val == null) return;
                String dig = digits(val);
                if (dig.length() > 11) dig = dig.substring(0, 11);
                String fmt = ValidationUtils.formatPhoneBr(dig);
                if (!fmt.equals(val)) {
                    txtTelefone.setText(fmt);
                    txtTelefone.positionCaret(fmt.length());
                }
            });
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

        TextField editor = dpDataNascimento.getEditor();
        if (editor == null) return;

        editor.textProperty().addListener((obs, oldText, newText) -> {
            if (updatingDateEditor) return;
            updatingDateEditor = true;
            try {
                String cleaned = newText.replaceAll("[^0-9/]", "");
                editor.setText(cleaned);
                editor.positionCaret(cleaned.length());
            } finally {
                updatingDateEditor = false;
            }
        });

        editor.focusedProperty().addListener((obs, was, is) -> {
            if (!is) {
                String t = editor.getText().trim();
                if (t.isBlank()) {
                    dpDataNascimento.setValue(null);
                    return;
                }
                try {
                    LocalDate dt = LocalDate.parse(t, fmtBr);
                    dpDataNascimento.setValue(dt);
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
                    .thenAccept(json -> Platform.runLater(() -> preencherEnderecoViaCep(json)))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> setMensagem("Não foi possível buscar o CEP."));
                        return null;
                    });

        } catch (Exception e) {
            setMensagem("Não foi possível buscar o CEP.");
        }
    }

    private void preencherEnderecoViaCep(String json) {
        Function<String, String> get = (key) -> {
            String pattern = "\"" + key + "\":";
            int idx = json.indexOf(pattern);
            if (idx < 0) return "";
            int start = json.indexOf("\"", idx + pattern.length()) + 1;
            int end = json.indexOf("\"", start);
            if (start <= 0 || end < 0) return "";
            return json.substring(start, end);
        };

        String rua = get.apply("logradouro");
        String bairro = get.apply("bairro");
        String cidade = get.apply("localidade");
        String uf = get.apply("uf");

        if (txtRua != null && !rua.isBlank()) txtRua.setText(rua);
        if (txtBairro != null && !bairro.isBlank()) txtBairro.setText(bairro);
        if (txtCidade != null && !cidade.isBlank()) txtCidade.setText(cidade);
        if (txtUf != null && !uf.isBlank()) txtUf.setText(uf);

        setMensagem("");
    }

    private void setMensagem(String msg) {
        if (lblMensagem != null) lblMensagem.setText(msg == null ? "" : msg);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String digits(String s) {
        return safe(s).replaceAll("\\D", "");
    }

    // =========================================================
    // HISTÓRICO
    // =========================================================
    private void abrirHistoricoPaciente(Paciente paciente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/paciente-historico-view.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass().getMethod("setPaciente", Paciente.class).invoke(controller, paciente);
                } catch (Exception ignored) { }
            }

            Stage stage = new Stage();
            stage.setTitle("Histórico do Paciente");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao abrir histórico do paciente.").showAndWait();
        }
    }
}