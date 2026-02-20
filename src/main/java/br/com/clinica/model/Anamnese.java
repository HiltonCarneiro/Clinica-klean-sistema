package br.com.clinica.model;

public class Anamnese {

    private Integer id;

    // paciente.id
    private Long pacienteId;

    // agendamento.id é Integer (pode ser nulo)
    private Integer agendamentoId;

    // usuario.id é Integer
    private Integer profissionalId;

    // "yyyy-MM-dd HH:mm:ss"
    private String dataHora;

    // "ANAMNESE_INICIAL" ou "EVOLUCAO"
    private String tipo;

    private String dadosJson;
    private String observacoes;

    public Anamnese() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public Integer getAgendamentoId() { return agendamentoId; }
    public void setAgendamentoId(Integer agendamentoId) { this.agendamentoId = agendamentoId; }

    public Integer getProfissionalId() { return profissionalId; }
    public void setProfissionalId(Integer profissionalId) { this.profissionalId = profissionalId; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDadosJson() { return dadosJson; }
    public void setDadosJson(String dadosJson) { this.dadosJson = dadosJson; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}