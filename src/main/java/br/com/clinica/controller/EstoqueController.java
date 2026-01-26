package br.com.clinica.controller;

import br.com.clinica.dao.ProdutoDAO;
import br.com.clinica.model.Produto;
import br.com.clinica.model.TipoProduto;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EstoqueController {

    // ====== CAMPOS DO FORMULÁRIO ======

    @FXML
    private TextField txtNome;

    @FXML
    private ComboBox<TipoProduto> cbTipo;

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

    // ====== FILTROS ======

    @FXML
    private CheckBox chkMostrarInativos;

    @FXML
    private CheckBox chkBaixoEstoque;

    @FXML
    private CheckBox chkVencendo;

    // ====== TABELA ======

    @FXML
    private TableView<Produto> tblProdutos;

    @FXML
    private TableColumn<Produto, String> colNome;

    @FXML
    private TableColumn<Produto, String> colTipo;

    @FXML
    private TableColumn<Produto, String> colUnidade;

    @FXML
    private TableColumn<Produto, Number> colEstoque;

    @FXML
    private TableColumn<Produto, String> colValidade;

    @FXML
    private TableColumn<Produto, Boolean> colAtivo;

    // ====== MENSAGEM ======

    @FXML
    private Label lblMensagem;

    // ====== INFRA ======

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private Produto selecionado;

    private static final DateTimeFormatter DATA_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        // Combo de tipos: agora só SUPLEMENTO e INSUMO
        cbTipo.setItems(FXCollections.observableArrayList(TipoProduto.values()));

        // Colunas da tabela
        colNome.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getNome()));

        colTipo.setCellValueFactory(cell -> {
            TipoProduto t = cell.getValue().getTipo();
            return new SimpleStringProperty(t != null ? t.getDescricao() : "");
        });

        colEstoque.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getEstoqueAtual()));

        colValidade.setCellValueFactory(cell -> {
            LocalDate v = cell.getValue().getValidade();
            String texto = v != null ? v.format(DATA_BR) : "";
            return new SimpleStringProperty(texto);
        });

        colAtivo.setCellValueFactory(cell ->
                new SimpleBooleanProperty(cell.getValue().isAtivo()));

        colAtivo.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean ativo, boolean empty) {
                super.updateItem(ativo, empty);
                if (empty || ativo == null) {
                    setText(null);
                } else {
                    setText(ativo ? "Ativo" : "Inativo");
                }
            }
        });

        // seleção na tabela -> preenche o formulário
        tblProdutos.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> preencherFormulario(newSel));

        chkAtivo.setSelected(true);
        lblMensagem.setText("");

        atualizarLista();
    }

    // ====== BOTÕES ======

    @FXML
    private void onNovo() {
        limparFormulario();
        tblProdutos.getSelectionModel().clearSelection();
        txtNome.requestFocus();
        lblMensagem.setText("");
    }

    @FXML
    private void onSalvar() {
        lblMensagem.setText("");

        try {
            Produto p = obterDoFormulario();
            produtoDAO.salvar(p);
            atualizarLista();
            selecionarNaTabela(p);
            lblMensagem.setText("Produto salvo com sucesso!");
        } catch (IllegalArgumentException e) {
            lblMensagem.setText(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            lblMensagem.setText("Erro ao salvar produto: " + e.getMessage());
        }
    }

    @FXML
    private void onAtivarDesativar() {
        Produto p = tblProdutos.getSelectionModel().getSelectedItem();
        if (p == null) {
            lblMensagem.setText("Selecione um produto na tabela.");
            return;
        }

        produtoDAO.ativarDesativar(p);
        atualizarLista();
        selecionarNaTabela(p);
        lblMensagem.setText("Produto " + (p.isAtivo() ? "ativado" : "inativado") + " com sucesso.");
    }

    @FXML
    private void onAtualizarLista() {
        atualizarLista();
    }

    // ====== APOIO ======

    private void atualizarLista() {
        boolean incluirInativos = chkMostrarInativos != null && chkMostrarInativos.isSelected();
        boolean baixoEstoque = chkBaixoEstoque != null && chkBaixoEstoque.isSelected();
        boolean vencendo = chkVencendo != null && chkVencendo.isSelected();

        List<Produto> produtos = produtoDAO.listar(incluirInativos, baixoEstoque, vencendo);
        tblProdutos.setItems(FXCollections.observableArrayList(produtos));
    }

    private void limparFormulario() {
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
    }

    private void preencherFormulario(Produto p) {
        if (p == null) {
            limparFormulario();
            return;
        }

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

    private Produto obterDoFormulario() {
        String nome = txtNome.getText() != null ? txtNome.getText().trim() : "";
        if (nome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do produto.");
        }

        TipoProduto tipo = cbTipo.getValue();
        if (tipo == null) {
            throw new IllegalArgumentException("Selecione o tipo do produto (Suplemento ou Insumo).");
        }

        String unidade = txtUnidade.getText() != null ? txtUnidade.getText().trim() : "";
        if (unidade.isBlank()) {
            throw new IllegalArgumentException("Informe a unidade (ex.: un, ml, cx).");
        }

        double estoqueAtual = parseDouble(txtEstoqueAtual.getText(), "Estoque atual");
        double estoqueMinimo = parseDouble(txtEstoqueMinimo.getText(), "Estoque mínimo");

        String lote = txtLote.getText();
        LocalDate validade = dpValidade.getValue();

        Double precoCusto = parseDoubleNullable(txtPrecoCusto.getText());
        Double precoVenda = parseDoubleNullable(txtPrecoVenda.getText());

        Produto p = (selecionado != null) ? selecionado : new Produto();

        p.setNome(nome);
        p.setTipo(tipo);
        p.setUnidade(unidade);
        p.setEstoqueAtual(estoqueAtual);
        p.setEstoqueMinimo(estoqueMinimo);
        p.setLote(lote);
        p.setValidade(validade);
        p.setPrecoCusto(precoCusto);
        p.setPrecoVenda(precoVenda);
        p.setAtivo(chkAtivo.isSelected());

        return p;
    }

    private double parseDouble(String texto, String campo) {
        try {
            if (texto == null) return 0.0;
            texto = texto.trim().replace(",", ".");
            if (texto.isBlank()) return 0.0;
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido no campo '" + campo + "'.");
        }
    }

    private Double parseDoubleNullable(String texto) {
        if (texto == null) return null;
        texto = texto.trim().replace(",", ".");
        if (texto.isBlank()) return null;
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido em preço.");
        }
    }

    private void selecionarNaTabela(Produto p) {
        if (p == null || p.getId() == null) return;

        for (Produto item : tblProdutos.getItems()) {
            if (p.getId().equals(item.getId())) {
                tblProdutos.getSelectionModel().select(item);
                tblProdutos.scrollTo(item);
                break;
            }
        }
    }
}
