package br.com.clinica.controller;

import br.com.clinica.dao.ProdutoDAO;
import br.com.clinica.model.Produto;
import br.com.clinica.model.enums.TipoProduto;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
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

    // Quantidade: 0, 1, 10,5, 1.234,56
    private static final DecimalFormat DF_QTD = new DecimalFormat("#,##0.##", SYM_BR);

    // Moeda no input (sem "R$")
    private static final DecimalFormat DF_MOEDA_INPUT = new DecimalFormat("#,##0.00", SYM_BR);

    @FXML
    public void initialize() {
        cbTipo.setItems(FXCollections.observableArrayList(TipoProduto.values()));

        // DatePicker dd/MM/yyyy + máscara no editor
        configurarDatePickerBR(dpValidade);
        aplicarMascaraDataNoEditor(dpValidade);

        // Quantidade (estoque) — permite vírgula e ponto; sem “moeda”
        aplicarFiltroQuantidade(txtEstoqueAtual);
        aplicarFiltroQuantidade(txtEstoqueMinimo);

        // Preços — máscara de moeda (digitos -> ##.###,##)
        aplicarMascaraMoeda(txtPrecoCusto);
        aplicarMascaraMoeda(txtPrecoVenda);

        // Colunas
        colNome.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNome()));

        colTipo.setCellValueFactory(cell -> {
            TipoProduto t = cell.getValue().getTipo();
            return new SimpleStringProperty(t != null ? t.getDescricao() : "");
        });

        colEstoque.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getEstoqueAtual()));
        colEstoque.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : DF_QTD.format(v.doubleValue()));
            }
        });

        colValidade.setCellValueFactory(cell -> {
            LocalDate v = cell.getValue().getValidade();
            return new SimpleStringProperty(v != null ? v.format(DATA_BR) : "");
        });

        colAtivo.setCellValueFactory(cell -> new SimpleBooleanProperty(cell.getValue().isAtivo()));
        colAtivo.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Boolean ativo, boolean empty) {
                super.updateItem(ativo, empty);
                if (empty || ativo == null) setText(null);
                else setText(ativo ? "Ativo" : "Inativo");
            }
        });

        tblProdutos.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> preencherFormulario(newSel));

        // Lista / filtros
        filteredList = new FilteredList<>(masterList, p -> true);
        SortedList<Produto> sorted = new SortedList<>(filteredList);
        sorted.comparatorProperty().bind(tblProdutos.comparatorProperty());
        tblProdutos.setItems(sorted);

        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltroBusca());

        chkAtivo.setSelected(true);
        lblMensagem.setText("");

        atualizarLista();
    }

    // AÇÕES

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

    // LISTA / BUSCA

    private void atualizarLista() {
        boolean incluirInativos = chkMostrarInativos != null && chkMostrarInativos.isSelected();
        boolean baixoEstoque = chkBaixoEstoque != null && chkBaixoEstoque.isSelected();
        boolean vencendo = chkVencendo != null && chkVencendo.isSelected();

        List<Produto> lista = produtoDAO.listar(incluirInativos, baixoEstoque, vencendo);
        masterList.setAll(lista);
        aplicarFiltroBusca();
    }

    private void aplicarFiltroBusca() {
        String termo = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();

        if (termo.isBlank()) {
            filteredList.setPredicate(p -> true);
            return;
        }

        filteredList.setPredicate(p -> {
            if (p == null) return false;
            String nome = safeLower(p.getNome());
            String lote = safeLower(p.getLote());
            String tipo = p.getTipo() != null ? safeLower(p.getTipo().getDescricao()) : "";
            return nome.contains(termo) || tipo.contains(termo) || lote.contains(termo);
        });
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    // FORM

    private void limparFormulario() {
        selecionado = null;
        txtNome.clear();
        cbTipo.getSelectionModel().clearSelection();
        txtEstoqueAtual.setText("0");
        txtEstoqueMinimo.setText("0");
        txtLote.clear();
        dpValidade.setValue(null);
        dpValidade.getEditor().clear();
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

        txtEstoqueAtual.setText(DF_QTD.format(p.getEstoqueAtual()));
        txtEstoqueMinimo.setText(DF_QTD.format(p.getEstoqueMinimo()));

        txtLote.setText(p.getLote());

        dpValidade.setValue(p.getValidade());

        dpValidade.getEditor().setText(p.getValidade() != null ? p.getValidade().format(DATA_BR) : "");

        txtPrecoCusto.setText(formatarMoedaInput(p.getPrecoCusto()));
        txtPrecoVenda.setText(formatarMoedaInput(p.getPrecoVenda()));

        chkAtivo.setSelected(p.isAtivo());
    }

    private Produto obterDoFormulario() {
        String nome = txtNome.getText() != null ? txtNome.getText().trim() : "";
        if (nome.isBlank()) throw new IllegalArgumentException("Informe o nome do produto.");

        // Em clínica/hospital é comum ter números no nome: "Vit D3 2000UI", "Seringa 10ml", etc.
        TipoProduto tipo = cbTipo.getValue();
        if (tipo == null) throw new IllegalArgumentException("Selecione o tipo do produto.");

        double estoqueAtual = parseNumero(txtEstoqueAtual.getText());
        double estoqueMinimo = parseNumero(txtEstoqueMinimo.getText());

        String lote = txtLote.getText();

        // Se o usuário digitou no editor, tenta converter
        LocalDate validade = dpValidade.getValue();
        if (validade == null) {
            String txt = dpValidade.getEditor().getText();
            if (txt != null && txt.trim().length() == 10) {
                try { validade = LocalDate.parse(txt.trim(), DATA_BR); } catch (Exception ignored) {}
            }
        }

        Double precoCusto = parseMoedaNullable(txtPrecoCusto.getText());
        Double precoVenda = parseMoedaNullable(txtPrecoVenda.getText());

        Produto p = (selecionado != null) ? selecionado : new Produto();
        p.setNome(nome);
        p.setTipo(tipo);
        p.setEstoqueAtual(estoqueAtual);
        p.setEstoqueMinimo(estoqueMinimo);
        p.setLote(lote);
        p.setValidade(validade);
        p.setPrecoCusto(precoCusto);
        p.setPrecoVenda(precoVenda);
        p.setAtivo(chkAtivo.isSelected());

        return p;
    }

    // MÁSCARAS / FORMATOS

    private void configurarDatePickerBR(DatePicker dp) {
        dp.setPromptText("dd/MM/aaaa");
        dp.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate date) {
                return date == null ? "" : date.format(DATA_BR);
            }
            @Override public LocalDate fromString(String string) {
                if (string == null || string.trim().isBlank()) return null;
                try { return LocalDate.parse(string.trim(), DATA_BR); }
                catch (Exception e) { return null; }
            }
        });
    }

    /**
     * Máscara de data no editor do DatePicker:
     * - aceita só números
     * - monta automaticamente dd/MM/aaaa
     * Ex: "01022026" -> "01/02/2026"
     */
    private void aplicarMascaraDataNoEditor(DatePicker dp) {
        TextField editor = dp.getEditor();

        editor.textProperty().addListener((obs, old, neu) -> {
            if (neu == null) return;

            String digits = neu.replaceAll("\\D", "");
            if (digits.length() > 8) digits = digits.substring(0, 8);

            String formatted = formatarDataDigits(digits);

            if (!formatted.equals(neu)) {
                int caret = formatted.length();
                editor.setText(formatted);
                editor.positionCaret(caret);
            }
        });
    }

    private String formatarDataDigits(String digits) {
        if (digits.isEmpty()) return "";
        if (digits.length() <= 2) return digits;
        if (digits.length() <= 4) return digits.substring(0, 2) + "/" + digits.substring(2);
        return digits.substring(0, 2) + "/" + digits.substring(2, 4) + "/" + digits.substring(4);
    }

    /**
     * Quantidade: permite digitar números com vírgula/ponto.
     * (Sem conversão agressiva para não atrapalhar o uso em estoque)
     */
    private void aplicarFiltroQuantidade(TextField tf) {
        tf.setTextFormatter(new TextFormatter<>(change -> {
            String novo = change.getControlNewText();
            if (novo == null || novo.isBlank()) return change;
            // permite dígitos, vírgula, ponto, espaço e sinal
            return novo.matches("[0-9.,\\- ]*") ? change : null;
        }));
    }

    /**
     * Máscara de moeda estilo clínica/hospital:
     * - usuário digita apenas números
     * - o campo vira automaticamente "1.234,56" (pt-BR)
     * Ex: digita 1 1 1 1 1 1 -> "1.111,11"
     */
    private void aplicarMascaraMoeda(TextField tf) {
        tf.setTextFormatter(new TextFormatter<>(change -> {
            String novo = change.getControlNewText();
            if (novo == null) return change;

            // Permite limpar o campo
            if (novo.isBlank()) {
                return change;
            }

            // Se digitou qualquer coisa que não seja dígito (ex: letras),
            // bloqueia a digitação imediatamente.
            // (Mantém a experiência "digita números e vira ##.###,##")
            String digits = novo.replaceAll("\\D", "");
            boolean temLetraOuSimbolo = !novo.equals(digits) && novo.matches(".*[A-Za-z].*");
            if (temLetraOuSimbolo) {
                return null; // bloqueia
            }

            // Mantém só dígitos (centavos)
            digits = digits.replaceFirst("^0+(?!$)", ""); // tira zeros à esquerda
            if (digits.length() > 14) digits = digits.substring(0, 14);

            // Se não sobrou dígito (ex: usuário tentou digitar só letras/símbolos), bloqueia
            if (digits.isEmpty()) {
                return null;
            }

            // Converte "digits" em valor monetário (centavos)
            java.math.BigDecimal value = new java.math.BigDecimal(digits).movePointLeft(2);
            value = value.setScale(2, java.math.RoundingMode.DOWN);

            String formatted = DF_MOEDA_INPUT.format(value);

            // Substitui o texto inteiro pelo formatado
            change.setText(formatted);
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(formatted.length());
            change.setAnchor(formatted.length());
            return change;
        }));
    }

    private String formatarMoedaInput(Double valor) {
        if (valor == null) return "";
        BigDecimal v = BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
        return DF_MOEDA_INPUT.format(v);
    }

    private double parseNumero(String texto) {
        Double v = parseNumeroNullable(texto);
        return v == null ? 0.0 : v;
    }

    private Double parseNumeroNullable(String texto) {
        if (texto == null) return null;
        String s = texto.trim();
        if (s.isBlank()) return null;

        // remove tudo que não for número/ponto/vírgula/sinal
        s = s.replaceAll("[^0-9,\\.\\-]", "");

        // pt-BR: se tiver . e , juntos -> . é milhar, , é decimal
        if (s.contains(",") && s.contains(".")) {
            s = s.replace(".", "").replace(",", ".");
        } else if (s.contains(",")) {
            s = s.replace(",", ".");
        }

        if (s.isBlank() || s.equals("-")) return null;

        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return null; }
    }

    private Double parseMoedaNullable(String texto) {
        // com a máscara, geralmente já vem “1.234,56”, mas garantimos parse mesmo assim
        return parseNumeroNullable(texto);
    }

    private void selecionarNaTabela(Produto p) {
        if (p == null || p.getId() == null) return;
        for (Produto item : masterList) {
            if (p.getId().equals(item.getId())) {
                tblProdutos.getSelectionModel().select(item);
                tblProdutos.scrollTo(item);
                break;
            }
        }
    }
}