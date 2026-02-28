package br.com.clinica.controller;

import br.com.clinica.dao.NotaDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.ProdutoDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.*;
import br.com.clinica.model.enums.TipoItemNota;
import br.com.clinica.service.NotaPdfService;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class CaixaController {

    // Controles da parte superior
    @FXML private ComboBox<Paciente> cbPaciente;
    @FXML private ComboBox<Usuario> cbProfissional;
    @FXML private ComboBox<String> cbFormaPagamento;
    @FXML private TextArea txtObservacao;

    // Controles de tipo de item
    @FXML private RadioButton rbProduto;
    @FXML private RadioButton rbProcedimento;

    private final ToggleGroup grupoTipoItem = new ToggleGroup();

    // Controles de dados do item
    @FXML private ComboBox<Produto> cbProduto;
    @FXML private TextField txtDescricaoProcedimento;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtValorUnitario;

    @FXML private Button btnAdicionarItem;
    @FXML private Button btnRemoverItem;

    // Tabela de itens
    @FXML private TableView<NotaItem> tblItens;
    @FXML private TableColumn<NotaItem, String> colDescricao;
    @FXML private TableColumn<NotaItem, String> colTipo;
    @FXML private TableColumn<NotaItem, Double> colQuantidade;
    @FXML private TableColumn<NotaItem, Double> colValorUnitario;
    @FXML private TableColumn<NotaItem, Double> colValorTotal;

    @FXML private Label lblTotal;
    @FXML private Button btnFinalizar;
    @FXML private Button btnFechar;

    // DAOs
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final NotaDAO notaDAO = new NotaDAO();

    // Serviço para geração de PDF
    private final NotaPdfService notaPdfService = new NotaPdfService();

    // Listas principais (originais) para pesquisa
    private final ObservableList<Paciente> pacientes = FXCollections.observableArrayList();
    private final ObservableList<Usuario> profissionais = FXCollections.observableArrayList();
    private final ObservableList<Produto> produtos = FXCollections.observableArrayList();

    // Lista de itens da nota
    private final ObservableList<NotaItem> itensNota = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarToggleGroup();
        carregarCombosPrincipais();
        configurarTabela();
        configurarTipoItem();

        configurarMascarasCamposNumericos();

        // Autocomplete/pesquisa
        configurarPesquisaCombos();

        atualizarTotal();
    }

    // =========================
    // Máscaras: Quantidade (inteiro) e Valor (moeda)
    // =========================
    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DecimalFormatSymbols DFS_BR = new DecimalFormatSymbols(LOCALE_PT_BR);
    private static final DecimalFormat DF_MOEDA = new DecimalFormat("#,##0.00", DFS_BR);

    private void configurarMascarasCamposNumericos() {
        if (txtQuantidade != null) {
            // Apenas números inteiros (sem letras)
            txtQuantidade.setTextFormatter(new TextFormatter<String>(change -> {
                if (!change.isContentChange()) return change;
                String novo = change.getControlNewText();
                if (novo == null || novo.isEmpty()) return change; // permite limpar
                if (novo.matches("\\d+")) return change;
                return null;
            }));
        }

        if (txtValorUnitario != null) {
            // Máscara de moeda: digita "1234" => "12,34"
            txtValorUnitario.setTextFormatter(new TextFormatter<String>(change -> {
                if (!change.isContentChange()) return change;

                String newText = change.getControlNewText();
                if (newText == null) return change;

                String digits = newText.replaceAll("\\D", "");

                // permite limpar totalmente
                if (digits.isEmpty()) {
                    change.setText("");
                    change.setRange(0, change.getControlText().length());
                    change.selectRange(0, 0);
                    return change;
                }

                // evita números gigantes
                if (digits.length() > 15) digits = digits.substring(0, 15);

                long cents = Long.parseLong(digits);
                double valor = cents / 100.0;
                String formatted = DF_MOEDA.format(valor);

                int oldLen = change.getControlText() == null ? 0 : change.getControlText().length();
                change.setText(formatted);
                change.setRange(0, oldLen);
                change.selectRange(formatted.length(), formatted.length());
                return change;
            }));
        }
    }

    private void setValorUnitarioMonetario(Double valor) {
        if (txtValorUnitario == null) return;
        if (valor == null) {
            txtValorUnitario.clear();
            return;
        }
        txtValorUnitario.setText(DF_MOEDA.format(valor));
    }

    private double parseMoedaBR(String texto) {
        if (texto == null) throw new NumberFormatException("Valor vazio.");
        String s = texto.trim();
        if (s.isEmpty()) throw new NumberFormatException("Valor vazio.");

        // remove símbolo e espaços; mantém dígitos e separadores
        s = s.replaceAll("[^0-9,\\.]", "");
        if (s.isEmpty()) throw new NumberFormatException("Valor vazio.");

        // remove separador de milhar e normaliza decimal
        s = s.replace(".", "");
        s = s.replace(",", ".");

        return Double.parseDouble(s);
    }

    private void configurarToggleGroup() {
        rbProduto.setToggleGroup(grupoTipoItem);
        rbProcedimento.setToggleGroup(grupoTipoItem);
        rbProduto.setSelected(true);
    }

    private void carregarCombosPrincipais() {
        // Pacientes
        pacientes.setAll(pacienteDAO.listarAtivos());
        cbPaciente.setItems(pacientes);

        // Profissionais (filtrados no DAO)
        profissionais.setAll(usuarioDAO.listarProfissionaisAtivos());
        cbProfissional.setItems(profissionais);

        // Formas de pagamento
        cbFormaPagamento.setItems(FXCollections.observableArrayList(
                "DINHEIRO", "PIX", "CREDITO", "DEBITO"
        ));
        if (!cbFormaPagamento.getItems().isEmpty()) {
            cbFormaPagamento.getSelectionModel().selectFirst();
        }

        // Produtos
        produtos.setAll(produtoDAO.listar(false, false, false));
        cbProduto.setItems(produtos);
    }

    private void configurarTabela() {
        tblItens.setItems(itensNota);

        colDescricao.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDescricao()));

        colTipo.setCellValueFactory(cell -> {
            TipoItemNota tipo = cell.getValue().getTipoItem();
            String texto;
            if (tipo == TipoItemNota.PRODUTO) texto = "Produto";
            else if (tipo == TipoItemNota.PROCEDIMENTO) texto = "Procedimento";
            else texto = "";
            return new SimpleStringProperty(texto);
        });

        colQuantidade.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getQuantidade()).asObject());

        colValorUnitario.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getValorUnitario()).asObject());

        colValorTotal.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getValorTotal()).asObject());
    }

    private void configurarTipoItem() {
        atualizarCamposTipoItem();

        grupoTipoItem.selectedToggleProperty().addListener((obs, old, novo) -> atualizarCamposTipoItem());

        cbProduto.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null && novo.getPrecoVenda() != null) {
                setValorUnitarioMonetario(novo.getPrecoVenda());
            }
        });
    }

    private void atualizarCamposTipoItem() {
        boolean isProduto = rbProduto.isSelected();

        cbProduto.setDisable(!isProduto);
        txtDescricaoProcedimento.setDisable(isProduto);

        if (isProduto) txtDescricaoProcedimento.clear();

        txtQuantidade.setDisable(false);
        txtValorUnitario.setDisable(false);
    }

    // Pesquisa em Combos

    private void configurarPesquisaCombos() {
        // Paciente: pesquisa pelo nome
        configurarComboPesquisa(cbPaciente, pacientes, p -> safe(p.getNome()));

        // Produto: pesquisa pelo nome
        configurarComboPesquisa(cbProduto, produtos, p -> safe(p.getNome()));

        // Profissional: "Pessoa (CARGO)"
        configurarComboPesquisa(cbProfissional, profissionais, this::textoProfissional);
    }

    private String textoProfissional(Usuario u) {
        if (u == null) return "";
        String pessoa = safe(u.getPessoaNome());
        String cargo = safe(u.getNome()); // no seu projeto, "nome" é o cargo
        if (pessoa.isBlank() && !safe(u.getLogin()).isBlank()) pessoa = u.getLogin();
        if (cargo.isBlank()) return pessoa;
        return pessoa + " (" + cargo + ")";
    }

    private <T> void configurarComboPesquisa(ComboBox<T> comboBox,
                                             ObservableList<T> itensOriginais,
                                             Function<T, String> textoFunc) {

        comboBox.setEditable(true);

        FilteredList<T> filtrados = new FilteredList<>(itensOriginais, p -> true);
        comboBox.setItems(filtrados);

        StringConverter<T> conv = new StringConverter<>() {
            @Override public String toString(T obj) { return obj == null ? "" : safe(textoFunc.apply(obj)); }
            @Override public T fromString(String s) { return comboBox.getValue(); }
        };
        comboBox.setConverter(conv);

        comboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : conv.toString(item));
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : conv.toString(item));
            }
        });

        TextField editor = comboBox.getEditor();

        editor.textProperty().addListener((obs, old, texto) -> {
            T sel = comboBox.getSelectionModel().getSelectedItem();
            if (sel != null && safe(textoFunc.apply(sel)).equals(texto)) return;

            String t = (texto == null) ? "" : texto.trim().toLowerCase();

            filtrados.setPredicate(item -> {
                if (t.isBlank()) return true;
                return safe(textoFunc.apply(item)).toLowerCase().contains(t);
            });

            if (!comboBox.isShowing()) comboBox.show();
        });

        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                Platform.runLater(() -> {
                    comboBox.getEditor().setText(conv.toString(novo));
                    comboBox.hide();
                });
            }
        });

        editor.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                confirmarSelecaoOuManter(comboBox, filtrados, textoFunc);
                comboBox.hide();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                comboBox.hide();
                e.consume();
            }
        });

        editor.focusedProperty().addListener((obs, old, focado) -> {
            if (Boolean.TRUE.equals(focado)) return;
            confirmarSelecaoOuManter(comboBox, filtrados, textoFunc);
            comboBox.hide();
        });

        comboBox.setOnHidden(e -> {
            T sel = comboBox.getSelectionModel().getSelectedItem();
            if (sel != null) {
                editor.setText(conv.toString(sel));
            }
            filtrados.setPredicate(x -> true);
        });
    }

    private <T> void confirmarSelecaoOuManter(ComboBox<T> comboBox,
                                              FilteredList<T> filtrados,
                                              Function<T, String> textoFunc) {

        String digitado = safe(comboBox.getEditor().getText()).trim();
        T atual = comboBox.getSelectionModel().getSelectedItem();

        if (digitado.isBlank()) {
            comboBox.getSelectionModel().clearSelection();
            comboBox.getEditor().clear();
            return;
        }

        T exato = null;
        for (T item : filtrados.getSource()) {
            if (safe(textoFunc.apply(item)).equalsIgnoreCase(digitado)) {
                exato = item;
                break;
            }
        }

        if (exato != null) {
            comboBox.getSelectionModel().select(exato);
            comboBox.getEditor().setText(safe(textoFunc.apply(exato)));
            return;
        }

        if (!filtrados.isEmpty()) {
            T first = filtrados.get(0);
            comboBox.getSelectionModel().select(first);
            comboBox.getEditor().setText(safe(textoFunc.apply(first)));
            return;
        }

        if (atual != null) {
            comboBox.getEditor().setText(safe(textoFunc.apply(atual)));
        } else {
            comboBox.getEditor().clear();
        }
    }

    private String safe(String s) { return s == null ? "" : s; }

    // Itens

    @FXML
    private void onAdicionarItem() {
        try {
            if (rbProduto.isSelected()) adicionarItemProduto();
            else adicionarItemProcedimento();

            atualizarTotal();
            limparCamposItem();

        } catch (NumberFormatException e) {
            mostrarErro("Valor inválido", "Informe valores numéricos válidos para quantidade e valor.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao adicionar item", e.getMessage());
        }
    }

    private void adicionarItemProduto() {
        Produto produto = cbProduto.getValue();
        if (produto == null) {
            mostrarErro("Produto obrigatório", "Selecione um produto para adicionar à nota.");
            return;
        }

        double quantidade = lerQuantidade();
        double valorUnitario = lerValorUnitario(produto.getPrecoVenda());

        NotaItem item = new NotaItem();
        item.setTipoItem(TipoItemNota.PRODUTO);
        item.setProduto(produto);
        item.setDescricao(produto.getNome());
        item.setQuantidade(quantidade);
        item.setValorUnitario(valorUnitario);

        itensNota.add(item);
    }

    private void adicionarItemProcedimento() {
        String descricao = txtDescricaoProcedimento.getText();
        if (descricao == null || descricao.isBlank()) {
            mostrarErro("Descrição obrigatória", "Informe a descrição do procedimento.");
            return;
        }

        double quantidade = lerQuantidade();
        if (quantidade <= 0) quantidade = 1.0;

        double valorUnitario = lerValorUnitario(null);

        NotaItem item = new NotaItem();
        item.setTipoItem(TipoItemNota.PROCEDIMENTO);
        item.setDescricao(descricao);
        item.setQuantidade(quantidade);
        item.setValorUnitario(valorUnitario);

        itensNota.add(item);
    }

    private double lerQuantidade() {
        String textoQtd = txtQuantidade.getText();
        if (textoQtd == null || textoQtd.isBlank()) return 1.0;

        // quantidade é inteira (sem letras)
        String digits = textoQtd.replaceAll("\\D", "");
        if (digits.isBlank()) return 1.0;

        return Double.parseDouble(digits);
    }

    private double lerValorUnitario(Double valorSugestao) {
        String textoValor = txtValorUnitario.getText();

        if ((textoValor == null || textoValor.isBlank()) && valorSugestao != null) return valorSugestao;
        if (textoValor == null || textoValor.isBlank()) throw new NumberFormatException("Valor unitário vazio.");

        return parseMoedaBR(textoValor);
    }

    @FXML
    private void onRemoverItem() {
        NotaItem selecionado = tblItens.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAviso("Selecione um item", "Selecione um item na tabela para remover.");
            return;
        }
        itensNota.remove(selecionado);
        atualizarTotal();
    }

    private void atualizarTotal() {
        double soma = 0.0;
        for (NotaItem item : itensNota) soma += item.getValorTotal();
        lblTotal.setText(String.format("R$ %.2f", soma));
    }

    private void limparCamposItem() {
        txtDescricaoProcedimento.clear();
    }

    // Nota

    @FXML
    private void onFinalizarNota() {
        try {
            Paciente paciente = cbPaciente.getValue();
            Usuario profissional = cbProfissional.getValue();
            String formaPagamento = cbFormaPagamento.getValue();

            if (paciente == null) { mostrarErro("Paciente obrigatório", "Selecione um paciente."); return; }
            if (profissional == null) { mostrarErro("Profissional obrigatório", "Selecione um profissional."); return; }
            if (formaPagamento == null || formaPagamento.isBlank()) { mostrarErro("Forma de pagamento obrigatória", "Selecione a forma de pagamento."); return; }
            if (itensNota.isEmpty()) { mostrarErro("Nenhum item", "Adicione pelo menos um item à nota antes de finalizar."); return; }

            Nota nota = new Nota();
            nota.setDataHora(LocalDateTime.now());
            nota.setPaciente(paciente);
            nota.setProfissional(profissional);
            nota.setFormaPagamento(formaPagamento);
            nota.setObservacao(txtObservacao.getText());

            List<NotaItem> listaItens = new ArrayList<>(itensNota);
            nota.setItens(listaItens);
            nota.recalcularTotais();

            notaDAO.salvarNota(nota);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar nota em PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Arquivo PDF", "*.pdf"));

            String nomePaciente = paciente.getNome() != null
                    ? paciente.getNome().replaceAll("[^a-zA-Z0-9_\\- ]", "")
                    : "nota";

            fileChooser.setInitialFileName("nota_" + nomePaciente + ".pdf");

            File destino = fileChooser.showSaveDialog(lblTotal.getScene().getWindow());

            if (destino != null) {
                try {
                    notaPdfService.gerarRecibo(nota, destino);
                    mostrarAviso("Nota salva",
                            "Nota gravada com sucesso!\n\nPDF gerado em:\n" + destino.getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mostrarErro("Erro ao gerar PDF", ex.getMessage());
                    return;
                }
            } else {
                mostrarAviso("Nota salva",
                        "Nota gravada com sucesso.\n(O PDF não foi gerado porque o salvamento foi cancelado.)");
            }

            limparFormulario();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao salvar nota", e.getMessage());
        }
    }

    private void limparFormulario() {
        itensNota.clear();
        atualizarTotal();
        txtObservacao.clear();
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) lblTotal.getScene().getWindow();
        stage.close();
    }

    // Util
    private void mostrarErro(String titulo, String detalhe) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(titulo);
        alert.setContentText(detalhe);
        alert.showAndWait();
    }

    private void mostrarAviso(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}