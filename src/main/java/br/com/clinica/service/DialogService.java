package br.com.clinica.service;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DialogService {

    public void erro(String titulo, String mensagem) {
        exibirMensagem(titulo, mensagem, "✕", "dialog-icon-error");
    }

    public void sucesso(String titulo, String mensagem) {
        exibirMensagem(titulo, mensagem, "✓", "dialog-icon-success");
    }

    public void aviso(String titulo, String mensagem) {
        exibirMensagem(titulo, mensagem, "!", "dialog-icon-warning");
    }

    public void informacao(String titulo, String mensagem) {
        exibirMensagem(titulo, mensagem, "i", "dialog-icon-info");
    }

    public boolean confirmar(String titulo, String cabecalho, String mensagem) {
        Dialog<ButtonType> dialog = criarDialogBase(titulo);

        Label icon = new Label("?");
        icon.getStyleClass().addAll("dialog-icon", "dialog-icon-confirm");

        Label title = new Label(cabecalho == null || cabecalho.isBlank() ? titulo : cabecalho);
        title.getStyleClass().add("dialog-title");

        Label text = new Label(normalizarMensagem(mensagem));
        text.getStyleClass().add("dialog-message");
        text.setWrapText(true);

        VBox texts = new VBox(6, title, text);
        texts.setAlignment(Pos.CENTER_LEFT);

        HBox content = new HBox(16, icon, texts);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(content);

        ButtonType confirmar = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().setAll(cancelar, confirmar);

        aplicarCss(dialog);

        return dialog.showAndWait().orElse(cancelar) == confirmar;
    }

    private void exibirMensagem(String titulo, String mensagem, String simbolo, String iconClass) {
        Dialog<ButtonType> dialog = criarDialogBase(titulo);

        Label icon = new Label(simbolo);
        icon.getStyleClass().addAll("dialog-icon", iconClass);

        Label title = new Label(titulo == null || titulo.isBlank() ? "Mensagem do sistema" : titulo);
        title.getStyleClass().add("dialog-title");

        Label text = new Label(normalizarMensagem(mensagem));
        text.getStyleClass().add("dialog-message");
        text.setWrapText(true);

        VBox texts = new VBox(6, title, text);
        texts.setAlignment(Pos.CENTER_LEFT);

        HBox content = new HBox(16, icon, texts);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);

        aplicarCss(dialog);

        dialog.showAndWait();
    }

    private Dialog<ButtonType> criarDialogBase(String titulo) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(titulo == null || titulo.isBlank() ? "Mensagem do sistema" : titulo);
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        return dialog;
    }

    private void aplicarCss(Dialog<ButtonType> dialog) {
        String css = getClass().getResource("/styles/app.css") == null
                ? null
                : getClass().getResource("/styles/app.css").toExternalForm();

        if (css != null) {
            dialog.getDialogPane().getStylesheets().add(css);
        }

        for (ButtonType buttonType : dialog.getDialogPane().getButtonTypes()) {
            Node button = dialog.getDialogPane().lookupButton(buttonType);

            if (button != null) {
                button.getStyleClass().add(
                        buttonType.getButtonData().isCancelButton()
                                ? "dialog-button-secondary"
                                : "dialog-button-primary"
                );
            }
        }
    }

    private String normalizarMensagem(String mensagem) {
        return mensagem == null || mensagem.isBlank()
                ? "Ocorreu um erro inesperado."
                : mensagem;
    }
}