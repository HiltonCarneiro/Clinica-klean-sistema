package br.com.clinica.controller.paciente;

import br.com.clinica.model.Paciente;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PacienteTableManager {

    private final TableView<Paciente> tabela;
    private final ObservableList<Paciente> pacientes;

    public PacienteTableManager(TableView<Paciente> tabela) {
        this.tabela = tabela;
        this.pacientes = FXCollections.observableArrayList();
    }

    public void configurarTabela(
            TableColumn<Paciente, String> colNome,
            TableColumn<Paciente, String> colCpf,
            TableColumn<Paciente, String> colTelefone
    ) {

        colNome.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getNome())
        );

        colCpf.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCpf())
        );

        colTelefone.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getTelefone())
        );

        tabela.setItems(pacientes);
    }

    public void carregarPacientes(List<Paciente> lista) {

        pacientes.clear();

        if (lista != null) {
            pacientes.addAll(lista);
        }
    }

    public void adicionarPaciente(Paciente paciente) {

        if (paciente != null) {
            pacientes.add(paciente);
        }
    }

    public void removerPaciente(Paciente paciente) {

        if (paciente != null) {
            pacientes.remove(paciente);
        }
    }

    public void atualizarTabela() {
        tabela.refresh();
    }

    public ObservableList<Paciente> getPacientes() {
        return pacientes;
    }

    public void aplicarFiltro(Predicate<Paciente> predicate) {

        if (predicate == null) {
            tabela.setItems(pacientes);
            return;
        }

        List<Paciente> filtrados = pacientes.stream()
                .filter(predicate)
                .collect(Collectors.toList());

        tabela.setItems(FXCollections.observableArrayList(filtrados));
    }
}