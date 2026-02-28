package br.com.clinica.model;

import br.com.clinica.model.enums.TipoItemNota;

public class NotaItem {

    private Long id;
    private Nota nota;
    private TipoItemNota tipoItem;
    private Produto produto;      // pode ser null se for PROCEDIMENTO
    private String descricao;
    private double quantidade;
    private double valorUnitario;
    private double valorTotal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Nota getNota() {
        return nota;
    }

    public void setNota(Nota nota) {
        this.nota = nota;
    }

    public TipoItemNota getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(TipoItemNota tipoItem) {
        this.tipoItem = tipoItem;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
        recalcularTotal();
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(double valorUnitario) {
        this.valorUnitario = valorUnitario;
        recalcularTotal();
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    private void recalcularTotal() {
        this.valorTotal = this.quantidade * this.valorUnitario;
    }
}
