package br.com.clinica.controller;

import br.com.clinica.controller.caixa.CaixaAlertManager;
import br.com.clinica.controller.caixa.MovimentoCaixaSummaryManager;
import br.com.clinica.controller.caixa.MovimentoCaixaTableManager;
import br.com.clinica.dao.MovimentoCaixaDAO;
import br.com.clinica.model.MovimentoCaixa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class MovimentoCaixaController {

    @FXML private DatePicker dtInicio;
    @FXML private DatePicker dtFim;

    @FXML private Button btnBuscar;

    @FXML private TableView<MovimentoCaixa> tblMovimentos;
    @FXML private TableColumn<MovimentoCaixa, LocalDate> colData;
    @FXML private TableColumn<MovimentoCaixa, String> colDescricao;
    @FXML private TableColumn<MovimentoCaixa, String> colTipo;
    @FXML private TableColumn<MovimentoCaixa, Double> colValor;
    @FXML private TableColumn<MovimentoCaixa, String> colFormaPagamento;
    @FXML private TableColumn<MovimentoCaixa, String> colPaciente;

    @FXML private Label lblEntradas;
    @FXML private Label lblSaidas;
    @FXML private Label lblSaldo;

    @FXML private Button btnFechar;

    private final MovimentoCaixaDAO movimentoCaixaDAO = new MovimentoCaixaDAO();

    private final CaixaAlertManager alertManager = new CaixaAlertManager();
    private final MovimentoCaixaTableManager tableManager = new MovimentoCaixaTableManager();
    private final MovimentoCaixaSummaryManager summaryManager = new MovimentoCaixaSummaryManager();

    private final ObservableList<MovimentoCaixa> movimentos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabela();
        configurarDatasIniciais();
        carregarMovimentos();
    }

    private void configurarTabela() {
        tableManager.configurarTabela(
                tblMovimentos,
                colData,
                colDescricao,
                colTipo,
                colValor,
                colFormaPagamento,
                colPaciente,
                movimentos
        );
    }

    private void configurarDatasIniciais() {
        LocalDate hoje = LocalDate.now();

        dtInicio.setValue(hoje);
        dtFim.setValue(hoje);
    }

    private void carregarMovimentos() {
        LocalDate inicio = dtInicio.getValue();
        LocalDate fim = dtFim.getValue();

        if (!periodoValido(inicio, fim)) {
            return;
        }

        List<MovimentoCaixa> lista = movimentoCaixaDAO.listarPorPeriodo(inicio, fim);

        movimentos.setAll(lista);
        atualizarTotais();
    }

    private boolean periodoValido(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            mostrarErro("Período inválido", "Informe as datas inicial e final.");
            return false;
        }

        if (fim.isBefore(inicio)) {
            mostrarErro("Período inválido", "A data final não pode ser menor que a data inicial.");
            return false;
        }

        return true;
    }

    private void atualizarTotais() {
        summaryManager.atualizarTotais(
                movimentos,
                lblEntradas,
                lblSaidas,
                lblSaldo
        );
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

    private void mostrarErro(String titulo, String detalhe) {
        alertManager.mostrarErro(titulo, detalhe);
    }
}