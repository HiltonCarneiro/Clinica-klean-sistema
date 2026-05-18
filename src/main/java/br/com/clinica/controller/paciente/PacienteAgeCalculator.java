package br.com.clinica.controller.paciente;

import java.time.LocalDate;
import java.time.Period;

public class PacienteAgeCalculator {

    public String calcularTexto(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            return "";
        }

        int idade = Period.between(dataNascimento, LocalDate.now()).getYears();

        return String.valueOf(Math.max(idade, 0));
    }
}