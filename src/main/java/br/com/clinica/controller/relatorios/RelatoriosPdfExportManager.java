package br.com.clinica.controller.relatorios;

import br.com.clinica.dao.NotaDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.service.RelatorioAgendamentosPdfService;
import br.com.clinica.service.RelatorioCaixaPdfService;
import br.com.clinica.service.RelatorioNotasPdfService;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.function.BiConsumer;

public class RelatoriosPdfExportManager {

    public void exportarCaixaPdf(
            Node ownerNode,
            ObservableList<MovimentoCaixa> caixaObs,
            LocalDate inicio,
            LocalDate fim,
            RelatorioCaixaPdfService relCaixaPdf,
            BiConsumer<String, String> avisoConsumer,
            BiConsumer<String, String> erroConsumer
    ) {
        if (caixaObs.isEmpty()) {
            avisoConsumer.accept("Nada para exportar", "Busque um relatório antes de exportar.");
            return;
        }

        File destino = escolherArquivoPdf(ownerNode, "Salvar relatório do caixa (PDF)", "relatorio_caixa.pdf");

        if (destino == null) return;

        try {
            relCaixaPdf.gerar(inicio, fim, caixaObs, destino);
            avisoConsumer.accept(
                    "PDF gerado com sucesso",
                    "O relatório de caixa foi salvo em:\n" + destino.getAbsolutePath()
            );
        } catch (Exception e) {
            e.printStackTrace();
            erroConsumer.accept("Erro ao gerar PDF", e.getMessage());
        }
    }

    public void exportarAgendamentosPdf(
            Node ownerNode,
            ObservableList<Agendamento> agObs,
            LocalDate inicio,
            LocalDate fim,
            String profissional,
            RelatorioAgendamentosPdfService relAgPdf,
            BiConsumer<String, String> avisoConsumer,
            BiConsumer<String, String> erroConsumer
    ) {
        if (agObs.isEmpty()) {
            avisoConsumer.accept("Nada para exportar", "Busque um relatório antes de exportar.");
            return;
        }

        File destino = escolherArquivoPdf(ownerNode, "Salvar relatório de agendamentos (PDF)", "relatorio_agendamentos.pdf");

        if (destino == null) return;

        try {
            relAgPdf.gerar(inicio, fim, profissional, agObs, destino);
            avisoConsumer.accept(
                    "PDF gerado com sucesso",
                    "O relatório de agendamentos foi salvo em:\n" + destino.getAbsolutePath()
            );
        } catch (Exception e) {
            e.printStackTrace();
            erroConsumer.accept("Erro ao gerar PDF", e.getMessage());
        }
    }

    public void exportarNotasPdf(
            Node ownerNode,
            ObservableList<NotaDAO.NotaResumo> notasObs,
            LocalDate inicio,
            LocalDate fim,
            String pacienteFiltro,
            String profissional,
            String formaPagamento,
            RelatorioNotasPdfService relNotasPdf,
            BiConsumer<String, String> avisoConsumer,
            BiConsumer<String, String> erroConsumer
    ) {
        if (notasObs.isEmpty()) {
            avisoConsumer.accept("Nada para exportar", "Busque um relatório antes de exportar.");
            return;
        }

        File destino = escolherArquivoPdf(ownerNode, "Salvar relatório de notas (PDF)", "relatorio_notas.pdf");

        if (destino == null) return;

        try {
            relNotasPdf.gerar(inicio, fim, pacienteFiltro, profissional, formaPagamento, notasObs, destino);
            avisoConsumer.accept(
                    "PDF gerado com sucesso",
                    "O relatório de notas foi salvo em:\n" + destino.getAbsolutePath()
            );
        } catch (Exception e) {
            e.printStackTrace();
            erroConsumer.accept("Erro ao gerar PDF", e.getMessage());
        }
    }

    private File escolherArquivoPdf(Node ownerNode, String titulo, String nomeInicial) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(titulo);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fileChooser.setInitialFileName(nomeInicial);

        return fileChooser.showSaveDialog(ownerNode.getScene().getWindow());
    }
}