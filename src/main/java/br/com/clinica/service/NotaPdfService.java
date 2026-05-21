package br.com.clinica.service;

import br.com.clinica.model.Nota;
import br.com.clinica.model.NotaItem;
import br.com.clinica.model.enums.TipoItemNota;
import br.com.clinica.service.pdf.PdfStyleUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class NotaPdfService {

    private static final DateTimeFormatter DATA_HORA_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void gerarRecibo(Nota nota, File destino) throws IOException {
        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float margin = 42f;
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float tableWidth = pageWidth - (margin * 2);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                float y = pageHeight - margin;

                try {
                    y = PdfStyleUtil.drawHeader(
                            document,
                            cs,
                            margin,
                            y,
                            "Recibo / Nota de Atendimento",
                            "Comprovante de lançamento financeiro e atendimento clínico."
                    );

                    PdfStyleUtil.drawText(cs, margin, y, 11, true, "Dados da nota", PdfStyleUtil.TEXT);
                    y -= 20;

                    PdfStyleUtil.drawText(cs, margin, y, 9, true, "Data/Hora:", PdfStyleUtil.MUTED);
                    PdfStyleUtil.drawText(cs, margin + 75, y, 9, false,
                            nota.getDataHora() == null ? "" : nota.getDataHora().format(DATA_HORA_FORMATTER),
                            PdfStyleUtil.TEXT);

                    PdfStyleUtil.drawText(cs, margin + 280, y, 9, true, "Forma:", PdfStyleUtil.MUTED);
                    PdfStyleUtil.drawText(cs, margin + 330, y, 9, false,
                            nota.getFormaPagamento(),
                            PdfStyleUtil.TEXT);
                    y -= 16;

                    PdfStyleUtil.drawText(cs, margin, y, 9, true, "Paciente:", PdfStyleUtil.MUTED);
                    PdfStyleUtil.drawText(cs, margin + 75, y, 9, false,
                            nota.getPaciente() == null ? "" : nota.getPaciente().getNome(),
                            PdfStyleUtil.TEXT);
                    y -= 16;

                    PdfStyleUtil.drawText(cs, margin, y, 9, true, "Profissional:", PdfStyleUtil.MUTED);
                    PdfStyleUtil.drawText(cs, margin + 75, y, 9, false,
                            nota.getProfissional() == null ? "" : nota.getProfissional().getNome(),
                            PdfStyleUtil.TEXT);

                    y -= 28;

                    PdfStyleUtil.drawSummaryCard(
                            cs,
                            margin,
                            y,
                            170,
                            54,
                            "Subtotal",
                            PdfStyleUtil.money(nota.getTotalBruto()),
                            PdfStyleUtil.TEXT
                    );

                    PdfStyleUtil.drawSummaryCard(
                            cs,
                            margin + 188,
                            y,
                            170,
                            54,
                            "Total",
                            PdfStyleUtil.money(nota.getTotalLiquido()),
                            PdfStyleUtil.PRIMARY
                    );

                    PdfStyleUtil.drawSummaryCard(
                            cs,
                            margin + 376,
                            y,
                            135,
                            54,
                            "Itens",
                            String.valueOf(nota.getItens() == null ? 0 : nota.getItens().size()),
                            PdfStyleUtil.TEXT
                    );

                    y -= 78;

                    PdfStyleUtil.drawText(cs, margin, y, 11, true, "Itens da nota", PdfStyleUtil.TEXT);
                    y -= 18;

                    String[] headers = {"ITEM", "DESCRIÇÃO", "TIPO", "QTD", "UNIT.", "TOTAL"};
                    float[] widths = {35, 210, 80, 45, 80, 80};

                    PdfStyleUtil.tableHeader(cs, margin, y, tableWidth, headers, widths);
                    y -= 18;

                    int index = 1;

                    if (nota.getItens() != null) {
                        for (NotaItem item : nota.getItens()) {
                            if (y < 95) {
                                break;
                            }

                            String tipo =
                                    item.getTipoItem() == TipoItemNota.PRODUTO
                                            ? "Produto"
                                            : "Procedimento";

                            String[] values = {
                                    String.valueOf(index),
                                    item.getDescricao(),
                                    tipo,
                                    String.format("%.2f", item.getQuantidade()),
                                    PdfStyleUtil.money(item.getValorUnitario()),
                                    PdfStyleUtil.money(item.getValorTotal())
                            };

                            PdfStyleUtil.tableRow(cs, margin, y, tableWidth, values, widths, index % 2 == 0);
                            y -= 18;
                            index++;
                        }
                    }

                    y -= 20;

                    if (nota.getObservacao() != null && !nota.getObservacao().isBlank()) {
                        PdfStyleUtil.drawText(cs, margin, y, 10, true, "Observações", PdfStyleUtil.TEXT);
                        y -= 15;
                        PdfStyleUtil.drawText(cs, margin, y, 9, false, nota.getObservacao(), PdfStyleUtil.TEXT);
                        y -= 25;
                    }

                    y -= 25;
                    PdfStyleUtil.line(cs, margin, margin + 230, y, PdfStyleUtil.BORDER);
                    PdfStyleUtil.drawText(cs, margin + 35, y - 14, 8, false,
                            "Assinatura do responsável",
                            PdfStyleUtil.MUTED);

                    PdfStyleUtil.drawFooter(
                            cs,
                            margin,
                            pageWidth,
                            42,
                            "Recibo emitido pelo sistema Clínica Klean. Guarde este documento para conferência."
                    );

                } catch (Exception e) {
                    throw new IOException("Erro ao montar PDF da nota.", e);
                }
            }

            document.save(destino);
        }
    }
}