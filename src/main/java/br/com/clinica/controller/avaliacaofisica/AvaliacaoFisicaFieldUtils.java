package br.com.clinica.controller.avaliacaofisica;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;

public final class AvaliacaoFisicaFieldUtils {

    private AvaliacaoFisicaFieldUtils() {
    }

    public static void limparCampo(TextInputControl campo) {
        if (campo != null) {
            campo.clear();
        }
    }

    public static void limparAreaTexto(TextArea area) {
        if (area != null) {
            area.clear();
        }
    }

    public static void limparCombo(ComboBox<?> comboBox) {
        if (comboBox != null) {
            comboBox.setValue(null);
        }
    }

    public static void setCampo(TextInputControl campo, String valor) {
        if (campo != null) {
            campo.setText(valor == null ? "" : valor);
        }
    }

    public static void setAreaTexto(TextArea area, String valor) {
        if (area != null) {
            area.setText(valor == null ? "" : valor);
        }
    }

    public static void setLabel(Label label, String valor) {
        if (label != null) {
            label.setText(valor == null || valor.isBlank() ? "-" : valor);
        }
    }

    public static String getTexto(TextInputControl campo) {
        return campo == null ? null : campo.getText();
    }

    public static String getTextoArea(TextArea area) {
        return area == null ? null : area.getText();
    }
}
