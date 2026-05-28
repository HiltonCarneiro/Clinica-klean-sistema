package br.com.clinica.controller.avaliacaofisica;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.Arrays;
import java.util.List;

public class AvaliacaoFisicaMaskManager {

    public void configurarMascarasNumericas(List<TextField> camposDecimais, TextField txtIdade) {
        if (camposDecimais != null) {
            camposDecimais.forEach(this::configurarCampoDecimalAutomatico);
        }

        configurarCampoInteiro(txtIdade);
    }

    public void configurarMascarasNumericas(TextField txtIdade, TextField... camposDecimais) {
        configurarMascarasNumericas(Arrays.asList(camposDecimais), txtIdade);
    }

    public void configurarCampoDecimalAutomatico(TextField campo) {
        if (campo == null) {
            return;
        }

        campo.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isBlank()) {
                return;
            }

            String numeros = newValue.replaceAll("[^\\d]", "");

            if (numeros.isEmpty()) {
                campo.setText("");
                return;
            }

            try {
                double valor = Double.parseDouble(numeros) / 100.0;
                String formatado = String.format("%.2f", valor).replace(".", ",");

                if (!formatado.equals(newValue)) {
                    campo.setText(formatado);
                    campo.positionCaret(formatado.length());
                }
            } catch (Exception ignored) {
            }
        });
    }

    public void configurarCampoInteiro(TextField campo) {
        if (campo == null) {
            return;
        }

        campo.setTextFormatter(new TextFormatter<>(change -> {
            String texto = change.getControlNewText();
            return texto.matches("\\d{0,3}") ? change : null;
        }));
    }
}
