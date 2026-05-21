package br.com.clinica.util;

import javafx.scene.control.Alert;

public final class AlertUtils {

    private AlertUtils() {
    }

    public static void aviso(String mensagem) {
        mostrar(Alert.AlertType.WARNING, mensagem);
    }

    public static void erro(String mensagem) {
        mostrar(Alert.AlertType.ERROR, mensagem);
    }

    public static void info(String mensagem) {
        mostrar(Alert.AlertType.INFORMATION, mensagem);
    }

    private static void mostrar(Alert.AlertType tipo, String mensagem) {
        new Alert(tipo, mensagem).showAndWait();
    }
}