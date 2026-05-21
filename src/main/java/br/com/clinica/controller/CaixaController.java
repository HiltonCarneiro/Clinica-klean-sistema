package br.com.clinica.controller;

import br.com.clinica.controller.caixa.CaixaAlertManager;
import br.com.clinica.controller.caixa.CaixaComboSearchManager;
import br.com.clinica.controller.caixa.CaixaMoneyFieldManager;
import br.com.clinica.controller.caixa.CaixaTableManager;
import br.com.clinica.dao.NotaDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.ProdutoDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.*;
import br.com.clinica.model.enums.TipoItemNota;
import br.com.clinica.service.NotaPdfService;
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

    @FXML private ComboBox<Paciente> cbPaciente;
    @FXML private ComboBox<Usuario> cbProfissional;
    @FXML private ComboBox<String> cbFormaPagamento;
    @FXML private TextArea txtObservacao;

    @FXML private RadioButton rbProduto;
    @FXML private RadioButton rbProcedimento;

    private final ToggleGroup grupoTipoItem = new ToggleGroup();

    @FXML private ComboBox<Produto> cbProduto;
    @FXML private TextField txtDescricaoProcedimento;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtValorUnitario;

    @FXML private Button btnAdicionarItem;
    @FXML private Button btnRemoverItem;

    @FXML private TableView<NotaItem> tblItens;
    @FXML private TableColumn<NotaItem, String> colDescricao;
    @FXML private TableColumn<NotaItem, String> colTipo;
    @FXML private TableColumn<NotaItem, Double> colQuantidade;
    @FXML private TableColumn<NotaItem, Double> colValorUnitario;
    @FXML private TableColumn<NotaItem, Double> colValorTotal;

    @FXML private Label lblTotal;
    @FXML private Button btnFinalizar;
    @FXML private Button btnFechar;

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final NotaDAO notaDAO = new NotaDAO();

    private final NotaPdfService notaPdfService = new NotaPdfService();

    private final CaixaAlertManager alertManager = new CaixaAlertManager();
    private final CaixaMoneyFieldManager moneyFieldManager = new CaixaMoneyFieldManager();
    private final CaixaTableManager tableManager = new CaixaTableManager();
    private final CaixaComboSearchManager comboSearchManager = new CaixaComboSearchManager();

    private final ObservableList<Paciente> pacientes = FXCollections.observableArrayList();
    private final ObservableList<Usuario> profissionais = FXCollections.observableArrayList();
    private final ObservableList<Produto> produtos = FXCollections.observableArrayList();
    private final ObservableList<NotaItem> itensNota = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarToggleGroup();
        carregarCombosPrincipais();
        configurarTabela();
        configurarTipoItem();
        configurarMascarasCamposNumericos();
        configurarPesquisaCombos();
        atualizarTotal();
    }

    private void configurarMascarasCamposNumericos() {
        moneyFieldManager.configurarMascarasCamposNumericos(
                txtQuantidade,
                txtValorUnitario
        );
    }

    private void setValorUnitarioMonetario(Double valor) {
        moneyFieldManager.setValorUnitarioMonetario(
                txtValorUnitario,
                valor
        );
    }

    private double parseMoedaBR(String texto) {
        return moneyFieldManager.parseMoedaBR(texto);
    }

    private void configurarToggleGroup() {
        rbProduto.setToggleGroup(grupoTipoItem);
        rbProcedimento.setToggleGroup(grupoTipoItem);
        rbProduto.setSelected(true);
    }

    private void carregarCombosPrincipais() {
        pacientes.setAll(pacienteDAO.listarAtivos());
        cbPaciente.setItems(pacientes);

        profissionais.setAll(usuarioDAO.listarProfissionaisAtivos());
        cbProfissional.setItems(profissionais);

        cbFormaPagamento.setItems(
                FXCollections.observableArrayList(
                        "DINHEIRO",
                        "PIX",
                        "CREDITO",
                        "DEBITO"
                )
        );

        if (!cbFormaPagamento.getItems().isEmpty()) {
            cbFormaPagamento.getSelectionModel().selectFirst();
        }

        produtos.setAll(produtoDAO.listar(false, false, false));
        cbProduto.setItems(produtos);
    }

    private void configurarTabela() {
        tableManager.configurarTabela(
                tblItens,
                colDescricao,
                colTipo,
                colQuantidade,
                colValorUnitario,
                colValorTotal,
                itensNota
        );
    }

    private void configurarTipoItem() {
        atualizarCamposTipoItem();

        grupoTipoItem.selectedToggleProperty().addListener(
                (obs, old, novo) -> atualizarCamposTipoItem()
        );

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

        if (isProduto) {
            txtDescricaoProcedimento.clear();
        }

        txtQuantidade.setDisable(false);
        txtValorUnitario.setDisable(false);
    }

    private void configurarPesquisaCombos() {
        comboSearchManager.configurarPesquisaCombos(
                cbPaciente,
                pacientes,
                cbProduto,
                produtos,
                cbProfissional,
                profissionais
        );
    }

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
            mostrarErro(
                    "Valor inválido",
                    "Informe valores numéricos válidos para quantidade e valor."
            );

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

        String digits = textoQtd.replaceAll("\\D", "");

        if (digits.isBlank()) {
            return 1.0;
        }

        return Double.parseDouble(digits);
    }

    private double lerValorUnitario(Double valorSugestao) {
        String textoValor = txtValorUnitario.getText();

        if ((textoValor == null || textoValor.isBlank()) && valorSugestao != null) {
            return valorSugestao;
        }

        if (textoValor == null || textoValor.isBlank()) {
            throw new NumberFormatException("Valor unitário vazio.");
        }

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

        for (NotaItem item : itensNota) {
            soma += item.getValorTotal();
        }

        lblTotal.setText(String.format("R$ %.2f", soma));
    }

    private void limparCamposItem() {
        txtDescricaoProcedimento.clear();
    }

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

            notaDAO.salvarNota(nota);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar nota em PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Arquivo PDF", "*.pdf")
            );

            String nomePaciente =
                    paciente.getNome() != null
                            ? paciente.getNome().replaceAll("[^a-zA-Z0-9_\\- ]", "")
                            : "nota";

            fileChooser.setInitialFileName("nota_" + nomePaciente + ".pdf");

            File destino = fileChooser.showSaveDialog(lblTotal.getScene().getWindow());

            if (destino != null) {
                try {
                    notaPdfService.gerarRecibo(nota, destino);

                    mostrarAviso(
                            "Nota salva",
                            "Nota gravada com sucesso!\n\nPDF gerado em:\n" + destino.getAbsolutePath()
                    );

                } catch (Exception ex) {
                    ex.printStackTrace();
                    mostrarErro("Erro ao gerar PDF", ex.getMessage());
                    return;
                }

            } else {
                mostrarAviso(
                        "Nota salva",
                        "Nota gravada com sucesso.\n(O PDF não foi gerado porque o salvamento foi cancelado.)"
                );
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

    private void mostrarErro(String titulo, String detalhe) {
        alertManager.mostrarErro(titulo, detalhe);
    }

    private void mostrarAviso(String titulo, String mensagem) {
        alertManager.mostrarAviso(titulo, mensagem);
    }
}