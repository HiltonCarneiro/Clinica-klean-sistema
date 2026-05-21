package br.com.clinica.controller.estoque;

import br.com.clinica.model.Produto;
import br.com.clinica.model.enums.TipoProduto;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class EstoqueFormManager {

    private final EstoqueMaskManager maskManager;
    private final DecimalFormat quantidadeFormatter;
    private final DateTimeFormatter dataFormatter;

    public EstoqueFormManager(
            EstoqueMaskManager maskManager,
            DecimalFormat quantidadeFormatter,
            DateTimeFormatter dataFormatter
    ) {
        this.maskManager = maskManager;
        this.quantidadeFormatter = quantidadeFormatter;
        this.dataFormatter = dataFormatter;
    }

    public void limparFormulario(
            TextField txtNome,
            ComboBox<TipoProduto> cbTipo,
            TextField txtEstoqueAtual,
            TextField txtEstoqueMinimo,
            TextField txtLote,
            DatePicker dpValidade,
            TextField txtPrecoCusto,
            TextField txtPrecoVenda,
            CheckBox chkAtivo
    ) {
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

    public void preencherFormulario(
            Produto p,
            TextField txtNome,
            ComboBox<TipoProduto> cbTipo,
            TextField txtEstoqueAtual,
            TextField txtEstoqueMinimo,
            TextField txtLote,
            DatePicker dpValidade,
            TextField txtPrecoCusto,
            TextField txtPrecoVenda,
            CheckBox chkAtivo
    ) {
        txtNome.setText(p.getNome());
        cbTipo.setValue(p.getTipo());

        txtEstoqueAtual.setText(quantidadeFormatter.format(p.getEstoqueAtual()));
        txtEstoqueMinimo.setText(quantidadeFormatter.format(p.getEstoqueMinimo()));

        txtLote.setText(p.getLote());

        dpValidade.setValue(p.getValidade());
        dpValidade.getEditor().setText(
                p.getValidade() != null
                        ? p.getValidade().format(dataFormatter)
                        : ""
        );

        txtPrecoCusto.setText(maskManager.formatarMoedaInput(p.getPrecoCusto()));
        txtPrecoVenda.setText(maskManager.formatarMoedaInput(p.getPrecoVenda()));

        chkAtivo.setSelected(p.isAtivo());
    }

    public Produto obterDoFormulario(
            Produto selecionado,
            TextField txtNome,
            ComboBox<TipoProduto> cbTipo,
            TextField txtEstoqueAtual,
            TextField txtEstoqueMinimo,
            TextField txtLote,
            DatePicker dpValidade,
            TextField txtPrecoCusto,
            TextField txtPrecoVenda,
            CheckBox chkAtivo
    ) {
        String nome = txtNome.getText() != null
                ? txtNome.getText().trim()
                : "";

        if (nome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do produto.");
        }

        TipoProduto tipo = cbTipo.getValue();

        if (tipo == null) {
            throw new IllegalArgumentException("Selecione o tipo do produto.");
        }

        double estoqueAtual = maskManager.parseNumero(txtEstoqueAtual.getText());
        double estoqueMinimo = maskManager.parseNumero(txtEstoqueMinimo.getText());

        Produto p = selecionado != null
                ? selecionado
                : new Produto();

        p.setNome(nome);
        p.setTipo(tipo);
        p.setEstoqueAtual(estoqueAtual);
        p.setEstoqueMinimo(estoqueMinimo);
        p.setLote(txtLote.getText());
        p.setValidade(maskManager.obterDataDigitada(dpValidade));
        p.setPrecoCusto(maskManager.parseMoedaNullable(txtPrecoCusto.getText()));
        p.setPrecoVenda(maskManager.parseMoedaNullable(txtPrecoVenda.getText()));
        p.setAtivo(chkAtivo.isSelected());

        return p;
    }
}