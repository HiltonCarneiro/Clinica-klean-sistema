package br.com.clinica.controller.paciente;

public class PacienteDocumentFormatter {

    public String formatCpf(String cpf) {

        cpf = digits(cpf);

        if (cpf.length() != 11) {
            return cpf;
        }

        return cpf.replaceFirst(
                "(\\d{3})(\\d{3})(\\d{3})(\\d{2})",
                "$1.$2.$3-$4"
        );
    }

    public String formatCep(String cep) {

        cep = digits(cep);

        if (cep.length() != 8) {
            return cep;
        }

        return cep.replaceFirst(
                "(\\d{5})(\\d{3})",
                "$1-$2"
        );
    }

    public String formatTelefone(String telefone) {

        telefone = digits(telefone);

        if (telefone.length() == 11) {

            return telefone.replaceFirst(
                    "(\\d{2})(\\d{5})(\\d{4})",
                    "($1) $2-$3"
            );
        }

        return telefone;
    }

    private String digits(String valor) {

        if (valor == null) {
            return "";
        }

        return valor.replaceAll("\\D", "");
    }
}