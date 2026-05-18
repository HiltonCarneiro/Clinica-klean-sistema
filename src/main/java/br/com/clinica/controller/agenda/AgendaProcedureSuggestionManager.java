package br.com.clinica.controller.agenda;

import br.com.clinica.model.enums.SalaAtendimento;
import br.com.clinica.service.AgendaService;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.geometry.Side;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.util.List;

public class AgendaProcedureSuggestionManager {

    private final AgendaService agendaService;
    private final ContextMenu menu = new ContextMenu();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(250));

    private long requestSequence = 0;

    public AgendaProcedureSuggestionManager(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    public void configure(
            TextField txtProcedimento,
            ComboBox<SalaAtendimento> cbSala
    ) {
        if (txtProcedimento == null) {
            return;
        }

        debounce.setOnFinished(event -> searchSuggestions(txtProcedimento, cbSala));

        txtProcedimento.textProperty().addListener((obs, oldValue, newValue) -> {
            String term = newValue == null ? "" : newValue.trim();

            if (term.length() < 2) {
                requestSequence++;
                menu.hide();
                return;
            }

            debounce.playFromStart();
        });

        txtProcedimento.focusedProperty().addListener((obs, oldValue, focused) -> {
            if (!focused) {
                requestSequence++;
                menu.hide();
            }
        });
    }

    private void searchSuggestions(
            TextField txtProcedimento,
            ComboBox<SalaAtendimento> cbSala
    ) {
        String term = txtProcedimento.getText() == null
                ? ""
                : txtProcedimento.getText().trim();

        if (term.length() < 2) {
            menu.hide();
            return;
        }

        SalaAtendimento sala = cbSala == null ? null : cbSala.getValue();
        long currentRequest = ++requestSequence;

        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() {
                return agendaService.sugerirProcedimentos(term, sala, 8);
            }
        };

        task.setOnSucceeded(event -> {
            if (currentRequest != requestSequence) {
                return;
            }

            String currentText = txtProcedimento.getText() == null
                    ? ""
                    : txtProcedimento.getText().trim();

            if (!currentText.equals(term)) {
                return;
            }

            showSuggestions(txtProcedimento, task.getValue());
        });

        task.setOnFailed(event -> {
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
            menu.hide();
        });

        Thread thread = new Thread(task, "agenda-procedure-suggestions");
        thread.setDaemon(true);
        thread.start();
    }

    private void showSuggestions(TextField txtProcedimento, List<String> suggestions) {
        menu.getItems().clear();

        if (suggestions == null || suggestions.isEmpty()) {
            menu.hide();
            return;
        }

        for (String suggestion : suggestions) {
            MenuItem item = new MenuItem(suggestion);

            item.setOnAction(event -> {
                txtProcedimento.setText(suggestion);
                txtProcedimento.positionCaret(suggestion.length());
                menu.hide();
            });

            menu.getItems().add(item);
        }

        if (!menu.isShowing()) {
            menu.show(txtProcedimento, Side.BOTTOM, 0, 0);
        }
    }
}