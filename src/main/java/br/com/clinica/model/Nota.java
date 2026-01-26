package br.com.clinica.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Nota {

    private Long id;
    private LocalDateTime dataHora;
    private Paciente paciente;
    private Usuario profissional;
    private String formaPagamento; // DINHEIRO, PIX, CARTAO etc.
    private double totalBruto;     // soma dos itens
    private double desconto;       // hoje sempre 0, mas já fica preparado
    private double totalLiquido;   // totalBruto - desconto
    private String observacao;
    private List<NotaItem> itens = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Usuario getProfissional() {
        return profissional;
    }

    public void setProfissional(Usuario profissional) {
        this.profissional = profissional;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public double getTotalBruto() {
        return totalBruto;
    }

    public void setTotalBruto(double totalBruto) {
        this.totalBruto = totalBruto;
    }

    public double getDesconto() {
        return desconto;
    }

    public void setDesconto(double desconto) {
        this.desconto = desconto;
    }

    public double getTotalLiquido() {
        return totalLiquido;
    }

    public void setTotalLiquido(double totalLiquido) {
        this.totalLiquido = totalLiquido;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<NotaItem> getItens() {
        return itens;
    }

    public void setItens(List<NotaItem> itens) {
        this.itens = itens;
    }

    public void adicionarItem(NotaItem item) {
        if (itens == null) {
            itens = new ArrayList<>();
        }
        itens.add(item);
        recalcularTotais();
    }

    public void removerItem(NotaItem item) {
        if (itens != null) {
            itens.remove(item);
            recalcularTotais();
        }
    }

    /**
     * Hoje: desconto sempre 0. Se um dia a dona da clínica quiser usar desconto,
     * é só usar o campo normalmente.
     */
    public void recalcularTotais() {
        double soma = 0.0;
        if (itens != null) {
            for (NotaItem item : itens) {
                soma += item.getValorTotal();
            }
        }
        this.totalBruto = soma;
        this.desconto = 0.0;
        this.totalLiquido = soma - this.desconto;
    }
}

