package br.com.clinica.controller.agenda;

import br.com.clinica.mapper.AgendaFormMapper;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.model.enums.SalaAtendimento;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AgendaFormManager {

    public void preencherFormulario(
            AgendaFormMapper formMapper,
            Agendamento agendamento,
            DatePicker dpData,
            TextField txtHoraInicio,
            TextField txtHoraFim,
            ComboBox<Usuario> cbProfissional,
            ComboBox<Paciente> cbPaciente,
            ComboBox<SalaAtendimento> cbSala,
            TextField txtProcedimento,
            TextArea txtObservacoes
    ) {
        if (agendamento == null) {
            return;
        }

        formMapper.fillForm(
                agendamento,
                dpData,
                txtHoraInicio,
                txtHoraFim,
                cbProfissional,
                cbPaciente,
                cbSala,
                txtProcedimento,
                txtObservacoes
        );
    }

    public void limparFormulario(
            AgendaFormMapper formMapper,
            TextField txtHoraInicio,
            TextField txtHoraFim,
            TextField txtProcedimento,
            TextArea txtObservacoes,
            ComboBox<Paciente> cbPaciente,
            ComboBox<SalaAtendimento> cbSala
    ) {
        formMapper.clearForm(
                txtHoraInicio,
                txtHoraFim,
                txtProcedimento,
                txtObservacoes,
                cbPaciente,
                cbSala
        );
    }

    public void limparFormularioCompleto(
            AgendaFormMapper formMapper,
            DatePicker dpData,
            TextField txtHoraInicio,
            TextField txtHoraFim,
            TextField txtProcedimento,
            TextArea txtObservacoes,
            ComboBox<Paciente> cbPaciente,
            ComboBox<SalaAtendimento> cbSala,
            TableView<Agendamento> tbAgenda,
            AgendaTableManager tableManager
    ) {
        limparFormulario(
                formMapper,
                txtHoraInicio,
                txtHoraFim,
                txtProcedimento,
                txtObservacoes,
                cbPaciente,
                cbSala
        );

        dpData.setValue(null);

        if (dpData.getEditor() != null) {
            dpData.getEditor().clear();
        }

        tableManager.clear(tbAgenda);
    }

    public Usuario obterProfissionalSelecionado(
            ComboBox<Usuario> cbProfissional,
            AgendaComboBoxConfigurer<Usuario> profissionalComboConfigurer,
            AgendaDisplayFormatter displayFormatter
    ) {
        return profissionalComboConfigurer.resolveSelectedItem(
                cbProfissional,
                displayFormatter::profissional
        );
    }

    public Paciente obterPacienteSelecionado(
            ComboBox<Paciente> cbPaciente,
            AgendaComboBoxConfigurer<Paciente> pacienteComboConfigurer
    ) {
        return pacienteComboConfigurer.resolveSelectedItem(
                cbPaciente,
                paciente -> paciente == null || paciente.getNome() == null
                        ? ""
                        : paciente.getNome()
        );
    }
}