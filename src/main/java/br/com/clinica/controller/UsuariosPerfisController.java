package br.com.clinica.controller;

import br.com.clinica.auth.Permissao;
import br.com.clinica.dao.AdminUsuariosPerfisDAO;
import br.com.clinica.dao.PerfilPermissaoDAO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.stream.Collectors;

public class UsuariosPerfisController {

    private final AdminUsuariosPerfisDAO adminDAO = new AdminUsuariosPerfisDAO();
    private final PerfilPermissaoDAO permDAO = new PerfilPermissaoDAO();

    // Usuários
    @FXML private TextField txtBuscaUsuario;
    @FXML private CheckBox chkIncluirInativos;

    @FXML private TableView<AdminUsuariosPerfisDAO.UsuarioRow> tblUsuarios;
    @FXML private TableColumn<AdminUsuariosPerfisDAO.UsuarioRow, Number> colUId;
    @FXML private TableColumn<AdminUsuariosPerfisDAO.UsuarioRow, String> colULogin;
    @FXML private TableColumn<AdminUsuariosPerfisDAO.UsuarioRow, String> colUPessoa;
    @FXML private TableColumn<AdminUsuariosPerfisDAO.UsuarioRow, String> colUCargo;
    @FXML private TableColumn<AdminUsuariosPerfisDAO.UsuarioRow, String> colUPerfil;
    @FXML private TableColumn<AdminUsuariosPerfisDAO.UsuarioRow, Boolean> colUAtivo;

    @FXML private Label lblMsgUsuarios;

    @FXML private TextField txtCargo;
    @FXML private TextField txtPessoaNome;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtSenha;
    @FXML private ComboBox<AdminUsuariosPerfisDAO.PerfilRow> cbPerfilUsuario;
    @FXML private CheckBox chkAtivoUsuario;
    @FXML private Label lblMsgUsuarioForm;

    private AdminUsuariosPerfisDAO.UsuarioRow usuarioEditando;

    // Perfis
    @FXML private TextField txtNovoPerfil;
    @FXML private TableView<AdminUsuariosPerfisDAO.PerfilRow> tblPerfis;
    @FXML private TableColumn<AdminUsuariosPerfisDAO.PerfilRow, Number> colPId;
    @FXML private TableColumn<AdminUsuariosPerfisDAO.PerfilRow, String> colPNome;
    @FXML private TextField txtRenomearPerfil;
    @FXML private Label lblMsgPerfis;

    // Permissões
    @FXML private ComboBox<AdminUsuariosPerfisDAO.PerfilRow> cbPerfilPermissoes;
    @FXML private VBox boxPermissoes;
    @FXML private Label lblMsgPermissoes;

    private final Map<String, CheckBox> checks = new LinkedHashMap<>();

