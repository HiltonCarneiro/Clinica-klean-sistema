package br.com.clinica.model;

/**
 * Tipo de produto para o estoque.
 * No momento só existem dois tipos:
 *  - SUPLEMENTO
 *  - INSUMO
 *
 * A coluna 'tipo' da tabela 'produto' é TEXT e armazena o name() do enum,
 * por exemplo: 'SUPLEMENTO' ou 'INSUMO'.
 */
public enum TipoProduto {

    SUPLEMENTO("Suplemento"),
    INSUMO("Insumo");

    private final String descricao;

    TipoProduto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        // usado automaticamente pelo ComboBox
        return descricao;
    }

    /**
     * Converte o valor vindo do banco (name ou descrição) para o enum.
     */
    public static TipoProduto fromDatabase(String valor) {
        if (valor == null) {
            return null;
        }
        for (TipoProduto t : values()) {
            if (t.name().equalsIgnoreCase(valor)
                    || t.descricao.equalsIgnoreCase(valor)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de produto desconhecido: " + valor);
    }

    /**
     * Valor que será gravado na coluna TEXT do banco.
     */
    public String toDatabase() {
        return name();
    }
}
