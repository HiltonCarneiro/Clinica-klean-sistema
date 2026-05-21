package br.com.clinica.service;

import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.enums.TipoMovimento;
import br.com.clinica.service.pdf.PdfStyleUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class RelatorioCaixaPdfService {

    private static final Locale PT = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT);

    public void gerar(LocalDate ini, LocalDate fim, List<MovimentoCaixa> itens, File destino) throws Exception {

        double entradas = itens.stream()
                .filter(m -> m.getTipo() == TipoMovimento.ENTRADA)
                .mapToDouble(MovimentoCaixa::getValor)
                .sum();

        double saidas = itens.stream()
                .filter(m -> m.getTipo() == TipoMovimento.SAIDA)
                .mapToDouble(MovimentoCaixa::getValor)
                .sum();

        double saldo = entradas - saidas;

        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 42f;
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float tableWidth = pageWidth - (margin * 2);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                float y = pageHeight - margin;

                y = PdfStyleUtil.drawHeader(
                        doc,
                        cs,
                        margin,
                        y,
                        "Relatório de Caixa",
                        "Período: " + format(ini) + " até " + format(fim)
                );

                PdfStyleUtil.drawSummaryCard(
                        cs,
                        margin,
                        y,
                        150,
                        54,
                        "Entradas",
                        PdfStyleUtil.money(entradas),
                        new java.awt.Color(22, 163, 74)
                );

                PdfStyleUtil.drawSummaryCard(
                        cs,
                        margin + 170,
                        y,
                        150,
                        54,
                        "Saídas",
                        PdfStyleUtil.money(saidas),
                        new java.awt.Color(220, 38, 38)
                );

                PdfStyleUtil.drawSummaryCard(
                        cs,
                        margin + 340,
                        y,
                        170,
                        54,
                        "Saldo",
                        PdfStyleUtil.money(saldo),
                        PdfStyleUtil.PRIMARY
                );

                y -= 78;

                PdfStyleUtil.drawText(cs, margin, y, 11, true, "Movimentações financeiras", PdfStyleUtil.TEXT);
                y -= 18;

                String[] headers = {"DATA", "TIPO", "FORMA", "VALOR", "DESCRIÇÃO / PACIENTE"};
                float[] widths = {70, 65, 80, 80, 235};

                PdfStyleUtil.tableHeader(cs, margin, y, tableWidth, headers, widths);
                y -= 18;

                int index = 0;

                for (MovimentoCaixa m : itens) {
                    if (y < 70) {
                        break;
                    }

                    String descricao = safe(m.getDescricao());

                    if (!safe(m.getPacienteNome()).isBlank()) {
                        descricao += " - " + safe(m.getPacienteNome());
                    }

                    String[] values = {
                            m.getData() == null ? "" : m.getData().format(D),
                            m.getTipo() == null ? "" : m.getTipo().name(),
                            safe(m.getFormaPagamento()),
                            PdfStyleUtil.money(m.getValor()),
                            descricao
                    };

                    PdfStyleUtil.tableRow(cs, margin, y, tableWidth, values, widths, index % 2 == 0);
                    y -= 18;
                    index++;
                }

                PdfStyleUtil.drawFooter(
                        cs,
                        margin,
                        pageWidth,
                        42,
                        "Relatório financeiro gerado pelo sistema Clínica Klean."
                );
            }

            doc.save(destino);
        }
    }

    private String format(LocalDate date) {
        return date == null ? "" : date.format(D);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}