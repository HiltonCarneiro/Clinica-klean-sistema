package br.com.clinica.controller.avaliacaofisica;

import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.List;

public class AvaliacaoFisicaComboManager {

    private final PacienteDAO pacienteDAO;
    private final UsuarioDAO usuarioDAO;

    public AvaliacaoFisicaComboManager(PacienteDAO pacienteDAO, UsuarioDAO usuarioDAO) {
        this.pacienteDAO = pacienteDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public void configurarCombos(ComboBox<String> cbSexo, ComboBox<String> cbNivelAtividade) {
        if (cbSexo != null) {
            cbSexo.setItems(FXCollections.observableArrayList("Masculino", "Feminino"));
        }

        if (cbNivelAtividade != null) {
            cbNivelAtividade.setItems(FXCollections.observableArrayList(
                    "Sedentário",
                    "Levemente ativo",
                    "Moderadamente ativo",
                    "Muito ativo",
                    "Atleta"
            ));
        }
    }

    public void carregarPacientes(ComboBox<Paciente> cbPacientes) {
        if (cbPacientes == null) {
            return;
        }

        List<Paciente> pacientes = pacienteDAO.listarAtivos();
        cbPacientes.setItems(FXCollections.observableArrayList(pacientes));
    }

    public void carregarProfissionais(ComboBox<Usuario> cbProfissionais) {
        if (cbProfissionais == null) {
            return;
        }

        List<Usuario> profissionais = usuarioDAO.listarProfissionaisAtivos();
        cbProfissionais.setItems(FXCollections.observableArrayList(profissionais));
    }

    public void preencherIdadePorPaciente(Paciente paciente, TextField txtIdade) {
        if (paciente != null && paciente.getDataNascimento() != null && txtIdade != null) {
            txtIdade.setText(String.valueOf(paciente.getIdade()));
        }
    }

    public void selecionarPacientePorId(ComboBox<Paciente> cbPacientes, Long pacienteId) {
        if (pacienteId == null || cbPacientes == null || cbPacientes.getItems() == null) {
            return;
        }

        for (Paciente paciente : cbPacientes.getItems()) {
            if (pacienteId.equals(paciente.getId())) {
                cbPacientes.setValue(paciente);
                return;
            }
        }
    }
}
