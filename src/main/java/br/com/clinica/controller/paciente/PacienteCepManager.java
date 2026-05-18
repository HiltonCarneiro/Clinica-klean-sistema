package br.com.clinica.controller.paciente;

import br.com.clinica.dto.CepAddress;
import br.com.clinica.exception.BusinessException;
import br.com.clinica.service.CepService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

public class PacienteCepManager {

    private final TextField txtCep;
    private final TextField txtRua;
    private final TextField txtBairro;
    private final TextField txtCidade;
    private final TextField txtUf;
    private final Consumer<String> mensagemConsumer;

    private final CepService cepService = new CepService();

    public PacienteCepManager(
            TextField txtCep,
            TextField txtRua,
            TextField txtBairro,
            TextField txtCidade,
            TextField txtUf,
            Consumer<String> mensagemConsumer
    ) {
        this.txtCep = txtCep;
        this.txtRua = txtRua;
        this.txtBairro = txtBairro;
        this.txtCidade = txtCidade;
        this.txtUf = txtUf;
        this.mensagemConsumer = mensagemConsumer;
    }

    public void configurarBuscaPorEnter() {
        if (txtCep == null) {
            return;
        }

        txtCep.setOnAction(event -> buscarEPreencher());
    }

    public void buscarEPreencher() {
        String cep = txtCep == null ? "" : txtCep.getText();

        Task<CepAddress> task = new Task<>() {
            @Override
            protected CepAddress call() {
                return cepService.buscarPorCep(cep);
            }
        };

        task.setOnRunning(event -> mensagemConsumer.accept("Consultando CEP..."));

        task.setOnSucceeded(event -> {
            CepAddress address = task.getValue();
            preencherEndereco(address);
            mensagemConsumer.accept("Endereço preenchido pelo CEP.");
        });

        task.setOnFailed(event -> {
            Throwable erro = task.getException();

            if (erro instanceof BusinessException) {
                mensagemConsumer.accept(erro.getMessage());
            } else {
                erro.printStackTrace();
                mensagemConsumer.accept("Erro ao consultar CEP.");
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void preencherEndereco(CepAddress address) {
        if (address == null) {
            return;
        }

        Platform.runLater(() -> {
            setText(txtCep, address.getCep());
            setText(txtRua, address.getRua());
            setText(txtBairro, address.getBairro());
            setText(txtCidade, address.getCidade());
            setText(txtUf, address.getUf());
        });
    }

    private void setText(TextField field, String value) {
        if (field != null && value != null && !value.isBlank()) {
            field.setText(value);
        }
    }
}