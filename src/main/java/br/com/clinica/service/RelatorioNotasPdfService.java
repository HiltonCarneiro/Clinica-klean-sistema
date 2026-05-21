package br.com.clinica.service;

import br.com.clinica.dao.NotaDAO;
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

public class RelatorioNotasPdfService {

    private static final Locale PT = Locale.forLanguageTag("pt-BR");

    public void gerar(
            LocalDate inicio,
            LocalDate fim,
            String pacienteFiltro,
            String profissional,
            String formaPagamento,
            List<NotaDAO.NotaResumo> notas,
            File destino
    ) throws Exception {

        double total = notas == null
                ? 0
                : notas.stream()
                  .mapToDouble(NotaDAO.NotaResumo::getTotalLiquido)
                  .sum();

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
                        "Relatório de Notas",
                        "Notas e recibos emitidos pela clínica."
                );

                PdfStyleUtil.drawSummaryCard(
                        cs,
                        margin,
                        y,
                        170,
                        54,
                        "Período",
                        format(inicio) + " até " + format(fim),
                        PdfStyleUtil.PRIMARY
                );

                PdfStyleUtil.drawSummaryCard(
                        cs,
                        margin + 190,
                        y,
                        120,
                        54,
                        "Notas",
                        String.valueOf(notas == null ? 0 : notas.size()),
                        PdfStyleUtil.TEXT
                );

                PdfStyleUtil.drawSummaryCard(
                        cs,
                        margin + 330,
                        y,
                        180,
                        54,
                        "Valor total",
                        PdfStyleUtil.money(total),
                        PdfStyleUtil.PRIMARY
                );

                y -= 78;

                PdfStyleUtil.drawText(
                        cs,
                        margin,
                        y,
                        11,
                        true,
                        "Notas emitidas",
                        PdfStyleUtil.TEXT
                );

                y -= 18;

                String[] headers = {
                        "ID",
                        "DATA/HORA",
                        "PACIENTE",
                        "PROFISSIONAL",
                        "FORMA",
                        "TOTAL"
                };

                float[] widths = {
                        45,
                        95,
                        150,
                        130,
                        90,
                        70
                };

                PdfStyleUtil.tableHeader(
                        cs,
                        margin,
                        y,
                        tableWidth,
                        headers,
                        widths
                );

                y -= 18;

                int index = 0;

                if (notas != null) {

                    for (NotaDAO.NotaResumo nota : notas) {

                        if (y < 70) {
                            break;
                        }

                        String[] values = {
                                String.valueOf(nota.getId()),
                                safe(nota.getDataHoraFmt()),
                                safe(nota.getPacienteNome()),
                                safe(nota.getProfissionalNome()),
                                safe(nota.getFormaPagamento()),
                                PdfStyleUtil.money(nota.getTotalLiquido())
                        };

                        PdfStyleUtil.tableRow(
                                cs,
                                margin,
                                y,
                                tableWidth,
                                values,
                                widths,
                                index % 2 == 0
                        );

                        y -= 18;
                        index++;
                    }
                }

                y -= 16;

                PdfStyleUtil.drawText(
                        cs,
                        margin,
                        y,
                        10,
                        true,
                        "Filtros aplicados",
                        PdfStyleUtil.TEXT
                );

                y -= 16;

                PdfStyleUtil.drawText(
                        cs,
                        margin,
                        y,
                        8,
                        false,
                        "Paciente: " + valorOuTodos(pacienteFiltro),
                        PdfStyleUtil.TEXT
                );

                y -= 12;

                PdfStyleUtil.drawText(
                        cs,
                        margin,
                        y,
                        8,
                        false,
                        "Profissional: " + valorOuTodos(profissional),
                        PdfStyleUtil.TEXT
                );

                y -= 12;

                PdfStyleUtil.drawText(
                        cs,
                        margin,
                        y,
                        8,
                        false,
                        "Pagamento: " + valorOuTodos(formaPagamento),
                        PdfStyleUtil.TEXT
                );

                PdfStyleUtil.drawFooter(
                        cs,
                        margin,
                        pageWidth,
                        42,
                        "Relatório financeiro emitido pelo sistema Clínica Klean."
                );
            }

            doc.save(destino);
        }
    }

    private String format(LocalDate d) {
        return d == null ? "" : d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", PT));
    }

    private String valorOuTodos(String valor) {
        return valor == null || valor.isBlank() ? "Todos" : valor;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}