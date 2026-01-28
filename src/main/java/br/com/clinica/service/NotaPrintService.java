package br.com.clinica.service;

import br.com.clinica.model.Nota;
import br.com.clinica.model.NotaItem;
import br.com.clinica.model.TipoItemNota;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Scale;
import javafx.stage.Window;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;

public class NotaPrintService {

    private static final DateTimeFormatter DATA_HORA_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void imprimirNota(Nota nota, Window janelaDona) {
        // ROOT
        VBox root = new VBox(10);
        root.setPadding(new Insets(30));
        root.setSpacing(15);
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(700);

        // ==== CABEÇALHO: LOGO + INFO CLÍNICA ====
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView logoView = carregarLogo();
        if (logoView != null) {
            logoView.setPreserveRatio(true);
            logoView.setFitHeight(80);
            header.getChildren().add(logoView);
        }

        VBox clinicBox = new VBox(4);
        Label clinicName = labelBold("Klean Saúde Integrativa", 18);
        Label clinicSub = normalLabel("Clínica de Saúde Integrativa");
        clinicSub.setFont(Font.font(12));
        Label clinicInfo1 = normalLabel("Endereço: ______________________________");
        Label clinicInfo2 = normalLabel("Telefone: ______________________________");

        clinicBox.getChildren().addAll(clinicName, clinicSub, clinicInfo1, clinicInfo2);
        header.getChildren().add(clinicBox);

        root.getChildren().add(header);
        root.getChildren().add(new Separator());

        // ==== TÍTULO DA NOTA ====
        Label titulo = labelBold("RECIBO / NOTA DE ATENDIMENTO", 16);
        titulo.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(titulo);

        // ==== BLOCOS: PACIENTE / PROFISSIONAL ====
        HBox infoTopRow = new HBox(20);
        infoTopRow.setAlignment(Pos.TOP_LEFT);

        // Paciente
        VBox pacienteBox = new VBox(3);
        Label pacienteTitle = sectionTitle("DADOS DO PACIENTE");
        Label pacienteNome = normalLabel("Nome: " + nota.getPaciente().getNome());
        pacienteBox.getChildren().addAll(pacienteTitle, pacienteNome);

        // Profissional
        VBox profBox = new VBox(3);
        Label profTitle = sectionTitle("DADOS DO PROFISSIONAL");
        Label profNome = normalLabel("Nome: " + nota.getProfissional().getNome());
        profBox.getChildren().addAll(profTitle, profNome);

        HBox.setHgrow(pacienteBox, Priority.ALWAYS);
        HBox.setHgrow(profBox, Priority.ALWAYS);

        infoTopRow.getChildren().addAll(pacienteBox, profBox);
        root.getChildren().add(infoTopRow);

        // ==== BLOCO: DADOS DA NOTA ====
        GridPane notaGrid = new GridPane();
        notaGrid.setHgap(15);
        notaGrid.setVgap(5);
        notaGrid.setPadding(new Insets(5, 0, 0, 0));

        Label lblData = boldSmall("DATA:");
        Label valData = normalLabel(nota.getDataHora().format(DATA_HORA_FORMATTER));

        Label lblForma = boldSmall("FORMA DE PAGAMENTO:");
        Label valForma = normalLabel(nota.getFormaPagamento());

        notaGrid.add(lblData, 0, 0);
        notaGrid.add(valData, 1, 0);
        notaGrid.add(lblForma, 0, 1);
        notaGrid.add(valForma, 1, 1);

        root.getChildren().add(notaGrid);
        root.getChildren().add(new Separator());

        // ==== TABELA DE ITENS ====
        VBox tabelaBox = new VBox(3);
        tabelaBox.setFillWidth(true);

        // Cabeçalho da tabela
        HBox headerRow = new HBox();
        headerRow.setStyle("-fx-background-color: #e6f0ff; -fx-border-color: #b0c4de; -fx-border-width: 1 1 1 1;");
        headerRow.setPadding(new Insets(5));

        Label colItem = tableHeader("ITEM");
        Label colDesc = tableHeader("DESCRIÇÃO");
        Label colQtd = tableHeader("QTD");
        Label colVlrUnit = tableHeader("VALOR UNIT.");
        Label colVlrTotal = tableHeader("VALOR TOTAL");

        colItem.setPrefWidth(80);
        colDesc.setPrefWidth(260);
        colQtd.setPrefWidth(60);
        colVlrUnit.setPrefWidth(110);
        colVlrTotal.setPrefWidth(110);

        headerRow.getChildren().addAll(colItem, colDesc, colQtd, colVlrUnit, colVlrTotal);
        tabelaBox.getChildren().add(headerRow);

        // Linhas da tabela
        int index = 1;
        for (NotaItem item : nota.getItens()) {
            HBox row = new HBox();
            row.setPadding(new Insets(4));
            row.setStyle("-fx-border-color: #dcdcdc; -fx-border-width: 0 1 1 1;");

            String tipo = (item.getTipoItem() == TipoItemNota.PRODUTO) ? "Produto" : "Procedimento";
            String itemLabel = index + " - " + tipo;

            Label lItem = tableCell(itemLabel, colItem.getPrefWidth());
            Label lDesc = tableCell(item.getDescricao(), colDesc.getPrefWidth());
            Label lQtd = tableCell(String.format("%.2f", item.getQuantidade()), colQtd.getPrefWidth());
            Label lUnit = tableCell(String.format("R$ %.2f", item.getValorUnitario()), colVlrUnit.getPrefWidth());
            Label lTotal = tableCell(String.format("R$ %.2f", item.getValorTotal()), colVlrTotal.getPrefWidth());

            row.getChildren().addAll(lItem, lDesc, lQtd, lUnit, lTotal);
            tabelaBox.getChildren().add(row);
            index++;
        }

        root.getChildren().add(tabelaBox);

        // ==== RESUMO DE TOTAIS ====
        VBox resumoBox = new VBox(3);
        resumoBox.setAlignment(Pos.CENTER_RIGHT);
        resumoBox.setPadding(new Insets(10, 0, 0, 0));

        Label subtotal = normalLabel(String.format("Subtotal: R$ %.2f", nota.getTotalBruto()));
        subtotal.setFont(Font.font(11));
        Label total = labelBold(String.format("TOTAL: R$ %.2f", nota.getTotalLiquido()), 13);
        total.setTextFill(Color.web("#1d4ed8")); // azulzinho

        resumoBox.getChildren().addAll(subtotal, total);
        root.getChildren().add(resumoBox);

        // ==== OBSERVAÇÕES ====
        if (nota.getObservacao() != null && !nota.getObservacao().isBlank()) {
            VBox obsBox = new VBox(3);
            obsBox.setPadding(new Insets(10, 0, 0, 0));
            Label obsTitle = sectionTitle("OBSERVAÇÕES");
            Label obsText = normalLabel(nota.getObservacao());
            obsText.setWrapText(true);
            obsBox.getChildren().addAll(obsTitle, obsText);
            root.getChildren().add(obsBox);
        }

        root.getChildren().add(new Separator());

        // ==== RODAPÉ: ASSINATURA ====
        VBox rodape = new VBox(5);
        rodape.setPadding(new Insets(10, 0, 0, 0));
        rodape.setAlignment(Pos.CENTER_LEFT);

        Label linhaAss = normalLabel("__________________________________________");
        Label txtAss = normalLabel("Assinatura do responsável");
        txtAss.setFont(Font.font(10));

        rodape.getChildren().addAll(linhaAss, txtAss);
        root.getChildren().add(rodape);

        // ==== IMPRESSÃO COM ESCALA PARA A PÁGINA ====
        Scene scene = new Scene(root);
        root.applyCss();
        root.layout();

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean aceitar = job.showPrintDialog(janelaDona);
            if (aceitar) {
                PageLayout pageLayout = job.getJobSettings().getPageLayout();

                double pagePrintableWidth = pageLayout.getPrintableWidth();
                double pagePrintableHeight = pageLayout.getPrintableHeight();

                double nodeWidth = root.getBoundsInParent().getWidth();
                double nodeHeight = root.getBoundsInParent().getHeight();

                double scaleX = pagePrintableWidth / nodeWidth;
                double scaleY = pagePrintableHeight / nodeHeight;
                double scale = Math.min(scaleX, scaleY);

                Scale transform = new Scale(scale, scale);
                root.getTransforms().add(transform);

                job.printPage(root);
                job.endJob();

                root.getTransforms().remove(transform);
            }
        }
    }

    // ======= HELPERS =======

    private ImageView carregarLogo() {
        try {
            InputStream is = getClass().getResourceAsStream("/images/logo-klean.png");
            if (is == null) {
                return null;
            }
            Image img = new Image(is);
            return new ImageView(img);
        } catch (Exception e) {
            return null;
        }
    }

    private void configurarNoEllipsis(Label lbl) {
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setWrapText(true);
        lbl.setTextOverrun(OverrunStyle.CLIP); // se faltar espaço, corta texto, não vira "..."
    }

    private Label normalLabel(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font(11));
        configurarNoEllipsis(lbl);
        return lbl;
    }

    private Label labelBold(String texto, int size) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font(null, FontWeight.BOLD, size));
        configurarNoEllipsis(lbl);
        return lbl;
    }

    private Label boldSmall(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font(null, FontWeight.BOLD, 11));
        configurarNoEllipsis(lbl);
        return lbl;
    }

    private Label sectionTitle(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font(null, FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web("#1d4ed8")); // azul
        configurarNoEllipsis(lbl);
        return lbl;
    }

    private Label tableHeader(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font(null, FontWeight.BOLD, 11));
        configurarNoEllipsis(lbl);
        return lbl;
    }

    private Label tableCell(String texto, double prefWidth) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font(10));
        lbl.setWrapText(true);
        lbl.setPrefWidth(prefWidth);
        lbl.setMinWidth(prefWidth);
        configurarNoEllipsis(lbl);
        return lbl;
    }
}
