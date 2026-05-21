package br.com.clinica.controller.agenda;

import br.com.clinica.model.Agendamento;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AgendaLoadManager {

    public void carregarAsync(
            Supplier<List<Agendamento>> loader,
            TableView<Agendamento> tabela,
            ObservableList<Agendamento> lista,
            Consumer<String> infoConsumer
    ) {

        Task<List<Agendamento>> task = new Task<>() {
            @Override
            protected List<Agendamento> call() {
                return loader.get();
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {

            lista.clear();

            List<Agendamento> resultado = task.getValue();

            if (resultado != null) {
                lista.addAll(resultado);
            }

            tabela.refresh();

            if (infoConsumer != null) {
                infoConsumer.accept("");
            }
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {

            Throwable ex = task.getException();

            if (ex != null) {
                ex.printStackTrace();
            }

            if (infoConsumer != null) {
                infoConsumer.accept(
                        "Erro ao carregar agenda: "
                                + (ex != null ? ex.getMessage() : "")
                );
            }
        }));

        Thread thread = new Thread(task, "agenda-load");

        thread.setDaemon(true);
        thread.start();
    }
}