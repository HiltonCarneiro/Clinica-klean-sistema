package br.com.clinica.controller.paciente;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class PacienteScreenManager {

    private final VBox boxEscolha;
    private final ScrollPane scrollCadastro;
    private final VBox boxBusca;

    public PacienteScreenManager(
            VBox boxEscolha,
            ScrollPane scrollCadastro,
            VBox boxBusca
    ) {
        this.boxEscolha = boxEscolha;
        this.scrollCadastro = scrollCadastro;
        this.boxBusca = boxBusca;
    }

    public void mostrarEscolha() {
        show(scrollCadastro, false);
        show(boxBusca, false);
        show(boxEscolha, true);
    }

    public void mostrarCadastro() {
        show(boxEscolha, false);
        show(boxBusca, false);
        show(scrollCadastro, true);
    }

    public void mostrarBusca() {
        show(boxEscolha, false);
        show(scrollCadastro, false);
        show(boxBusca, true);
    }

    public boolean cadastroVisivel() {
        return scrollCadastro != null && scrollCadastro.isVisible();
    }

    private void show(Node node, boolean visible) {
        if (node == null) {
            return;
        }

        node.setVisible(visible);
        node.setManaged(visible);
    }
}