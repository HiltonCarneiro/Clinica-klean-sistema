package br.com.clinica.controller.paciente;

import br.com.clinica.exception.BusinessException;
import br.com.clinica.model.Paciente;
import br.com.clinica.service.PacienteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacienteSearchManager {

    private final TextField txtBusca;
    private final CheckBox chkMostrarInativosBusca;
    private final TableView<Paciente> tableBusca;
    private final TableColumn<Paciente, String> colBuscaNome;
    private final TableColumn<Paciente, String> colBuscaCpf;
    private final TableColumn<Paciente, String> colBuscaTelefone;
    private final TableColumn<Paciente, String> colBuscaDataNascimento;
    private final TableColumn<Paciente, String> colBuscaAtivo;
    private final Button btnEditarSelecionado;
    private final Button btnHistoricoSelecionado;
    private final PacienteService pacienteService;
    private final PacienteDocumentFormatter formatter;
    private final Function<String, String> rgFormatter;
    private final Consumer<Paciente> selectionConsumer;

    private final ObservableList<Paciente> pacientes = FXCollections.observableArrayList();

    public PacienteSearchManager(
            TextField txtBusca,
            CheckBox chkMostrarInativosBusca,
            TableView<Paciente> tableBusca,
            TableColumn<Paciente, String> colBuscaNome,
            TableColumn<Paciente, String> colBuscaCpf,
            TableColumn<Paciente, String> colBuscaTelefone,
            TableColumn<Paciente, String> colBuscaDataNascimento,
            TableColumn<Paciente, String> colBuscaAtivo,
            Button btnEditarSelecionado,
            Button btnHistoricoSelecionado,
            PacienteService pacienteService,
            PacienteDocumentFormatter formatter,
            Function<String, String> rgFormatter,
            Consumer<Paciente> selectionConsumer
    ) {
        this.txtBusca = txtBusca;
        this.chkMostrarInativosBusca = chkMostrarInativosBusca;
        this.tableBusca = tableBusca;
        this.colBuscaNome = colBuscaNome;
        this.colBuscaCpf = colBuscaCpf;
        this.colBuscaTelefone = colBuscaTelefone;
        this.colBuscaDataNascimento = colBuscaDataNascimento;
        this.colBuscaAtivo = colBuscaAtivo;
        this.btnEditarSelecionado = btnEditarSelecionado;
        this.btnHistoricoSelecionado = btnHistoricoSelecionado;
        this.pacienteService = pacienteService;
        this.formatter = formatter;
        this.rgFormatter = rgFormatter;
        this.selectionConsumer = selectionConsumer;
    }

    public void configurarTabela() {
        if (tableBusca == null) {
            return;
        }

        colBuscaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        colBuscaCpf.setCellValueFactory(cell ->
                new SimpleStringProperty(formatDocumento(cell.getValue()))
        );

        colBuscaTelefone.setCellValueFactory(cell ->
                new SimpleStringProperty(formatter.formatTelefone(cell.getValue().getTelefone()))
        );

        colBuscaDataNascimento.setCellValueFactory(cell ->
                new SimpleStringProperty(formatarData(cell.getValue().getDataNascimento()))
        );

        colBuscaAtivo.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().isAtivo() ? "Sim" : "Não")
        );

        tableBusca.setItems(pacientes);

        tableBusca.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldValue, selected) -> selecionarPaciente(selected)
        );
    }

    public void configurarCampoBusca(Runnable onBuscar) {
        if (txtBusca != null) {
            txtBusca.setOnAction(event -> onBuscar.run());
        }
    }

    public void buscar(Consumer<String> mensagemConsumer) {
        try {
            String termo = txtBusca == null ? "" : txtBusca.getText();
            boolean incluirInativos = chkMostrarInativosBusca != null
                    && chkMostrarInativosBusca.isSelected();

            List<Paciente> resultado = pacienteService.buscar(termo, incluirInativos);

            pacientes.setAll(resultado);
            limparSelecao();

        } catch (BusinessException e) {
            mensagemConsumer.accept(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mensagemConsumer.accept("Erro ao buscar pacientes.");
        }
    }

    public void limparBusca(Consumer<String> mensagemConsumer) {
        if (txtBusca != null) {
            txtBusca.clear();
        }

        if (chkMostrarInativosBusca != null) {
            chkMostrarInativosBusca.setSelected(false);
        }

        buscar(mensagemConsumer);
    }

    public void carregarTodos(boolean incluirInativos, Consumer<String> mensagemConsumer) {
        try {
            pacientes.setAll(pacienteService.listarTodos(incluirInativos));
        } catch (BusinessException e) {
            mensagemConsumer.accept(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mensagemConsumer.accept("Erro ao atualizar lista de pacientes.");
        }
    }

    public Paciente obterSelecionado() {
        if (tableBusca == null) {
            return null;
        }

        return tableBusca.getSelectionModel().getSelectedItem();
    }

    public void limparSelecao() {
        if (tableBusca != null) {
            tableBusca.getSelectionModel().clearSelection();
        }

        selecionarPaciente(null);
    }

    private void selecionarPaciente(Paciente paciente) {
        atualizarBotoesBusca(paciente != null);

        if (selectionConsumer != null) {
            selectionConsumer.accept(paciente);
        }
    }

    private void atualizarBotoesBusca(boolean selecionado) {
        if (btnEditarSelecionado != null) {
            btnEditarSelecionado.setDisable(!selecionado);
        }

        if (btnHistoricoSelecionado != null) {
            btnHistoricoSelecionado.setDisable(!selecionado);
        }
    }

    private String formatDocumento(Paciente paciente) {
        if (paciente == null) {
            return "";
        }

        String cpf = safe(paciente.getCpf());

        if (!cpf.isBlank()) {
            return formatter.formatCpf(cpf);
        }

        return rgFormatter.apply(paciente.getRg());
    }

    private String formatarData(LocalDate data) {
        if (data == null) {
            return "";
        }

        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}