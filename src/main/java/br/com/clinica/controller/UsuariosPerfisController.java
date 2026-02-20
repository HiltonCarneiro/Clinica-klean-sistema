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

    // ===================== USUÁRIOS =====================
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

    // ===================== PERFIS =====================
    @FXML private TextField txtNovoPerfil;
    @FXML private TableView<AdminUsuariosPerfisDAO.PerfilRow> tblPerfis;
    @FXML private TableColumn<AdminUsuariosPerfisDAO.PerfilRow, Number> colPId;
    @FXML private TableColumn<AdminUsuariosPerfisDAO.PerfilRow, String> colPNome;
    @FXML private TextField txtRenomearPerfil;
    @FXML private Label lblMsgPerfis;

    // ✅ novo
    @FXML private Button btnExcluirPerfil;

    // ===================== PERMISSÕES =====================
    @FXML private ComboBox<AdminUsuariosPerfisDAO.PerfilRow> cbPerfilPermissoes;
    @FXML private VBox boxPermissoes;
    @FXML private Label lblMsgPermissoes;

    private final Map<String, CheckBox> checks = new HashMap<>();

    @FXML
    private void initialize() {
        // --------- tabela usuários
        colUId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().id));
        colULogin.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().login)));
        colUPessoa.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().pessoaNome)));
        colUCargo.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().nome)));
        colUPerfil.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().perfilNome)));
        colUAtivo.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().ativo));

        colUAtivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : (v ? "Sim" : "Não"));
            }
        });

        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) carregarUsuario(n);
        });

        if (txtBuscaUsuario != null) {
            txtBuscaUsuario.textProperty().addListener((obs, o, n) -> atualizarUsuarios());
        }

        // --------- tabela perfis
        colPId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().id));
        colPNome.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().nome)));

        // ✅ excluir só habilita com seleção
        if (btnExcluirPerfil != null) {
            btnExcluirPerfil.setDisable(true);
            tblPerfis.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                btnExcluirPerfil.setDisable(n == null);
            });
        }

        // carregar listas
        atualizarPerfis();
        atualizarUsuarios();

        // checklist permissões
        montarChecklist();
    }

    // =========================================================
    // USUÁRIOS
    // =========================================================

    @FXML
    private void onAtualizarUsuarios() { atualizarUsuarios(); }

    private void atualizarUsuarios() {
        lblMsgUsuarios.setText("");
        String termo = txtBuscaUsuario != null ? txtBuscaUsuario.getText() : "";
        boolean incluirInativos = chkIncluirInativos != null && chkIncluirInativos.isSelected();

        var lista = adminDAO.listarUsuarios(termo, incluirInativos);
        tblUsuarios.setItems(FXCollections.observableArrayList(lista));
    }

    private void carregarUsuario(AdminUsuariosPerfisDAO.UsuarioRow u) {
        usuarioEditando = u;
        txtCargo.setText(safe(u.nome));
        txtPessoaNome.setText(safe(u.pessoaNome));
        txtLogin.setText(safe(u.login));
        txtSenha.clear();
        chkAtivoUsuario.setSelected(u.ativo);

        var found = cbPerfilUsuario.getItems().stream()
                .filter(p -> p.id == u.perfilId)
                .findFirst()
                .orElse(null);
        cbPerfilUsuario.setValue(found);

        lblMsgUsuarioForm.setText("");
    }

    @FXML
    private void onNovoUsuario() {
        usuarioEditando = null;
        txtCargo.clear();
        txtPessoaNome.clear();
        txtLogin.clear();
        txtSenha.clear();
        chkAtivoUsuario.setSelected(true);
        cbPerfilUsuario.getSelectionModel().clearSelection();
        lblMsgUsuarioForm.setText("");
    }

    @FXML
    private void onCancelarUsuario() {
        lblMsgUsuarioForm.setText("");
        tblUsuarios.getSelectionModel().clearSelection();
        onNovoUsuario();
    }

    @FXML
    private void onSalvarUsuario() {
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

        try {
            if (usuarioEditando == null) {
                if (senha.isBlank()) { lblMsgUsuarioForm.setText("Informe a senha."); return; }

                AdminUsuariosPerfisDAO.UsuarioRow novo = new AdminUsuariosPerfisDAO.UsuarioRow();
                novo.nome = cargo;
                novo.pessoaNome = pessoa;
                novo.login = login;
                novo.senha = senha;
                novo.ativo = ativo;
                novo.perfilId = perfil.id;

                adminDAO.inserirUsuario(novo);
                lblMsgUsuarioForm.setText("Usuário criado.");

            } else {
                // se não digitar senha, mantém a atual
                String senhaFinal = senha.isBlank() ? usuarioEditando.senha : senha;

                AdminUsuariosPerfisDAO.UsuarioRow upd = new AdminUsuariosPerfisDAO.UsuarioRow();
                upd.id = usuarioEditando.id;
                upd.nome = cargo;
                upd.pessoaNome = pessoa;
                upd.login = login;
                upd.senha = senhaFinal;
                upd.ativo = ativo;
                upd.perfilId = perfil.id;

                adminDAO.atualizarUsuario(upd);
                lblMsgUsuarioForm.setText("Usuário atualizado.");
            }

            atualizarUsuarios();

        } catch (Exception e) {
            lblMsgUsuarioForm.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    private void onAtivarInativarUsuario() {
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

    @FXML
    private void onResetarSenha() {
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

    @FXML
    private void onFazerBackupAgora() {
        try {
            var r = br.com.clinica.service.BackupService.fazerBackupAgora(
                    java.nio.file.Paths.get(System.getProperty("user.home"), "ClinicaIntegracao", "backups")
            );

            Alert alert = new Alert(r.ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle("Backup do Sistema");
            alert.setHeaderText(r.ok ? "Backup concluído" : "Falha ao gerar backup");
            alert.setContentText(r.mensagem + (r.arquivo != null ? "\nArquivo: " + r.arquivo.toAbsolutePath() : ""));
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Backup do Sistema");
            alert.setHeaderText("Falha ao gerar backup");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // =========================================================
    // PERFIS
    // =========================================================

    @FXML
    private void onAtualizarPerfis() { atualizarPerfis(); }

    private void atualizarPerfis() {
        var perfis = adminDAO.listarPerfis();
        var obs = FXCollections.observableArrayList(perfis);
        tblPerfis.setItems(obs);
        cbPerfilUsuario.setItems(obs);
        cbPerfilPermissoes.setItems(obs);
    }

    @FXML
    private void onCriarPerfil() {
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

    @FXML
    private void onRenomearPerfil() {
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

    // ✅ NOVO: excluir perfil
    @FXML
    private void onExcluirPerfil() {
        lblMsgPerfis.setText("");
        var sel = tblPerfis.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsgPerfis.setText("Selecione um perfil."); return; }

        String nome = safeTrim(sel.nome).toUpperCase();
        if (nome.equals("ADMINISTRADOR")) {
            lblMsgPerfis.setText("O perfil ADMINISTRADOR não pode ser excluído.");
            return;
        }

        try {
            if (permDAO.perfilEmUso(sel.id)) {
                lblMsgPerfis.setText("Não é possível excluir: perfil em uso (usuários e/ou permissões).");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar exclusão");
            confirm.setHeaderText("Excluir perfil");
            confirm.setContentText("Tem certeza que deseja excluir o perfil \"" + sel.nome + "\"?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            permDAO.excluirPerfil(sel.id);
            atualizarPerfis();
            lblMsgPerfis.setText("Perfil excluído.");
        } catch (Exception e) {
            lblMsgPerfis.setText("Erro: " + e.getMessage());
        }
    }

    // =========================================================
    // PERMISSÕES
    // =========================================================

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

    @FXML
    private void onCarregarPermissoes() {
        lblMsgPermissoes.setText("");
        var perfil = cbPerfilPermissoes.getValue();
        if (perfil == null) { lblMsgPermissoes.setText("Selecione um perfil."); return; }

        try {
            Set<String> atuais = permDAO.listarPorPerfilId(perfil.id);
            for (var e : checks.entrySet()) {
                e.getValue().setSelected(atuais.contains(e.getKey()));
            }
            lblMsgPermissoes.setText("Permissões carregadas.");
        } catch (Exception e) {
            lblMsgPermissoes.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    private void onSalvarPermissoes() {
        lblMsgPermissoes.setText("");
        var perfil = cbPerfilPermissoes.getValue();
        if (perfil == null) { lblMsgPermissoes.setText("Selecione um perfil."); return; }

        try {
            Set<String> sel = checks.entrySet().stream()
                    .filter(e -> e.getValue().isSelected())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            permDAO.salvarPermissoes(perfil.id, sel);
            lblMsgPermissoes.setText("Permissões salvas.");
        } catch (Exception e) {
            lblMsgPermissoes.setText("Erro: " + e.getMessage());
        }
    }

    // =========================================================
    // UTILS
    // =========================================================

    private static String safe(String s) { return s == null ? "" : s; }
    private static String safeTrim(String s) { return s == null ? "" : s.trim(); }
}