package br.com.clinica.controller.paciente;

import javafx.scene.Node;

public class PacienteScreenState {

    public void show(Node node, boolean visible) {

        if (node == null) {
            return;
        }

        node.setVisible(visible);
        node.setManaged(visible);
    }
}