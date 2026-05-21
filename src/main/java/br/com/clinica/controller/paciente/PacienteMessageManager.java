package br.com.clinica.controller.paciente;

import javafx.scene.control.Label;

public class PacienteMessageManager {

    public void setMensagem(Label lblMensagem, String mensagem) {

        if (lblMensagem != null) {
            lblMensagem.setText(
                    mensagem == null ? "" : mensagem
            );
        }
    }
}
