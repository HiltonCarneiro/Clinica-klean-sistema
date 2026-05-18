package br.com.clinica.mapper;

import br.com.clinica.dto.AgendaFormData;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.model.enums.SalaAtendimento;
import br.com.clinica.util.AgendaTimeParser;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AgendaFormMapper {

    private static final DateTimeFormatter HORA_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("pt-BR"));

    public AgendaFormData fromForm(
            DatePicker dpData,
            TextField txtHoraInicio,
            TextField txtHoraFim,
            ComboBox<Usuario> cbProfissional,
            ComboBox<Paciente> cbPaciente,
            ComboBox<SalaAtendimento> cbSala,
            TextField txtProcedimento,
            TextArea txtObservacoes,
            Usuario profissionalSelecionado,
            Paciente pacienteSelecionado
    ) {
        AgendaFormData data = new AgendaFormData();

        data.setData(dpData.getValue());
        data.setHoraInicio(AgendaTimeParser.parse(txtHoraInicio.getText()));
        data.setHoraFim(AgendaTimeParser.parse(txtHoraFim.getText()));
        data.setProfissional(profissionalSelecionado);
        data.setPaciente(pacienteSelecionado);
        data.setSala(cbSala.getValue());
        data.setProcedimento(txtProcedimento.getText());
        data.setObservacoes(txtObservacoes.getText());

        return data;
    }

    public void fillForm(
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
        dpData.setValue(agendamento.getData());

        txtHoraInicio.setText(
                agendamento.getHoraInicio() == null
                        ? ""
                        : agendamento.getHoraInicio().format(HORA_FORMATTER)
        );

        txtHoraFim.setText(
                agendamento.getHoraFim() == null
                        ? ""
                        : agendamento.getHoraFim().format(HORA_FORMATTER)
        );

        txtProcedimento.setText(
                agendamento.getProcedimento() == null
                        ? ""
                        : agendamento.getProcedimento()
        );

        txtObservacoes.setText(
                agendamento.getObservacoes() == null
                        ? ""
                        : agendamento.getObservacoes()
        );

        if (agendamento.getSala() != null) {
            cbSala.setValue(agendamento.getSala());
        }

        if (agendamento.getPacienteId() != null) {
            cbPaciente.getItems()
                    .stream()
                    .filter(p -> p.getId() != null
                            && p.getId().intValue() == agendamento.getPacienteId())
                    .findFirst()
                    .ifPresent(cbPaciente::setValue);
        }

        if (agendamento.getProfissionalId() != null) {
            cbProfissional.getItems()
                    .stream()
                    .filter(u -> u.getId() != null
                            && u.getId().intValue() == agendamento.getProfissionalId())
                    .findFirst()
                    .ifPresent(cbProfissional::setValue);
        }
    }

    public void clearForm(
            TextField txtHoraInicio,
            TextField txtHoraFim,
            TextField txtProcedimento,
            TextArea txtObservacoes,
            ComboBox<Paciente> cbPaciente,
            ComboBox<SalaAtendimento> cbSala
    ) {
        txtHoraInicio.clear();
        txtHoraFim.clear();
        txtProcedimento.clear();
        txtObservacoes.clear();
        cbPaciente.getSelectionModel().clearSelection();
        cbPaciente.setValue(null);
        cbSala.getSelectionModel().clearSelection();
        cbSala.setValue(null);
    }
}