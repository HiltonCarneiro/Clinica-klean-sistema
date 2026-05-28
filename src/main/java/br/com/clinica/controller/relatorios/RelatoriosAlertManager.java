package br.com.clinica.controller.relatorios;

import br.com.clinica.service.DialogService;

public class RelatoriosAlertManager {

    private final DialogService dialogService = new DialogService();

    public void erro(String titulo, String mensagem) {
        dialogService.erro(titulo, mensagem);
    }

    public void aviso(String titulo, String mensagem) {
        dialogService.informacao(titulo, mensagem);
    }
}