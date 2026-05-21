package br.com.clinica.controller.agenda;

import br.com.clinica.model.Agendamento;
import javafx.scene.control.TableView;

import java.util.function.Consumer;

public class AgendaSelectionManager {

    public Agendamento obterSelecionado(
            TableView<Agendamento> tabela
    ) {
        if (tabela == null) {
            return null;
        }

        return tabela.getSelectionModel().getSelectedItem();
    }

    public boolean validarSelecao(
            Agendamento agendamento,
            Consumer<String> messageConsumer,
            String mensagem
    ) {

        if (agendamento != null) {
            return true;
        }

        if (messageConsumer != null) {
            messageConsumer.accept(mensagem);
        }

        return false;
    }
}