package br.com.clinica.mapper;

import br.com.clinica.dto.PacienteFormData;
import br.com.clinica.model.Paciente;

public class PacienteFormMapper {

    public Paciente toEntity(Paciente paciente, PacienteFormData data) {
        paciente.setNome(data.getNome());
        paciente.setCpf(data.getCpf());
        paciente.setRg(data.getRg());
        paciente.setDataNascimento(data.getDataNascimento());
        paciente.setTelefone(data.getTelefone());

        paciente.setRua(data.getRua());
        paciente.setNumero(data.getNumero());
        paciente.setComplemento(data.getComplemento());
        paciente.setBairro(data.getBairro());
        paciente.setCidade(data.getCidade());
        paciente.setCep(data.getCep());
        paciente.setUf(data.getUf());

        paciente.setResponsavelLegal(data.getResponsavelLegal());

        return paciente;
    }

    public PacienteFormData toFormData(Paciente paciente) {
        PacienteFormData data = new PacienteFormData();

        data.setNome(paciente.getNome());
        data.setCpf(paciente.getCpf());
        data.setRg(paciente.getRg());
        data.setDataNascimento(paciente.getDataNascimento());
        data.setTelefone(paciente.getTelefone());

        data.setRua(paciente.getRua());
        data.setNumero(paciente.getNumero());
        data.setComplemento(paciente.getComplemento());
        data.setBairro(paciente.getBairro());
        data.setCidade(paciente.getCidade());
        data.setCep(paciente.getCep());
        data.setUf(paciente.getUf());

        data.setResponsavelLegal(paciente.getResponsavelLegal());

        return data;
    }
}