package br.com.clinica.controller;

import br.com.clinica.dao.ProdutoDAO;
import br.com.clinica.model.Produto;
import br.com.clinica.model.TipoProduto;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EstoqueController {

    @FXML
    private TextField txtNome;

    @FXML
    private ComboBox<TipoProduto> cbTipo;       // AGORA COM TipoProduto

    @FXML
    private TextField txtUnidade;

    @FXML
    private TextField txtEstoqueAtual;

    @FXML
    private TextField txtEstoqueMinimo;

    @FXML
    private TextField txtLote;

    @FXML
    private DatePicker dpValidade;

    @FXML
    private TextField txtPrecoCusto;

    @FXML
    private TextField txtPrecoVenda;

    @FXML
    private CheckBox chkAtivo;

    @FXML
    private CheckBox chkMostrarInativos;

    @FXML
    private CheckBox chkBaixoEstoque;

    @FXML
    private CheckBox chkVencendo;

    @FXML
    private TableView<Produto> tableProdutos;

    @FXML
    private TableColumn<Produto, String> colNome;

    @FXML
    private TableColumn<Produto, String> colTipo;

    @FXML
    private TableColumn<Produto, Number> colEstoque;

    @FXML
    private TableColumn<Produto, String> colValidade;

    @FXML
    private TableColumn<Produto, Boolean> colAtivo;

    @FXML
    private Label lblMensagem;

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final ObservableList<Produto> dados = FXCollections.observableArrayList();

    private Produto selecionado;

    private final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        // Preenche tipos no ComboBox usando o enum (vai mostrar o rótulo bonitinho)
        cbTipo.setItems(FXCollections.observableArrayList(TipoProduto.values()));

        // Configura as colunas da tabela
        colNome.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNome()));

        colTipo.setCellValueFactory(c -> {
            TipoProduto t = c.getValue().getTipo();
            return new SimpleStringProperty(t != null ? t.getRotulo() : "");
        });

        colEstoque.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getEstoqueAtual()));

        colValidade.setCellValueFactory(c -> {
            LocalDate v = c.getValue().getValidade();
            return new SimpleStringProperty(v != null ? v.format(BR) : "");
        });

        colAtivo.setCellValueFactory(c ->
                new SimpleBooleanProperty(c.getValue().isAtivo()));

        tableProdutos.setItems(dados);
        tableProdutos.setOnMouseClicked(this::onSelecionarProduto);

        carregarLista();
        novoProduto();
    }

    private void carregarLista() {
        boolean incluirInativos = chkMostrarInativos.isSelected();
        boolean baixoEstoque = chkBaixoEstoque.isSelected();
        boolean vencendo = chkVencendo.isSelected();

        dados.setAll(produtoDAO.listar(incluirInativos, baixoEstoque, vencendo));
    }

    private void novoProduto() {
        selecionado = null;
        txtNome.clear();
        cbTipo.getSelectionModel().clearSelection();
        txtUnidade.clear();
        txtEstoqueAtual.setText("0");
        txtEstoqueMinimo.setText("0");
        txtLote.clear();
        dpValidade.setValue(null);
        txtPrecoCusto.clear();
        txtPrecoVenda.clear();
        chkAtivo.setSelected(true);
        lblMensagem.setText("");
    }

    @FXML
    private void onNovo() {
        novoProduto();
    }

    @FXML
    private void onSalvar() {
        lblMensagem.setText("");

        if (txtNome.getText().isBlank()) {
            lblMensagem.setText("Informe o nome do produto.");
            return;
        }

        if (cbTipo.getValue() == null) {
            lblMensagem.setText("Selecione o tipo do produto.");
            return;
        }

        try {
            Produto p = (selecionado == null) ? new Produto() : selecionado;

            p.setNome(txtNome.getText().trim());
            p.setTipo(cbTipo.getValue()); // Enum
            p.setUnidade(txtUnidade.getText().trim());

            double estAtual = parseDouble(txtEstoqueAtual.getText());
            double estMin = parseDouble(txtEstoqueMinimo.getText());
            p.setEstoqueAtual(estAtual);
            p.setEstoqueMinimo(estMin);

            p.setLote(txtLote.getText().trim());
            p.setValidade(dpValidade.getValue());

            p.setPrecoCusto(parseDoubleNullable(txtPrecoCusto.getText()));
            p.setPrecoVenda(parseDoubleNullable(txtPrecoVenda.getText()));

            p.setAtivo(chkAtivo.isSelected());

            produtoDAO.salvar(p);

            lblMensagem.setText("Produto salvo com sucesso!");
            carregarLista();
            novoProduto();

        } catch (NumberFormatException e) {
            lblMensagem.setText("Verifique os valores numéricos (estoque / preços).");
        }
    }

    private double parseDouble(String texto) {
        if (texto == null || texto.isBlank()) return 0.0;
        return Double.parseDouble(texto.replace(",", "."));
    }

    private Double parseDoubleNullable(String texto) {
        if (texto == null || texto.isBlank()) return null;
        return Double.parseDouble(texto.replace(",", "."));
    }

    @FXML
    private void onAtivarDesativar() {
        Produto p = tableProdutos.getSelectionModel().getSelectedItem();
        if (p == null) {
            lblMensagem.setText("Selecione um produto na tabela.");
            return;
        }

        p.setAtivo(!p.isAtivo());
        produtoDAO.ativarDesativar(p);
        carregarLista();

        lblMensagem.setText("Produto " + (p.isAtivo() ? "ativado" : "inativado") + " com sucesso.");
    }

    @FXML
    private void onAtualizarLista() {
        carregarLista();
    }

    private void onSelecionarProduto(MouseEvent event) {
        Produto p = tableProdutos.getSelectionModel().getSelectedItem();
        if (p == null) return;

        selecionado = p;

        txtNome.setText(p.getNome());
        cbTipo.setValue(p.getTipo());
        txtUnidade.setText(p.getUnidade());
        txtEstoqueAtual.setText(String.valueOf(p.getEstoqueAtual()));
        txtEstoqueMinimo.setText(String.valueOf(p.getEstoqueMinimo()));
        txtLote.setText(p.getLote());
        dpValidade.setValue(p.getValidade());
        txtPrecoCusto.setText(p.getPrecoCusto() != null ? String.valueOf(p.getPrecoCusto()) : "");
        txtPrecoVenda.setText(p.getPrecoVenda() != null ? String.valueOf(p.getPrecoVenda()) : "");
        chkAtivo.setSelected(p.isAtivo());
    }
}
