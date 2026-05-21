package br.com.clinica.controller.agenda;

import br.com.clinica.model.Agendamento;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;

import java.util.function.Consumer;

public class AgendaDateChangeManager {

    public void onDataAlterada(
            DatePicker dpData,
            TableView<Agendamento> tbAgenda,
            AgendaDateRuleManager dateRuleManager,
            Consumer<String> messageConsumer,
            Runnable carregarAgendaDoDia
    ) {
        boolean dataValida = dateRuleManager.validarDataSelecionada(
                dpData,
                tbAgenda,
                messageConsumer
        );

        if (!dataValida) {
            return;
        }

        if (carregarAgendaDoDia != null) {
            carregarAgendaDoDia.run();
        }
    }
}