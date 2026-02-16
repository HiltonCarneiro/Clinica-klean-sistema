package br.com.clinica.controller;

import br.com.clinica.auth.AuthGuard;
import br.com.clinica.auth.Permissao;
import br.com.clinica.dao.AdminUsuariosPerfisDAO;
import br.com.clinica.dao.AdminUsuariosPerfisDAO.PerfilRow;
import br.com.clinica.dao.AdminUsuariosPerfisDAO.UsuarioRow;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UsuariosPerfisController {

    @FXML private ListView<PerfilRow> lstPerfis;
    @FXML private TextField txtNovoPerfil;
    @FXML private Label lblMsgPerfis;

    @FXML private TableView<UsuarioRow> tblUsuarios;
    @FXML private TextField txtBuscaUsuario;
    @FXML private CheckBox chkIncluirInativos;

    @FXML private TextField txtCargo;
    @FXML private TextField txtPessoaNome;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtSenha;
    @FXML private CheckBox chkAtivo;
    @FXML private ComboBox<PerfilRow> cbPerfil;

    @FXML private Label lblMsgUsuarios;

    private final AdminUsuariosPerfisDAO dao = new AdminUsuariosPerfisDAO();

    @FXML
    public void initialize() {
        AuthGuard.exigirPermissao(Permissao.USUARIO_GERENCIAR);
        carregarPerfis();
        carregarUsuarios();
    }

    // ===================== PERFIS =====================

    @FXML
    private void onCriarPerfil() {
        String nome = txtNovoPerfil.getText();
        if (nome == null || nome.isBlank()) {
            lblMsgPerfis.setText("Informe o nome do perfil.");
            return;
        }
        dao.criarPerfil(nome.trim());
        txtNovoPerfil.clear();
        lblMsgPerfis.setText("Perfil criado.");
        carregarPerfis();
    }

    @FXML
    private void onRenomearPerfil() {
        PerfilRow sel = lstPerfis.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMsgPerfis.setText("Selecione um perfil.");
            return;
        }
        String novoNome = txtNovoPerfil.getText();
        if (novoNome == null || novoNome.isBlank()) {
            lblMsgPerfis.setText("Informe o novo nome.");
            return;
        }
        dao.renomearPerfil(sel.id, novoNome.trim());
        lblMsgPerfis.setText("Perfil renomeado.");
        carregarPerfis();
    }

    private void carregarPerfis() {
        lstPerfis.setItems(FXCollections.observableArrayList(dao.listarPerfis()));
        cbPerfil.setItems(FXCollections.observableArrayList(dao.listarPerfis()));
    }

    // ===================== USUÁRIOS =====================

    @FXML
    private void onBuscarUsuarios() {
        carregarUsuarios();
    }

    private void carregarUsuarios() {
        String termo = txtBuscaUsuario.getText();
        boolean incluir = chkIncluirInativos.isSelected();
        tblUsuarios.setItems(FXCollections.observableArrayList(dao.listarUsuarios(termo, incluir)));
    }

    @FXML
    private void onNovoUsuario() {
        tblUsuarios.getSelectionModel().clearSelection();
        limparFormularioUsuario();
    }

    @FXML
    private void onSalvarUsuario() {
        try {
            UsuarioRow u = obterDoFormulario();
            if (u.id == 0) {
                dao.inserirUsuario(u);
                lblMsgUsuarios.setText("Usuário criado.");
            } else {
                dao.atualizarUsuario(u);
                lblMsgUsuarios.setText("Usuário atualizado.");
            }
            carregarUsuarios();
        } catch (RuntimeException ex) {
            lblMsgUsuarios.setText(ex.getMessage());
        }
    }

    @FXML
    private void onAtivarInativar() {
        UsuarioRow sel = tblUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        dao.ativarInativarUsuario(sel.id, !sel.ativo);
        carregarUsuarios();
    }

    @FXML
    private void onResetSenha() {
        UsuarioRow sel = tblUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (txtSenha.getText() == null || txtSenha.getText().isBlank()) {
            lblMsgUsuarios.setText("Informe a nova senha.");
            return;
        }
        dao.resetSenha(sel.id, txtSenha.getText());
        lblMsgUsuarios.setText("Senha resetada.");
    }

    private UsuarioRow obterDoFormulario() {
        UsuarioRow u = new UsuarioRow();
        UsuarioRow sel = tblUsuarios.getSelectionModel().getSelectedItem();
        if (sel != null) u.id = sel.id;

        u.nome = txtCargo.getText();
        u.pessoaNome = txtPessoaNome.getText();
        u.login = txtLogin.getText();
        u.senha = txtSenha.getText();
        u.ativo = chkAtivo.isSelected();

        PerfilRow p = cbPerfil.getSelectionModel().getSelectedItem();
        if (p == null) throw new RuntimeException("Selecione um perfil.");
        u.perfilId = p.id;

        if (u.login == null || u.login.isBlank()) throw new RuntimeException("Login é obrigatório.");
        if (u.id == 0 && (u.senha == null || u.senha.isBlank())) throw new RuntimeException("Senha é obrigatória ao criar.");

        return u;
    }

    private void limparFormularioUsuario() {
        txtCargo.clear();
        txtPessoaNome.clear();
        txtLogin.clear();
        txtSenha.clear();
        chkAtivo.setSelected(true);
        cbPerfil.getSelectionModel().clearSelection();
    }
}