package br.com.clinica.controller.paciente;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class PacienteFormBehavior {

    public void configureNumeroBehavior(
            CheckBox chkSemNumero,
            TextField txtNumero
    ) {

        if (chkSemNumero == null || txtNumero == null) {
            return;
        }

        chkSemNumero.selectedProperty().addListener(
                (obs, oldVal, semNumero) -> {

                    txtNumero.setDisable(semNumero);

                    if (semNumero) {
                        txtNumero.clear();
                    }
                }
        );
    }
}