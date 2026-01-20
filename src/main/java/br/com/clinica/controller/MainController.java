package br.com.clinica.controller;

import br.com.clinica.model.Usuario;
import br.com.clinica.session.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class MainController {

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblPerfil;

    @FXML
    private void initialize() {
        Usuario usuario = Session.getUsuario();

        if (usuario != null) {
            String textoUsuario = usuario.getNome() + " (" + usuario.getLogin() + ")";
            lblUsuario.setText("Usuário: " + textoUsuario);

            String nomePerfil = (usuario.getPerfil() != null)
                    ? usuario.getPerfil().getNome()
                    : "SEM PERFIL";

            lblPerfil.setText("Perfil: " + nomePerfil);
        } else {
            lblUsuario.setText("Usuário: (não definido)");
            lblPerfil.setText("Perfil: (não definido)");
        }
    }

    // ====== Menu Arquivo ======
    @FXML
    private void onSair() {
        Session.limpar();
        Stage stage = (Stage) lblUsuario.getScene().getWindow();
        stage.close();
    }

    // ====== Menu Cadastros ======
    @FXML
    private void onPacientes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/paciente-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            Stage stage = new Stage();
            stage.setTitle("Pacientes - Clínica Integração");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao abrir tela de pacientes");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void onUsuarios() {
        info("Usuários", "Aqui vamos abrir a tela de gerenciamento de usuários.");
    }

    // ====== Menu Agenda ======
    @FXML
    private void onAgendamentos() {
        info("Agenda", "Aqui vamos abrir a tela de agendamentos.");
    }

    // ====== Menu Estoque ======
    @FXML
    private void onEstoque() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/estoque-view.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);

            Stage stage = new Stage();
            stage.setTitle("Estoque - Clínica Integração");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace(); // deixa isso aqui pra ver o erro no console
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao abrir tela de estoque");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    // ====== Menu Financeiro ======
    @FXML
    private void onFinanceiro() {
        info("Financeiro", "Aqui vamos abrir a tela de caixa / notas.");
    }

    // ====== Utilitário ======
    private void info(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
