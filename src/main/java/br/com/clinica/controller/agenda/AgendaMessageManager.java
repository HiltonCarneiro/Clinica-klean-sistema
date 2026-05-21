package br.com.clinica.controller.agenda;

import javafx.scene.control.Label;

public class AgendaMessageManager {

    public void setMessage(Label lblMensagem, String message) {
        if (lblMensagem != null) {
            lblMensagem.setText(message == null ? "" : message);
        }
    }
}