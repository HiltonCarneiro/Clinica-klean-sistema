package br.com.clinica.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private Label lblUsuarioLogado;

    private String usuarioLogado;

    @FXML
    private void initialize() {
        // Se quiser, pode colocar algo inicial aqui depois
    }

    /**
     * Chamado pelo LoginController depois de autenticar
     */
    public void setUsuarioLogado(String usuario) {
        this.usuarioLogado = usuario;
        if (lblUsuarioLogado != null) {
            lblUsuarioLogado.setText(usuario);
        }
    }

    // ====== AÇÕES DO MENU E DOS BOTÕES DA TELA INICIAL ======

    @FXML
    private void onPacientes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/paciente-view.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);

            Stage stage = new Stage();
            stage.setTitle("Pacientes - Clínica Integração");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de pacientes", e.getMessage());
        }
    }

    @FXML
    private void onEstoque() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/estoque-view.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);

            Stage stage = new Stage();
            stage.setTitle("Estoque - Clínica Integração");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de estoque", e.getMessage());
        }
    }

    @FXML
    private void onAgenda() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/agenda-view.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 600);

            Stage stage = new Stage();
            stage.setTitle("Agenda - Clínica Integração");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de agenda", e.getMessage());
        }
    }

    @FXML
    private void onCaixa() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/caixa-view.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 600);

            Stage stage = new Stage();
            stage.setTitle("Caixa / Notas - Clínica Integração");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de caixa / notas", e.getMessage());
        }
    }

    @FXML
    private void onRelatorios() {
        // Ainda não implementamos relatórios.
        mostrarAviso("Relatórios", "Tela de Relatórios ainda não implementada.\nNo futuro: atendimentos, faturamento, estoque etc.");
    }

    @FXML
    private void onUsuarios() {
        // No futuro vamos fazer tela de administração de usuários/perfis
        mostrarAviso("Usuários & Perfis", "Tela de administração de usuários ainda não implementada.");
    }

    @FXML
    private void onSair() {
        // Fecha a aplicação inteira
        Stage stage = (Stage) lblUsuarioLogado.getScene().getWindow();
        stage.close();
    }

    // ====== MÉTODOS DE APOIO ======

    private void mostrarErro(String titulo, String detalhe) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(titulo);
        alert.setContentText(detalhe);
        alert.showAndWait();
    }

    private void mostrarAviso(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}