package br.com.clinica.dto;

import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.model.enums.SalaAtendimento;

import java.time.LocalDate;
import java.time.LocalTime;

public class AgendaFormData {

    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private Usuario profissional;
    private Paciente paciente;
    private SalaAtendimento sala;
    private String procedimento;
    private String observacoes;

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }

    public Usuario getProfissional() {
        return profissional;
    }

    public void setProfissional(Usuario profissional) {
        this.profissional = profissional;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public SalaAtendimento getSala() {
        return sala;
    }

    public void setSala(SalaAtendimento sala) {
        this.sala = sala;
    }

    public String getProcedimento() {
        return procedimento;
    }

    public void setProcedimento(String procedimento) {
        this.procedimento = procedimento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}