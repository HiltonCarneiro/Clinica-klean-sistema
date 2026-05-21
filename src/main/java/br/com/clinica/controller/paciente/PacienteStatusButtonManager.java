package br.com.clinica.controller.paciente;

import br.com.clinica.model.Paciente;
import javafx.scene.control.Button;

public class PacienteStatusButtonManager {

    public void atualizarBotoesStatus(
            Paciente paciente,
            Button btnInativar,
            Button btnAtivar
    ) {

        boolean temPaciente =
                paciente != null
                        && paciente.getId() != null;

        boolean ativo =
                temPaciente
                        && paciente.isAtivo();

        if (btnInativar != null) {
            btnInativar.setDisable(!temPaciente || !ativo);
        }

        if (btnAtivar != null) {
            btnAtivar.setDisable(!temPaciente || ativo);
        }
    }
}
