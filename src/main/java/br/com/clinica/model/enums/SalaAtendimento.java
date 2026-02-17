package br.com.clinica.model.enums;

public enum SalaAtendimento {
    // Mantém os "names" (SALA_1, SALA_2, SALA_3)
    SALA_1("Consultório 1"),
    SALA_2("Consultório 2"),
    SALA_3("Sala de Procedimentos");

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