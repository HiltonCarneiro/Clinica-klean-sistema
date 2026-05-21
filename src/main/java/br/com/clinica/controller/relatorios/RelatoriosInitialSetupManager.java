package br.com.clinica.controller.relatorios;

import br.com.clinica.dao.NotaDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.Usuario;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public class RelatoriosInitialSetupManager {

    public static final String FORMA_TODAS = "TODAS";

    public void configurarDatasPadrao(
            DatePicker dtCxInicio,
            DatePicker dtCxFim,
            DatePicker dtAgInicio,
            DatePicker dtAgFim,
            DatePicker dtNotaInicio,
            DatePicker dtNotaFim
    ) {
        LocalDate hoje = LocalDate.now();

        dtCxInicio.setValue(hoje);
        dtCxFim.setValue(hoje);
        dtAgInicio.setValue(hoje);
        dtAgFim.setValue(hoje);
        dtNotaInicio.setValue(hoje);
        dtNotaFim.setValue(hoje);
    }

    public ObservableList<Usuario> montarListaProfissionais(
            List<Usuario> profissionais
    ) {
        ObservableList<Usuario> profObs = FXCollections.observableArrayList();
        profObs.add(null);
        profObs.addAll(profissionais);
        return profObs;
    }

    public void configurarComboProfissionais(
            ComboBox<Usuario> comboBox,
            ObservableList<Usuario> profissionais,
            Function<Usuario, String> nomeFormatter
    ) {
        comboBox.setItems(profissionais);

        comboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos");
                } else {
                    setText(nomeFormatter.apply(item));
                }
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos");
                } else {
                    setText(nomeFormatter.apply(item));
                }
            }
        });

        comboBox.getSelectionModel().selectFirst();
    }

    public void configurarComboFormaPagamento(
            ComboBox<String> cbNotaForma
    ) {
        cbNotaForma.setItems(FXCollections.observableArrayList(
                FORMA_TODAS,
                "DINHEIRO",
                "PIX",
                "CARTAO",
                "TRANSFERENCIA",
                "OUTRO"
        ));

        cbNotaForma.getSelectionModel().select(FORMA_TODAS);
    }

    public void configurarTabelaCaixa(
            TableView<MovimentoCaixa> tblCaixa,
            ObservableList<MovimentoCaixa> caixaObs,
            TableColumn<MovimentoCaixa, String> colCxData,
            TableColumn<MovimentoCaixa, String> colCxTipo,
            TableColumn<MovimentoCaixa, String> colCxDescricao,
            TableColumn<MovimentoCaixa, String> colCxForma,
            TableColumn<MovimentoCaixa, Double> colCxValor,
            TableColumn<MovimentoCaixa, String> colCxPaciente
    ) {
        tblCaixa.setItems(caixaObs);

        colCxData.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getData() != null
                                ? c.getValue().getData().toString()
                                : ""
                )
        );

        colCxTipo.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getTipo() != null
                                ? c.getValue().getTipo().name()
                                : ""
                )
        );

        colCxDescricao.setCellValueFactory(c ->
                new SimpleStringProperty(nvl(c.getValue().getDescricao()))
        );

        colCxForma.setCellValueFactory(c ->
                new SimpleStringProperty(nvl(c.getValue().getFormaPagamento()))
        );

        colCxValor.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getValor()).asObject()
        );

        colCxPaciente.setCellValueFactory(c ->
                new SimpleStringProperty(nvl(c.getValue().getPacienteNome()))
        );
    }

    public void configurarTabelaAgendamentos(
            TableView<Agendamento> tblAgendamentos,
            ObservableList<Agendamento> agObs,
            TableColumn<Agendamento, String> colAgData,
            TableColumn<Agendamento, String> colAgHora,
            TableColumn<Agendamento, String> colAgProf,
            TableColumn<Agendamento, String> colAgSala,
            TableColumn<Agendamento, String> colAgPaciente,
            TableColumn<Agendamento, String> colAgStatus,
            TableColumn<Agendamento, String> colAgProced
    ) {
        tblAgendamentos.setItems(agObs);

        colAgData.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getData() != null
                                ? c.getValue().getData().toString()
                                : ""
                )
        );

        colAgHora.setCellValueFactory(c ->
                new SimpleStringProperty(
                        (c.getValue().getHoraInicio() != null
                                ? c.getValue().getHoraInicio().toString()
                                : "")
                                + " - "
                                + (c.getValue().getHoraFim() != null
                                ? c.getValue().getHoraFim().toString()
                                : "")
                )
        );

        colAgProf.setCellValueFactory(c ->
                new SimpleStringProperty(nvl(c.getValue().getProfissionalNome()))
        );

        colAgSala.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getSala() != null
                                ? c.getValue().getSala().getDescricao()
                                : ""
                )
        );

        colAgPaciente.setCellValueFactory(c ->
                new SimpleStringProperty(nvl(c.getValue().getPacienteNome()))
        );

        colAgStatus.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getStatus() != null
                                ? c.getValue().getStatus().name()
                                : ""
                )
        );

        colAgProced.setCellValueFactory(c ->
                new SimpleStringProperty(nvl(c.getValue().getProcedimento()))
        );
    }

    public void configurarTabelaNotas(
            TableView<NotaDAO.NotaResumo> tblNotas,
            ObservableList<NotaDAO.NotaResumo> notasObs,
            TableColumn<NotaDAO.NotaResumo, Long> colNotaId,
            TableColumn<NotaDAO.NotaResumo, String> colNotaDataHora,
            TableColumn<NotaDAO.NotaResumo, String> colNotaPaciente,
            TableColumn<NotaDAO.NotaResumo, String> colNotaProfissional,
            TableColumn<NotaDAO.NotaResumo, String> colNotaForma,
            TableColumn<NotaDAO.NotaResumo, Double> colNotaTotal
    ) {
        tblNotas.setItems(notasObs);

        colNotaId.setCellValueFactory(c ->
                new SimpleLongProperty(c.getValue().getId()).asObject()
        );

        colNotaDataHora.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDataHoraFmt())
        );

        colNotaPaciente.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getPacienteNome())
        );

        colNotaProfissional.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getProfissionalNome())
        );

        colNotaForma.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFormaPagamento())
        );

        colNotaTotal.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getTotalLiquido()).asObject()
        );
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}