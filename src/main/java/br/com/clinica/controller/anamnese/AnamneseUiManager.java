package br.com.clinica.controller.anamnese;

import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.session.Session;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AnamneseUiManager {

    public void preencherCabecalho(
            Label lblPaciente,
            Label lblProfissional,
            Label lblAgendamento,
            Paciente paciente,
            Agendamento agendamento
    ) {
        lblPaciente.setText("Paciente: " + (paciente != null ? paciente.getNome() : "-"));

        Usuario usuario = Session.getUsuario();
        lblProfissional.setText("Profissional: " + (usuario != null ? usuario.getPessoaNome() : "-"));

        lblAgendamento.setText("Agendamento ID: " + (agendamento != null ? agendamento.getId() : "-"));
    }

    public void atualizarVisibilidadeBotoes(
            String tipo,
            boolean inicialJaSalva,
            Button btnSalvarInicial,
            Button btnSalvarEvolucao,
            Button btnNovaEvolucao
    ) {
        boolean inicial = "ANAMNESE_INICIAL".equals(tipo);

        btnSalvarInicial.setVisible(inicial);
        btnSalvarInicial.setManaged(inicial);

        btnSalvarEvolucao.setVisible(!inicial);
        btnSalvarEvolucao.setManaged(!inicial);

        btnNovaEvolucao.setVisible(!inicial);
        btnNovaEvolucao.setManaged(!inicial);

        if (!inicial) {
            btnSalvarEvolucao.setDisable(!inicialJaSalva);
            btnNovaEvolucao.setDisable(!inicialJaSalva);
        }
    }
}
