package br.com.clinica.controller.caixa;

import br.com.clinica.service.DialogService;

public class CaixaAlertManager {

    private final DialogService dialogService = new DialogService();

    public void mostrarErro(String titulo, String detalhe) {
        dialogService.erro(titulo, detalhe);
    }

    public void mostrarAviso(String titulo, String mensagem) {
        dialogService.informacao(titulo, mensagem);
    }
}