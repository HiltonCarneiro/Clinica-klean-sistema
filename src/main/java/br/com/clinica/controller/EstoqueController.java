package br.com.clinica.controller;

import br.com.clinica.controller.estoque.EstoqueFilterManager;
import br.com.clinica.controller.estoque.EstoqueFormManager;
import br.com.clinica.controller.estoque.EstoqueMaskManager;
import br.com.clinica.controller.estoque.EstoqueTableManager;
import br.com.clinica.dao.ProdutoDAO;
import br.com.clinica.model.Produto;
import br.com.clinica.model.enums.TipoProduto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class EstoqueController {

    @FXML private TextField txtNome;
    @FXML private ComboBox<TipoProduto> cbTipo;
    @FXML private TextField txtEstoqueAtual;
    @FXML private TextField txtEstoqueMinimo;
    @FXML private TextField txtLote;
    @FXML private DatePicker dpValidade;
    @FXML private TextField txtPrecoCusto;
    @FXML private TextField txtPrecoVenda;
    @FXML private CheckBox chkAtivo;

    @FXML private CheckBox chkMostrarInativos;
    @FXML private CheckBox chkBaixoEstoque;
    @FXML private CheckBox chkVencendo;

    @FXML private TextField txtBuscar;

    @FXML private TableView<Produto> tblProdutos;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, String> colTipo;
    @FXML private TableColumn<Produto, Number> colEstoque;
    @FXML private TableColumn<Produto, String> colValidade;
    @FXML private TableColumn<Produto, Boolean> colAtivo;

    @FXML private Label lblMensagem;

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private Produto selecionado;

    private final ObservableList<Produto> masterList = FXCollections.observableArrayList();
    private FilteredList<Produto> filteredList;

    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormatSymbols SYM_BR = DecimalFormatSymbols.getInstance(LOCALE_BR);

    private static final DecimalFormat DF_QTD = new DecimalFormat("#,##0.##", SYM_BR);
    private static final DecimalFormat DF_MOEDA_INPUT = new DecimalFormat("#,##0.00", SYM_BR);

    private final EstoqueTableManager tableManager =
            new EstoqueTableManager(DF_QTD, DATA_BR);

    private final EstoqueFilterManager filterManager =
            new EstoqueFilterManager();

    private final EstoqueMaskManager maskManager =
            new EstoqueMaskManager(DATA_BR, DF_MOEDA_INPUT);

    private final EstoqueFormManager formManager =
            new EstoqueFormManager(maskManager, DF_QTD, DATA_BR);

    @FXML
    public void initialize() {
        cbTipo.setItems(FXCollections.observableArrayList(TipoProduto.values()));

        configurarMascaras();
        configurarTabela();
        configurarSelecaoTabela();
        configurarListaEFiltros();

        chkAtivo.setSelected(true);
        lblMensagem.setText("");

        atualizarLista();
    }

    private void configurarMascaras() {
        maskManager.configurarDatePickerBR(dpValidade);
        maskManager.aplicarMascaraDataNoEditor(dpValidade);

        maskManager.aplicarFiltroQuantidade(txtEstoqueAtual);
        maskManager.aplicarFiltroQuantidade(txtEstoqueMinimo);

        maskManager.aplicarMascaraMoeda(txtPrecoCusto);
        maskManager.aplicarMascaraMoeda(txtPrecoVenda);
    }

    private void configurarTabela() {
        tableManager.configurarTabela(
                colNome,
                colTipo,
                colEstoque,
                colValidade,
                colAtivo
        );
    }

    private void configurarSelecaoTabela() {
        tblProdutos.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> preencherFormulario(newSel));
    }

    private void configurarListaEFiltros() {
        filteredList = filterManager.criarFiltro(masterList);

        SortedList<Produto> sorted = new SortedList<>(filteredList);
        sorted.comparatorProperty().bind(tblProdutos.comparatorProperty());

        tblProdutos.setItems(sorted);

        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltroBusca());
    }

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

            lblMensagem.setText("Salvo com sucesso!");

            atualizarLista();
            selecionarNaTabela(p);

        } catch (IllegalArgumentException ex) {
            lblMensagem.setText(ex.getMessage());

        } catch (Exception ex) {
            ex.printStackTrace();
            lblMensagem.setText("Erro ao salvar: " + ex.getMessage());
        }
    }

    @FXML
    private void onAtivarDesativar() {
        lblMensagem.setText("");

        Produto sel = tblProdutos.getSelectionModel().getSelectedItem();

        if (sel == null) {
            lblMensagem.setText("Selecione um produto na tabela.");
            return;
        }

        try {
            sel.setAtivo(!sel.isAtivo());

            produtoDAO.salvar(sel);

            atualizarLista();
            selecionarNaTabela(sel);

            lblMensagem.setText("Status atualizado!");

        } catch (Exception e) {
            e.printStackTrace();
            lblMensagem.setText("Erro ao atualizar status.");
        }
    }

    @FXML
    private void onAtualizarLista() {
        atualizarLista();
    }

    @FXML
    private void onLimparBusca() {
        txtBuscar.clear();
        aplicarFiltroBusca();
    }

    private void atualizarLista() {
        boolean incluirInativos = chkMostrarInativos != null && chkMostrarInativos.isSelected();
        boolean baixoEstoque = chkBaixoEstoque != null && chkBaixoEstoque.isSelected();
        boolean vencendo = chkVencendo != null && chkVencendo.isSelected();

        List<Produto> lista = produtoDAO.listar(
                incluirInativos,
                baixoEstoque,
                vencendo
        );

        masterList.setAll(lista);
        aplicarFiltroBusca();
    }

    private void aplicarFiltroBusca() {
        filterManager.aplicarFiltroBusca(
                filteredList,
                txtBuscar.getText()
        );
    }

    private void limparFormulario() {
        selecionado = null;

        formManager.limparFormulario(
                txtNome,
                cbTipo,
                txtEstoqueAtual,
                txtEstoqueMinimo,
                txtLote,
                dpValidade,
                txtPrecoCusto,
                txtPrecoVenda,
                chkAtivo
        );
    }

    private void preencherFormulario(Produto p) {
        if (p == null) {
            limparFormulario();
            return;
        }

        selecionado = p;

        formManager.preencherFormulario(
                p,
                txtNome,
                cbTipo,
                txtEstoqueAtual,
                txtEstoqueMinimo,
                txtLote,
                dpValidade,
                txtPrecoCusto,
                txtPrecoVenda,
                chkAtivo
        );
    }

    private Produto obterDoFormulario() {
        return formManager.obterDoFormulario(
                selecionado,
                txtNome,
                cbTipo,
                txtEstoqueAtual,
                txtEstoqueMinimo,
                txtLote,
                dpValidade,
                txtPrecoCusto,
                txtPrecoVenda,
                chkAtivo
        );
    }

    private void selecionarNaTabela(Produto p) {
        if (p == null || p.getId() == null) {
            return;
        }

        for (Produto item : masterList) {
            if (p.getId().equals(item.getId())) {
                tblProdutos.getSelectionModel().select(item);
                tblProdutos.scrollTo(item);
                break;
            }
        }
    }
}