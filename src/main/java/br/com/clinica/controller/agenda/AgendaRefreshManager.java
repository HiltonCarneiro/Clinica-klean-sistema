package br.com.clinica.controller.agenda;

public class AgendaRefreshManager {

    public void atualizarLista(Runnable carregarAgendaDoDia) {
        if (carregarAgendaDoDia != null) {
            carregarAgendaDoDia.run();
        }
    }
}