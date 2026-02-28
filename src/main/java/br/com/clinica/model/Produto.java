package br.com.clinica.model;

import br.com.clinica.model.enums.TipoProduto;

import java.time.LocalDate;


public class Produto {

    private Long id;
    private String nome;
    private TipoProduto tipo;
    private double estoqueAtual;
    private double estoqueMinimo;
    private String lote;
    private LocalDate validade;
    private Double precoCusto;
    private Double precoVenda;
    private boolean ativo;

    public Produto() {
        this.ativo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public TipoProduto getTipo() { return tipo; }
    public void setTipo(TipoProduto tipo) { this.tipo = tipo; }

    public double getEstoqueAtual() { return estoqueAtual; }
    public void setEstoqueAtual(double estoqueAtual) { this.estoqueAtual = estoqueAtual; }

    public double getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(double estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public LocalDate getValidade() { return validade; }
    public void setValidade(LocalDate validade) { this.validade = validade; }

    public Double getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(Double precoCusto) { this.precoCusto = precoCusto; }

    public Double getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(Double precoVenda) { this.precoVenda = precoVenda; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    @Override
    public String toString() {
        return nome != null ? nome : "Produto";
    }
}