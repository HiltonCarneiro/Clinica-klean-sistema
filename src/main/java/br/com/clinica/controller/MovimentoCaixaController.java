package br.com.clinica.controller;

import br.com.clinica.dao.MovimentoCaixaDAO;
import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.enums.TipoMovimento;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class MovimentoCaixaController {

    @FXML
    private DatePicker dtInicio;

    @FXML
    private DatePicker dtFim;

    @FXML
    private Button btnBuscar;

    @FXML
    private TableView<MovimentoCaixa> tblMovimentos;

    @FXML
    private TableColumn<MovimentoCaixa, LocalDate> colData;

    @FXML
    private TableColumn<MovimentoCaixa, String> colDescricao;

    @FXML
    private TableColumn<MovimentoCaixa, String> colTipo;

    @FXML
    private TableColumn<MovimentoCaixa, Double> colValor;

    @FXML
    private TableColumn<MovimentoCaixa, String> colFormaPagamento;

    @FXML
    private TableColumn<MovimentoCaixa, String> colPaciente;

    @FXML
    private Label lblEntradas;

    @FXML
    private Label lblSaidas;

    @FXML
    private Label lblSaldo;

    @FXML
    private Button btnFechar;

    private final MovimentoCaixaDAO movimentoCaixaDAO = new MovimentoCaixaDAO();
    private final ObservableList<MovimentoCaixa> movimentos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabela();
        configurarDatasIniciais();
        carregarMovimentos();
    }

    private void configurarTabela() {
        tblMovimentos.setItems(movimentos);

        colData.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getData()));

        colDescricao.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDescricao()));

        colTipo.setCellValueFactory(cell -> {
            TipoMovimento tipo = cell.getValue().getTipo();
            String texto = (tipo == TipoMovimento.ENTRADA) ? "Entrada" : "Saída";
            return new SimpleStringProperty(texto);
        });

        colValor.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getValor()).asObject());

        colFormaPagamento.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFormaPagamento()));

        colPaciente.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPacienteNome()));
    }

    private void configurarDatasIniciais() {
        LocalDate hoje = LocalDate.now();
        dtInicio.setValue(hoje);
        dtFim.setValue(hoje);
    }

    private void carregarMovimentos() {
        LocalDate inicio = dtInicio.getValue();
        LocalDate fim = dtFim.getValue();

        if (inicio == null || fim == null) {
            mostrarErro("Período inválido", "Informe as datas inicial e final.");
            return;
        }

        if (fim.isBefore(inicio)) {
            mostrarErro("Período inválido", "A data final não pode ser menor que a data inicial.");
            return;
        }

        List<MovimentoCaixa> lista = movimentoCaixaDAO.listarPorPeriodo(inicio, fim);
        movimentos.setAll(lista);
        atualizarTotais();
    }

    private void atualizarTotais() {
        double entradas = 0.0;
        double saídas = 0.0;

        for (MovimentoCaixa mov : movimentos) {
            if (mov.getTipo() == TipoMovimento.ENTRADA) {
                entradas += mov.getValor();
            } else if (mov.getTipo() == TipoMovimento.SAIDA) {
                saídas += mov.getValor();
            }
        }

        double saldo = entradas - saídas;

        lblEntradas.setText(String.format("R$ %.2f", entradas));
        lblSaidas.setText(String.format("R$ %.2f", saídas));
        lblSaldo.setText(String.format("R$ %.2f", saldo));
    }

    @FXML
    private void onBuscar() {
        carregarMovimentos();
    }

    @FXML
    private void onFechar() {
        Stage stage = (Stage) lblSaldo.getScene().getWindow();
        stage.close();
    }

    // Utilitários

    private void mostrarErro(String titulo, String detalhe) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(titulo);
        alert.setContentText(detalhe);
        alert.showAndWait();
    }
}
