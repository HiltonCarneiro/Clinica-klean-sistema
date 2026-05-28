package br.com.clinica.controller.relatorios;

import br.com.clinica.dao.NotaDAO;
import br.com.clinica.model.Nota;
import br.com.clinica.service.NotaPdfService;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.function.BiConsumer;

public class RelatoriosNotaReimpressaoManager {

    public void reimprimirNota(
            TableView<NotaDAO.NotaResumo> tblNotas,
            NotaDAO notaDAO,
            NotaPdfService notaPdfService,
            BiConsumer<String, String> avisoConsumer,
            BiConsumer<String, String> erroConsumer
    ) {
        NotaDAO.NotaResumo selecionada = tblNotas.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            avisoConsumer.accept("Selecione uma nota", "Selecione uma nota na tabela para reimprimir.");
            return;
        }

        try {
            Nota nota = notaDAO.buscarNotaCompleta(selecionada.getId());

            File destino = escolherArquivoPdf(tblNotas, nota);

            if (destino == null) return;

            notaPdfService.gerarRecibo(nota, destino);

            avisoConsumer.accept(
                    "PDF gerado com sucesso",
                    "A nota foi reimpressa e salva em:\n" + destino.getAbsolutePath()
            );

        } catch (Exception e) {
            e.printStackTrace();
            erroConsumer.accept("Erro ao reimprimir", e.getMessage());
        }
    }

    private File escolherArquivoPdf(TableView<NotaDAO.NotaResumo> tblNotas, Nota nota) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar PDF da nota");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fileChooser.setInitialFileName("nota_ID" + nota.getId() + ".pdf");

        return fileChooser.showSaveDialog(tblNotas.getScene().getWindow());
    }
}