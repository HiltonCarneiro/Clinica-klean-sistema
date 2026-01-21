package br.com.clinica.model.enums;

public enum SalaAtendimento {
    SALA_1("Sala 1"),
    SALA_2("Sala 2"),
    SALA_3("Sala 3");

    private final String descricao;

    SalaAtendimento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
