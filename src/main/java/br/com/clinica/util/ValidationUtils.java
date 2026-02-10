package br.com.clinica.util;

public final class ValidationUtils {

    private ValidationUtils() {}

    public static String onlyDigits(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    /** Aceita letras (com acento), espaço, hífen e apóstrofo */
    public static boolean isValidName(String nome) {
        if (nome == null) return false;
        String n = nome.trim();
        if (n.isEmpty()) return false;
        if (n.length() < 3) return false;

        return n.matches("^[A-Za-zÀ-ÿ'\\- ]+$");
    }

    public static boolean isValidCpf(String cpf) {
        String d = onlyDigits(cpf);
        if (d.length() != 11) return false;

        if (d.matches("(\\d)\\1{10}")) return false;

        int dig1 = calcCpfDigit(d.substring(0, 9), 10);
        int dig2 = calcCpfDigit(d.substring(0, 10), 11);

        return dig1 == (d.charAt(9) - '0') && dig2 == (d.charAt(10) - '0');
    }

    private static int calcCpfDigit(String base, int pesoInicial) {
        int soma = 0;
        int peso = pesoInicial;
        for (int i = 0; i < base.length(); i++) {
            soma += (base.charAt(i) - '0') * peso--;
        }
        int mod = soma % 11;
        int dig = 11 - mod;
        return (dig >= 10) ? 0 : dig;
    }

    public static boolean isValidCep(String cep) {
        String d = onlyDigits(cep);
        return d.length() == 8;
    }

    /** Aceita 10 (fixo) ou 11 (celular) dígitos */
    public static boolean isValidPhoneBr(String telefone) {
        String d = onlyDigits(telefone);
        return d.length() == 10 || d.length() == 11;
    }

    public static String formatCpf(String cpf) {
        String d = onlyDigits(cpf);
        if (d.length() > 11) d = d.substring(0, 11);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            sb.append(d.charAt(i));
            if (i == 2 || i == 5) sb.append('.');
            if (i == 8) sb.append('-');
        }
        return sb.toString();
    }

    public static String formatCep(String cep) {
        String d = onlyDigits(cep);
        if (d.length() > 8) d = d.substring(0, 8);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            sb.append(d.charAt(i));
            if (i == 4) sb.append('-');
        }
        return sb.toString();
    }

    public static String formatPhoneBr(String telefone) {
        String d = onlyDigits(telefone);
        if (d.length() > 11) d = d.substring(0, 11);

        if (d.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();

        // (DD) ...
        sb.append('(');
        for (int i = 0; i < d.length() && i < 2; i++) sb.append(d.charAt(i));
        if (d.length() >= 2) sb.append(") ");
        else return sb.toString();

        if (d.length() <= 10) {
            // (DD) ####-####
            for (int i = 2; i < d.length(); i++) {
                sb.append(d.charAt(i));
                if (i == 5) sb.append('-');
            }
        } else {
            // (DD) #####-####
            for (int i = 2; i < d.length(); i++) {
                sb.append(d.charAt(i));
                if (i == 6) sb.append('-');
            }
        }

        return sb.toString();
    }
}