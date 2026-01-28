package br.com.clinica.controller;

import br.com.clinica.dao.NotaDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.ProdutoDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.*;
import br.com.clinica.service.NotaPdfService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CaixaController {

    // === Controles da parte superior ===
    @FXML
    private ComboBox<Paciente> cbPaciente;

    @FXML
    private ComboBox<Usuario> cbProfissional;

    @FXML
    private ComboBox<String> cbFormaPagamento;

    @FXML
    private TextArea txtObservacao;

    // === Controles de tipo de item ===
    @FXML
    private RadioButton rbProduto;

    @FXML
    private RadioButton rbProcedimento;

    // ToggleGroup será criado via código
    private final ToggleGroup grupoTipoItem = new ToggleGroup();

    // === Controles de dados do item ===
    @FXML
    private ComboBox<Produto> cbProduto;

    @FXML
    private TextField txtDescricaoProcedimento;

    @FXML
    private TextField txtQuantidade;

    @FXML
    private TextField txtValorUnitario;

    @FXML
    private Button btnAdicionarItem;

    @FXML
    private Button btnRemoverItem;

    // === Tabela de itens ===
    @FXML
    private TableView<NotaItem> tblItens;

    @FXML
    private TableColumn<NotaItem, String> colDescricao;

    @FXML
    private TableColumn<NotaItem, String> colTipo;

    @FXML
    private TableColumn<NotaItem, Double> colQuantidade;

    @FXML
    private TableColumn<NotaItem, Double> colValorUnitario;

    @FXML
    private TableColumn<NotaItem, Double> colValorTotal;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnFinalizar;

    @FXML
    private Button btnFechar;

    // === DAOs ===
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final NotaDAO notaDAO = new NotaDAO();

    // Serviço para geração de PDF
    private final NotaPdfService notaPdfService = new NotaPdfService();

    // Lista observável de itens da nota
    private final ObservableList<NotaItem> itensNota = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarToggleGroup();
        carregarCombosPrincipais();
        configurarTabela();
        configurarTipoItem();
        atualizarTotal();
    }

    // -------------------------------------------------------------------------
    // Inicialização / configuração
    // -------------------------------------------------------------------------

    private void configurarToggleGroup() {
        rbProduto.setToggleGroup(grupoTipoItem);
        rbProcedimento.setToggleGroup(grupoTipoItem);
        rbProduto.setSelected(true);
    }

    private void carregarCombosPrincipais() {
        List<Paciente> pacientes = pacienteDAO.listarAtivos();
        cbPaciente.setItems(FXCollections.observableArrayList(pacientes));

        List<Usuario> profissionais = usuarioDAO.listarProfissionaisAtivos();
        cbProfissional.setItems(FXCollections.observableArrayList(profissionais));

        cbFormaPagamento.setItems(FXCollections.observableArrayList(
                "DINHEIRO",
                "PIX",
                "CARTAO",
                "DEBITO"
        ));
        if (!cbFormaPagamento.getItems().isEmpty()) {
            cbFormaPagamento.getSelectionModel().selectFirst();
        }

        List<Produto> produtos = produtoDAO.listar(false, false, false);
        cbProduto.setItems(FXCollections.observableArrayList(produtos));
    }

    private void configurarTabela() {
        tblItens.setItems(itensNota);

        colDescricao.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDescricao()));

        colTipo.setCellValueFactory(cell -> {
            TipoItemNota tipo = cell.getValue().getTipoItem();
            String texto;
            if (tipo == TipoItemNota.PRODUTO) {
                texto = "Produto";
            } else if (tipo == TipoItemNota.PROCEDIMENTO) {
                texto = "Procedimento";
            } else {
                texto = "";
            }
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
                txtValorUnitario.setText(String.valueOf(novo.getPrecoVenda()));
            }
        });
    }

    private void atualizarCamposTipoItem() {
        boolean isProduto = rbProduto.isSelected();

        cbProduto.setDisable(!isProduto);
        txtDescricaoProcedimento.setDisable(isProduto);

        if (isProduto) {
            txtDescricaoProcedimento.clear();
        }

        txtQuantidade.setDisable(false);
        txtValorUnitario.setDisable(false);
    }

    // -------------------------------------------------------------------------
    // Ações de itens
    // -------------------------------------------------------------------------

    @FXML
    private void onAdicionarItem() {
        try {
            if (rbProduto.isSelected()) {
                adicionarItemProduto();
            } else {
                adicionarItemProcedimento();
            }
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
        if (quantidade <= 0) {
            quantidade = 1.0;
        }

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
        if (textoQtd == null || textoQtd.isBlank()) {
            return 1.0;
        }
        return Double.parseDouble(textoQtd.replace(",", "."));
    }

    private double lerValorUnitario(Double valorSugestao) {
        String textoValor = txtValorUnitario.getText();
        if ((textoValor == null || textoValor.isBlank()) && valorSugestao != null) {
            return valorSugestao;
        }
        if (textoValor == null || textoValor.isBlank()) {
            throw new NumberFormatException("Valor unitário vazio.");
        }
        return Double.parseDouble(textoValor.replace(",", "."));
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
        for (NotaItem item : itensNota) {
            soma += item.getValorTotal();
        }
        lblTotal.setText(String.format("R$ %.2f", soma));
    }

    private void limparCamposItem() {
        txtDescricaoProcedimento.clear();
        // mantém quantidade e valor
    }

    // -------------------------------------------------------------------------
    // Ações da nota
    // -------------------------------------------------------------------------

    @FXML
    private void onFinalizarNota() {
        try {
            Paciente paciente = cbPaciente.getValue();
            Usuario profissional = cbProfissional.getValue();
            String formaPagamento = cbFormaPagamento.getValue();

            if (paciente == null) {
                mostrarErro("Paciente obrigatório", "Selecione um paciente.");
                return;
            }
            if (profissional == null) {
                mostrarErro("Profissional obrigatório", "Selecione um profissional.");
                return;
            }
            if (formaPagamento == null || formaPagamento.isBlank()) {
                mostrarErro("Forma de pagamento obrigatória", "Selecione a forma de pagamento.");
                return;
            }
            if (itensNota.isEmpty()) {
                mostrarErro("Nenhum item", "Adicione pelo menos um item à nota antes de finalizar.");
                return;
            }

            Nota nota = new Nota();
            nota.setDataHora(LocalDateTime.now());
            nota.setPaciente(paciente);
            nota.setProfissional(profissional);
            nota.setFormaPagamento(formaPagamento);
            nota.setObservacao(txtObservacao.getText());

            List<NotaItem> listaItens = new ArrayList<>(itensNota);
            nota.setItens(listaItens);
            nota.recalcularTotais();

            // 1) Salva no banco (nota, itens, movimento, baixa estoque)
            notaDAO.salvarNota(nota);

            // 2) Escolhe onde salvar o PDF
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
                    return; // não limpa se der erro no PDF
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
        // mantém paciente, profissional e forma de pagamento
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) lblTotal.getScene().getWindow();
        stage.close();
    }

    // -------------------------------------------------------------------------
    // Métodos utilitários de mensagem
    // -------------------------------------------------------------------------

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
