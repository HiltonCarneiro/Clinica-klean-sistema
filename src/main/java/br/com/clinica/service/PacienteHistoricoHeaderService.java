package br.com.clinica.service;

import br.com.clinica.model.Paciente;
import br.com.clinica.util.ValidationUtils;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class PacienteHistoricoHeaderService {

    private final DateTimeFormatter fmtBr = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void preencherCabecalho(
            Paciente paciente,
            Label lblNome,
            Label lblCpf,
            Label lblRg,
            Label lblTelefone,
            Label lblDataNascimento,
            Label lblIdade,
            Label lblEndereco,
            Label lblAtivo
    ) {
        if (paciente == null) {
            return;
        }

        setText(lblNome, safe(paciente.getNome()));
        setText(lblCpf, ValidationUtils.formatCpf(safe(paciente.getCpf())));
        setText(lblRg, formatRg(safe(paciente.getRg())));
        setText(lblTelefone, ValidationUtils.formatPhoneBr(safe(paciente.getTelefone())));

        LocalDate dn = paciente.getDataNascimento();

        setText(lblDataNascimento, dn == null ? "" : dn.format(fmtBr));
        setText(lblIdade, calcularIdadeTexto(dn));
        setText(lblEndereco, montarEnderecoCompleto(paciente));
        setText(lblAtivo, paciente.isAtivo() ? "Sim" : "Não");
    }

    private String calcularIdadeTexto(LocalDate dn) {
        if (dn == null) {
            return "";
        }

        int idade = Period.between(dn, LocalDate.now()).getYears();

        if (idade < 0) {
            idade = 0;
        }

        return idade + " anos";
    }

    private String montarEnderecoCompleto(Paciente p) {
        if (p == null) {
            return "";
        }

        String rua = safe(p.getRua()).trim();
        String numero = safe(p.getNumero()).trim();
        String complemento = safe(p.getComplemento()).trim();
        String bairro = safe(p.getBairro()).trim();
        String cidade = safe(p.getCidade()).trim();
        String cep = safe(p.getCep()).trim();
        String uf = safe(p.getUf()).trim();

        StringBuilder sb = new StringBuilder();

        append(sb, rua, "");
        append(sb, numero, ", ");
        append(sb, complemento, " - ");
        append(sb, bairro, " - ");
        append(sb, cidade, " - ");

        if (!uf.isBlank()) {
            if (sb.length() > 0) {
                sb.append("/").append(uf);
            } else {
                sb.append(uf);
            }
        }

        if (!cep.isBlank()) {
            String cepFmt = ValidationUtils.formatCep(cep);

            if (!cepFmt.isBlank()) {
                if (sb.length() > 0) {
                    sb.append(" - CEP: ").append(cepFmt);
                } else {
                    sb.append("CEP: ").append(cepFmt);
                }
            }
        }

        if (sb.length() == 0) {
            return safe(p.getEndereco());
        }

        return sb.toString();
    }

    private void append(StringBuilder sb, String valor, String separador) {
        if (valor == null || valor.isBlank()) {
            return;
        }

        if (sb.length() > 0) {
            sb.append(separador);
        }

        sb.append(valor);
    }

    private String formatRg(String rg) {
        String digits = safe(rg).replaceAll("\\D", "");

        if (digits.isBlank()) {
            return "";
        }

        if (digits.length() > 7) {
            digits = digits.substring(0, 7);
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < digits.length(); i++) {
            if (i == 1 || i == 4) {
                sb.append('.');
            }

            sb.append(digits.charAt(i));
        }

        return sb.toString();
    }

    private void setText(Label label, String texto) {
        if (label != null) {
            label.setText(texto == null ? "" : texto);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}