package br.com.clinica.controller.caixa;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CaixaMoneyFieldManager {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DecimalFormatSymbols DFS_BR = new DecimalFormatSymbols(LOCALE_PT_BR);
    private static final DecimalFormat DF_MOEDA = new DecimalFormat("#,##0.00", DFS_BR);

    public void configurarMascarasCamposNumericos(
            TextField txtQuantidade,
            TextField txtValorUnitario
    ) {
        configurarMascaraQuantidade(txtQuantidade);
        configurarMascaraValorUnitario(txtValorUnitario);
    }

    public void setValorUnitarioMonetario(
            TextField txtValorUnitario,
            Double valor
    ) {
        if (txtValorUnitario == null) {
            return;
        }

        if (valor == null) {
            txtValorUnitario.clear();
            return;
        }

        txtValorUnitario.setText(DF_MOEDA.format(valor));
    }

    public double parseMoedaBR(String texto) {
        if (texto == null) {
            throw new NumberFormatException("Valor vazio.");
        }

        String valor = texto.trim();

        if (valor.isEmpty()) {
            throw new NumberFormatException("Valor vazio.");
        }

        valor = valor.replaceAll("[^0-9,\\.]", "");

        if (valor.isEmpty()) {
            throw new NumberFormatException("Valor vazio.");
        }

        valor = valor.replace(".", "");
        valor = valor.replace(",", ".");

        return Double.parseDouble(valor);
    }

    private void configurarMascaraQuantidade(TextField txtQuantidade) {
        if (txtQuantidade == null) {
            return;
        }

        txtQuantidade.setTextFormatter(
                new TextFormatter<String>(change -> {
                    if (!change.isContentChange()) {
                        return change;
                    }

                    String novo = change.getControlNewText();

                    if (novo == null || novo.isEmpty()) {
                        return change;
                    }

                    if (novo.matches("\\d+")) {
                        return change;
                    }

                    return null;
                })
        );
    }

    private void configurarMascaraValorUnitario(TextField txtValorUnitario) {
        if (txtValorUnitario == null) {
            return;
        }

        txtValorUnitario.setTextFormatter(
                new TextFormatter<String>(change -> {
                    if (!change.isContentChange()) {
                        return change;
                    }

                    String novoTexto = change.getControlNewText();

                    if (novoTexto == null) {
                        return change;
                    }

                    String digits = novoTexto.replaceAll("\\D", "");

                    if (digits.isEmpty()) {
                        change.setText("");
                        change.setRange(
                                0,
                                change.getControlText().length()
                        );
                        change.selectRange(0, 0);
                        return change;
                    }

                    if (digits.length() > 15) {
                        digits = digits.substring(0, 15);
                    }

                    long cents = Long.parseLong(digits);
                    double valor = cents / 100.0;

                    String formatted = DF_MOEDA.format(valor);

                    int oldLen =
                            change.getControlText() == null
                                    ? 0
                                    : change.getControlText().length();

                    change.setText(formatted);
                    change.setRange(0, oldLen);
                    change.selectRange(
                            formatted.length(),
                            formatted.length()
                    );

                    return change;
                })
        );
    }
}