package br.com.clinica.controller;

import br.com.clinica.auth.AuthGuard;
import br.com.clinica.auth.Permissao;
import br.com.clinica.dao.AuditoriaDAO;
import br.com.clinica.dao.AuditoriaDAO.LinhaAuditoria;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AuditoriaController {

    @FXML private ComboBox<Integer> cbLimite;
    @FXML private TextField txtFiltro;

    @FXML private TableView<LinhaAuditoria> tblAuditoria;
    @FXML private TableColumn<LinhaAuditoria, Number> colId;
    @FXML private TableColumn<LinhaAuditoria, String> colDataHora;
    @FXML private TableColumn<LinhaAuditoria, String> colUsuario;
    @FXML private TableColumn<LinhaAuditoria, String> colAcao;
    @FXML private TableColumn<LinhaAuditoria, String> colEntidade;
    @FXML private TableColumn<LinhaAuditoria, String> colEntidadeId;
    @FXML private TableColumn<LinhaAuditoria, String> colDetalhes;

    @FXML private Label lblStatus;

    private final AuditoriaDAO dao = new AuditoriaDAO();

    @FXML
    public void initialize() {
        // garante admin-only via permissão
        AuthGuard.exigirPermissao(Permissao.AUDITORIA_VER);

        cbLimite.setItems(FXCollections.observableArrayList(100, 200, 500, 1000, 2000));
        cbLimite.getSelectionModel().select(Integer.valueOf(200));

        colId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().id));
        colDataHora.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().dataHora)));
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().usuario)));
        colAcao.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().acao)));
        colEntidade.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().entidade)));
        colEntidadeId.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().entidadeId)));
        colDetalhes.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().detalhes)));

        carregar();

        cbLimite.setOnAction(e -> carregar());
        txtFiltro.textProperty().addListener((obs, o, n) -> aplicarFiltro());
    }

    @FXML
    private void onAtualizar() {
        carregar();
    }

    private void carregar() {
        int limite = cbLimite.getValue() != null ? cbLimite.getValue() : 200;
        var lista = FXCollections.observableArrayList(dao.listarUltimos(limite));
        tblAuditoria.setItems(lista);
        aplicarFiltro();

        if (lblStatus != null) lblStatus.setText("Registros carregados: " + tblAuditoria.getItems().size());
    }

    private void aplicarFiltro() {
        if (tblAuditoria.getItems() == null) return;

        String termo = txtFiltro.getText() == null ? "" : txtFiltro.getText().trim().toLowerCase();

        var base = FXCollections.observableArrayList(tblAuditoria.getItems());
        FilteredList<LinhaAuditoria> filtrada = new FilteredList<>(base, r -> true);

        if (!termo.isBlank()) {
            filtrada.setPredicate(r -> join(r).toLowerCase().contains(termo));
        }

        tblAuditoria.setItems(filtrada);
        if (lblStatus != null) lblStatus.setText("Registros carregados: " + filtrada.size());
    }

    private String join(LinhaAuditoria r) {
        return nvl(r.dataHora) + " " +
                nvl(r.usuario) + " " +
                nvl(r.acao) + " " +
                nvl(r.entidade) + " " +
                nvl(r.entidadeId) + " " +
                nvl(r.detalhes);
    }

    private String nvl(String s) { return s == null ? "" : s; }
}