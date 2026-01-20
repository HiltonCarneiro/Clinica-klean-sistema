package br.com.clinica.controller;

import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Paciente;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PacienteController {

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtCpf;

    @FXML
    private DatePicker dpDataNascimento;

    @FXML
    private TextField txtTelefone;

    @FXML
    private TextField txtRua;

    @FXML
    private TextField txtNumero;

    @FXML
    private TextField txtBairro;

    @FXML
    private TextField txtCidade;

    @FXML
    private TextField txtCep;

    @FXML
    private TextField txtUf;

    @FXML
    private TextField txtResponsavelLegal;

    @FXML
    private Label lblMensagem;

    @FXML
    private TableView<Paciente> tablePacientes;

    @FXML
    private TableColumn<Paciente, String> colNome;

    @FXML
    private TableColumn<Paciente, String> colCpf;

    @FXML
    private TableColumn<Paciente, String> colTelefone;

    @FXML
    private TableColumn<Paciente, String> colDataNascimento;

    @FXML
    private TableColumn<Paciente, String> colAtivo;

    @FXML
    private Button btnInativar;

    @FXML
    private Button btnAtivar;

    @FXML
    private CheckBox chkMostrarInativos;

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final ObservableList<Paciente> pacientes = FXCollections.observableArrayList();

    private Paciente pacienteSelecionado;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private boolean atualizandoEditorData = false;

    @FXML
    private void initialize() {
        configurarTabela();
        configurarMascaraData();
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

    private void configurarTabela() {
        colNome.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNome()));
        colCpf.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCpf()));
        colTelefone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefone()));

        colDataNascimento.setCellValueFactory(data -> {
            LocalDate dt = data.getValue().getDataNascimento();
            String texto = (dt != null) ? dt.format(formatter) : "";
            return new SimpleStringProperty(texto);
        });

        colAtivo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isAtivo() ? "Sim" : "Não"));

        tablePacientes.setItems(pacientes);
    }

    /** Máscara de data: digita 01022024 -> 01/02/2024 */
    private void configurarMascaraData() {
        TextField editor = dpDataNascimento.getEditor();
        editor.setPromptText("dd/mm/aaaa");

        dpDataNascimento.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(string, formatter);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });

        editor.textProperty().addListener((obs, oldText, newText) -> {
            if (atualizandoEditorData) return;

            String digits = newText.replaceAll("[^0-9]", "");
            if (digits.length() > 8) {
                digits = digits.substring(0, 8);
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                sb.append(digits.charAt(i));
                if ((i == 1 || i == 3) && i < digits.length() - 1) {
                    sb.append('/');
                }
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
                } catch (DateTimeParseException e) {
                    // data inválida, ignora
                }
            } else {
                dpDataNascimento.setValue(null);
            }
        });
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
        txtTelefone.setText(p.getTelefone());
        txtRua.setText(p.getRua());
        txtNumero.setText(p.getNumero());
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

        String nome = txtNome.getText();
        String cpf = txtCpf.getText();
        LocalDate dataNascimento = dpDataNascimento.getValue();
        String telefone = txtTelefone.getText();
        String rua = txtRua.getText();
        String numero = txtNumero.getText();
        String bairro = txtBairro.getText();
        String cidade = txtCidade.getText();
        String cep = txtCep.getText();
        String uf = txtUf.getText();
        String responsavel = txtResponsavelLegal.getText();

        if (nome == null || nome.isBlank()) {
            lblMensagem.setText("Nome é obrigatório.");
            return;
        }

        if (pacienteSelecionado == null) {
            Paciente p = new Paciente();
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
            p.setResponsavelLegal(responsavel);
            p.setAtivo(true);

            pacienteDAO.salvar(p);
            lblMensagem.setText("Paciente salvo com sucesso.");
        } else {
            pacienteSelecionado.setNome(nome);
            pacienteSelecionado.setCpf(cpf);
            pacienteSelecionado.setDataNascimento(dataNascimento);
            pacienteSelecionado.setTelefone(telefone);
            pacienteSelecionado.setRua(rua);
            pacienteSelecionado.setNumero(numero);
            pacienteSelecionado.setBairro(bairro);
            pacienteSelecionado.setCidade(cidade);
            pacienteSelecionado.setCep(cep);
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

        String cep = txtCep.getText();
        if (cep == null) cep = "";
        cep = cep.replaceAll("\\D", ""); // só números

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
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

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

            if (logradouro != null && !logradouro.isBlank()) {
                txtRua.setText(logradouro);
            }
            if (bairro != null && !bairro.isBlank()) {
                txtBairro.setText(bairro);
            }
            if (cidade != null && !cidade.isBlank()) {
                txtCidade.setText(cidade);
            }
            if (uf != null && !uf.isBlank()) {
                txtUf.setText(uf);
            }

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
