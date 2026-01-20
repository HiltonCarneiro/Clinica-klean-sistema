package br.com.clinica.model;

/**
 * Tipo do produto no estoque.
 * - No BANCO: usamos name() => SUPLEMENTO, ORAL, INJETAVEL, TOPICO
 * - Na TELA: usamos o rótulo com acento => "Suplemento", "Oral", "Injetável", "Tópico"
 */
public enum TipoProduto {

    SUPLEMENTO("Suplemento"),
    ORAL("Oral"),
    INJETAVEL("Injetável"),
    TOPICO("Tópico");

    private final String rotulo;

    TipoProduto(String rotulo) {
        this.rotulo = rotulo;
    }

    /** Texto bonito pra mostrar na tela */
    public String getRotulo() {
        return rotulo;
    }

    /** Como salvar no banco (sem acento) */
    public String toDatabase() {
        return this.name(); // SUPLEMENTO, ORAL, INJETAVEL, TOPICO
    }

    /** Converter valor do banco para enum */
    public static TipoProduto fromDatabase(String value) {
        if (value == null) return null;
        return TipoProduto.valueOf(value); // espera SUPLEMENTO, ORAL, INJETAVEL, TOPICO
    }

    /** Para ComboBox e TableView mostrarem o rótulo automaticamente */
    @Override
    public String toString() {
        return rotulo;
    }
}