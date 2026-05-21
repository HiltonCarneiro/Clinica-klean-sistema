package br.com.clinica.controller.estoque;

import br.com.clinica.model.Produto;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class EstoqueFilterManager {

    public FilteredList<Produto> criarFiltro(
            ObservableList<Produto> masterList
    ) {
        return new FilteredList<>(masterList, p -> true);
    }

    public void aplicarFiltroBusca(
            FilteredList<Produto> filteredList,
            String termo
    ) {

        String busca = termo == null
                ? ""
                : termo.trim().toLowerCase();

        if (busca.isBlank()) {
            filteredList.setPredicate(p -> true);
            return;
        }

        filteredList.setPredicate(produto -> {

            if (produto == null) {
                return false;
            }

            String nome = safeLower(produto.getNome());

            String lote = safeLower(produto.getLote());

            String tipo =
                    produto.getTipo() != null
                            ? safeLower(produto.getTipo().getDescricao())
                            : "";

            return nome.contains(busca)
                    || lote.contains(busca)
                    || tipo.contains(busca);
        });
    }

    private String safeLower(String s) {
        return s == null
                ? ""
                : s.toLowerCase();
    }
}