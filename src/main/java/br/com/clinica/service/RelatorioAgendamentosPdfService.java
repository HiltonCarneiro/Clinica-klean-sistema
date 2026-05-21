package br.com.clinica.service;

import br.com.clinica.model.Agendamento;
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

public class RelatorioAgendamentosPdfService {

    private static final Locale PT = Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", PT);

    public void gerar(
            LocalDate inicio,
            LocalDate fim,
            String profissional,
            List<Agendamento> itens,
            File destino
    ) throws Exception {

        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 42f;
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float tableWidth = pageWidth - (margin * 2);

            try (PDPageContentStream cs =
                         new PDPageContentStream(doc, page)) {

                float y = pageHeight - margin;

                y = PdfStyleUtil.drawHeader(
                        doc,
                        cs,
                        margin,
                        y,
                        "Relatório de Agendamentos",
                        "Agenda clínica e atendimentos do período."
                );

                PdfStyleUtil.drawSummaryCard(
                        cs,
                        margin,
                        y,
                        180,
                        54,
                        "Período",
                        format(inicio) + " até " + format(fim),
                        PdfStyleUtil.PRIMARY
                );

                PdfStyleUtil.drawSummaryCard(
                        cs,
                        margin + 200,
                        y,
                        150,
                        54,
                        "Atendimentos",
                        String.valueOf(itens == null ? 0 : itens.size()),
                        PdfStyleUtil.TEXT
                );

                PdfStyleUtil.drawSummaryCard(
                        cs,
                        margin + 370,
                        y,
                        140,
                        54,
                        "Profissional",
                        profissional == null || profissional.isBlank()
                                ? "Todos"
                                : profissional,
                        PdfStyleUtil.TEXT
                );

                y -= 78;

                PdfStyleUtil.drawText(
                        cs,
                        margin,
                        y,
                        11,
                        true,
                        "Consultas e procedimentos",
                        PdfStyleUtil.TEXT
                );

                y -= 18;

                String[] headers = {
                        "DATA",
                        "HORÁRIO",
                        "PACIENTE",
                        "PROFISSIONAL",
                        "STATUS"
                };

                float[] widths = {
                        80,
                        70,
                        170,
                        160,
                        90
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

                if (itens != null) {

                    for (Agendamento ag : itens) {

                        if (y < 70) {
                            break;
                        }

                        String horario =
                                safe(ag.getHoraInicio())
                                        + " - "
                                        + safe(ag.getHoraFim());

                        String[] values = {
                                ag.getData() == null
                                        ? ""
                                        : ag.getData().format(DATA),

                                horario,

                                safe(ag.getPacienteNome()),

                                safe(ag.getProfissionalNome()),

                                safe(ag.getStatus())
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

                PdfStyleUtil.drawFooter(
                        cs,
                        margin,
                        pageWidth,
                        42,
                        "Relatório operacional da agenda clínica - Clínica Klean."
                );
            }

            doc.save(destino);
        }
    }

    private String format(LocalDate d) {
        return d == null ? "" : d.format(DATA);
    }

    private String safe(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}