    @FXML
    private void initialize() {
        // Tabela Usuários
        colUId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().id));
        colULogin.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().login)));
        colUPessoa.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().pessoaNome)));
        colUCargo.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().nome)));
        colUPerfil.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().perfilNome)));
        colUAtivo.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().ativo));

        colUAtivo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : (v ? "Sim" : "Não"));
            }
        });

        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) carregarUsuario(n);
        });

        txtBuscaUsuario.textProperty().addListener((obs, o, n) -> atualizarUsuarios());

        // Tabela Perfis
        colPId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().id));
        colPNome.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().nome)));

        // Carregar listas
        atualizarPerfis();
        atualizarUsuarios();

        // Checklist Permissões
        montarChecklist();
    }

    // ===================== Usuários =====================

    @FXML private void onAtualizarUsuarios() { atualizarUsuarios(); }

    @FXML private void onNovoUsuario() {
        usuarioEditando = null;
        txtCargo.clear();
        txtPessoaNome.clear();
        txtLogin.clear();
        txtSenha.clear();
        chkAtivoUsuario.setSelected(true);
        cbPerfilUsuario.getSelectionModel().clearSelection();
        lblMsgUsuarioForm.setText("");
    }

    @FXML private void onCancelarUsuario() {
        tblUsuarios.getSelectionModel().clearSelection();
        onNovoUsuario();
    }

    @FXML private void onSalvarUsuario() {
        lblMsgUsuarioForm.setText("");

        String cargo = safeTrim(txtCargo.getText()).toUpperCase();
        String pessoa = safeTrim(txtPessoaNome.getText());
        String login = safeTrim(txtLogin.getText());
        String senha = txtSenha.getText() != null ? txtSenha.getText().trim() : "";
        boolean ativo = chkAtivoUsuario.isSelected();
        var perfil = cbPerfilUsuario.getValue();

        if (cargo.isBlank()) { lblMsgUsuarioForm.setText("Informe o cargo."); return; }
        if (pessoa.isBlank()) { lblMsgUsuarioForm.setText("Informe o nome da pessoa."); return; }
        if (login.isBlank()) { lblMsgUsuarioForm.setText("Informe o login."); return; }
        if (perfil == null) { lblMsgUsuarioForm.setText("Selecione o perfil."); return; }

        boolean criando = (usuarioEditando == null);
        if (criando && senha.isBlank()) { lblMsgUsuarioForm.setText("Informe a senha."); return; }

        try {
            AdminUsuariosPerfisDAO.UsuarioRow u = criando ? new AdminUsuariosPerfisDAO.UsuarioRow() : usuarioEditando;
            u.nome = cargo;
            u.pessoaNome = pessoa;
            u.login = login;
            u.ativo = ativo;
            u.perfilId = perfil.id;

            if (!senha.isBlank()) u.senha = senha; // se editando e deixar em branco, mantém

            if (criando) adminDAO.inserirUsuario(u);
            else adminDAO.atualizarUsuario(u);

            atualizarUsuarios();
            lblMsgUsuarioForm.setText("Salvo com sucesso.");
        } catch (Exception e) {
            lblMsgUsuarioForm.setText("Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML private void onAtivarInativarUsuario() {
        var sel = tblUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsgUsuarios.setText("Selecione um usuário."); return; }

        try {
            adminDAO.ativarInativarUsuario(sel.id, !sel.ativo);
            atualizarUsuarios();
            lblMsgUsuarios.setText("Atualizado.");
        } catch (Exception e) {
            lblMsgUsuarios.setText("Erro: " + e.getMessage());
        }
    }

    @FXML private void onResetarSenha() {
        var sel = tblUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsgUsuarios.setText("Selecione um usuário."); return; }

        TextInputDialog d = new TextInputDialog();
        d.setTitle("Resetar senha");
        d.setHeaderText("Nova senha para: " + sel.login);
        d.setContentText("Senha:");
        Optional<String> r = d.showAndWait();
        if (r.isEmpty()) return;

        String nova = r.get().trim();
        if (nova.isBlank()) { lblMsgUsuarios.setText("Senha vazia não pode."); return; }

        try {
            adminDAO.resetSenha(sel.id, nova);
            lblMsgUsuarios.setText("Senha atualizada.");
        } catch (Exception e) {
            lblMsgUsuarios.setText("Erro: " + e.getMessage());
        }
    }

    private void atualizarUsuarios() {
        lblMsgUsuarios.setText("");
        var lista = adminDAO.listarUsuarios(txtBuscaUsuario.getText(), chkIncluirInativos.isSelected());
        tblUsuarios.setItems(FXCollections.observableArrayList(lista));
    }

    private void carregarUsuario(AdminUsuariosPerfisDAO.UsuarioRow u) {
        usuarioEditando = u;
        txtCargo.setText(safe(u.nome));
        txtPessoaNome.setText(safe(u.pessoaNome));
        txtLogin.setText(safe(u.login));
        txtSenha.clear();
        chkAtivoUsuario.setSelected(u.ativo);

        var found = cbPerfilUsuario.getItems().stream().filter(p -> p.id == u.perfilId).findFirst().orElse(null);
        cbPerfilUsuario.setValue(found);

        lblMsgUsuarioForm.setText("");
    }

    // ===================== Perfis =====================

    @FXML private void onAtualizarPerfis() { atualizarPerfis(); }

    @FXML private void onCriarPerfil() {
        lblMsgPerfis.setText("");
        String nome = safeTrim(txtNovoPerfil.getText()).toUpperCase();
        if (nome.isBlank()) { lblMsgPerfis.setText("Informe o nome."); return; }

        try {
            adminDAO.criarPerfil(nome);
            txtNovoPerfil.clear();
            atualizarPerfis();
            lblMsgPerfis.setText("Perfil criado.");
        } catch (Exception e) {
            lblMsgPerfis.setText("Erro: " + e.getMessage());
        }
    }

    @FXML private void onRenomearPerfil() {
        lblMsgPerfis.setText("");
        var sel = tblPerfis.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsgPerfis.setText("Selecione um perfil."); return; }

        String novo = safeTrim(txtRenomearPerfil.getText()).toUpperCase();
        if (novo.isBlank()) { lblMsgPerfis.setText("Informe o novo nome."); return; }

        try {
            adminDAO.renomearPerfil(sel.id, novo);
            atualizarPerfis();
            lblMsgPerfis.setText("Renomeado.");
        } catch (Exception e) {
            lblMsgPerfis.setText("Erro: " + e.getMessage());
        }
    }

    private void atualizarPerfis() {
        var perfis = adminDAO.listarPerfis();
        var obs = FXCollections.observableArrayList(perfis);
        tblPerfis.setItems(obs);
        cbPerfilUsuario.setItems(obs);
        cbPerfilPermissoes.setItems(obs);
    }

    // ===================== Permissões =====================

    private void montarChecklist() {
        boxPermissoes.getChildren().clear();
        checks.clear();

        Permissao[] vals = Permissao.values();
        Arrays.sort(vals, Comparator.comparing(Enum::name));

        for (Permissao p : vals) {
            CheckBox cb = new CheckBox(p.name());
            checks.put(p.name(), cb);
            boxPermissoes.getChildren().add(cb);
        }
    }

    @FXML private void onCarregarPermissoes() {
        lblMsgPermissoes.setText("");
        var perfil = cbPerfilPermissoes.getValue();
        if (perfil == null) { lblMsgPermissoes.setText("Selecione um perfil."); return; }

        Set<String> marcadas = permDAO.listarPorPerfilId(perfil.id);
        for (var e : checks.entrySet()) e.getValue().setSelected(marcadas.contains(e.getKey()));

        lblMsgPermissoes.setText("Carregado.");
    }

    @FXML private void onSalvarPermissoes() {
        lblMsgPermissoes.setText("");
        var perfil = cbPerfilPermissoes.getValue();
        if (perfil == null) { lblMsgPermissoes.setText("Selecione um perfil."); return; }

        Set<String> selecionadas = checks.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        try {
            permDAO.salvarPermissoes(perfil.id, selecionadas);
            lblMsgPermissoes.setText("Salvo.");
        } catch (Exception e) {
            lblMsgPermissoes.setText("Erro: " + e.getMessage());
        }
    }

    // utils
    private String safe(String s) { return s == null ? "" : s; }
    private String safeTrim(String s) { return s == null ? "" : s.trim(); }

    @FXML
    private void onFazerBackupAgora() {
        try {
            var r = br.com.clinica.service.BackupService.fazerBackupAgora(
                    java.nio.file.Paths.get(System.getProperty("user.home"), "ClinicaIntegracao", "backups")
            );

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    r.ok ? javafx.scene.control.Alert.AlertType.INFORMATION
                            : javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Backup do Sistema");
            alert.setHeaderText(r.ok ? "Backup concluído" : "Falha ao gerar backup");
            alert.setContentText(r.mensagem + (r.arquivo != null ? "\nArquivo: " + r.arquivo.toAbsolutePath() : ""));
            alert.showAndWait();

        } catch (Exception e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Backup do Sistema");
            alert.setHeaderText("Erro inesperado");
            alert.setContentText("Não foi possível gerar o backup.\n" + e.getMessage());
            alert.showAndWait();
        }
    }

}
