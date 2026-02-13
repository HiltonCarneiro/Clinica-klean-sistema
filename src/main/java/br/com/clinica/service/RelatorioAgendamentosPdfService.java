package br.com.clinica.service;

import br.com.clinica.model.Agendamento;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class RelatorioAgendamentosPdfService {

    private static final Locale PT = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT);
    private static final DateTimeFormatter H = DateTimeFormatter.ofPattern("HH:mm", PT);
    private static final DateTimeFormatter DH = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", PT);

    public void gerar(LocalDate ini, LocalDate fim, String profissionalFiltro,
                      List<Agendamento> itens, File destino) throws Exception {

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 50f;
            float y = page.getMediaBox().getHeight() - margin;
            float w = page.getMediaBox().getWidth();

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                y = text(cs, margin, y, 14, true, "Klean Saúde Integrativa");
                y = text(cs, margin, y, 14, true, "RELATÓRIO DE AGENDAMENTOS");
                y = text(cs, margin, y, 10, false, "Período: " + ini.format(D) + " até " + fim.format(D));
                if (profissionalFiltro != null && !profissionalFiltro.isBlank()) {
                    y = text(cs, margin, y, 10, false, "Profissional: " + profissionalFiltro);
                }
                y = text(cs, margin, y, 10, false, "Gerado em: " + LocalDateTime.now().format(DH));

                y -= 6;
                line(cs, margin, w - margin, y);
                y -= 18;

                float xData = margin;
                float xHora = margin + 85;
                float xProf = margin + 165;
                float xSala = margin + 345;
                float xPac  = margin + 420;

                tableHeader(cs, xData, xHora, xProf, xSala, xPac, y,
                        "DATA", "HORA", "PROFISSIONAL", "SALA", "PACIENTE");
                y -= 14;

                for (Agendamento a : itens) {
                    if (y < margin + 60) break;

                    String hora = a.getHoraInicio().format(H) + "-" + a.getHoraFim().format(H);

                    drawRow(cs, xData, xHora, xProf, xSala, xPac, y,
                            a.getData() != null ? a.getData().format(D) : "",
                            hora,
                            cut(nvl(a.getProfissionalNome()), 22),
                            cut(a.getSala() != null ? a.getSala().getDescricao() : "", 10),
                            cut(nvl(a.getPacienteNome()), 18)
                    );
                    y -= 12;
                }
            }

            doc.save(destino);
        }
    }

    private void tableHeader(PDPageContentStream cs, float x1, float x2, float x3, float x4, float x5, float y,
                             String c1, String c2, String c3, String c4, String c5) throws Exception {
        draw(cs, x1, y, 9, true, c1);
        draw(cs, x2, y, 9, true, c2);
        draw(cs, x3, y, 9, true, c3);
        draw(cs, x4, y, 9, true, c4);
        draw(cs, x5, y, 9, true, c5);
        line(cs, x1, 545, y - 4);
    }

    private void drawRow(PDPageContentStream cs, float x1, float x2, float x3, float x4, float x5, float y,
                         String c1, String c2, String c3, String c4, String c5) throws Exception {
        draw(cs, x1, y, 9, false, c1);
        draw(cs, x2, y, 9, false, c2);
        draw(cs, x3, y, 9, false, c3);
        draw(cs, x4, y, 9, false, c4);
        draw(cs, x5, y, 9, false, c5);
    }

    private float text(PDPageContentStream cs, float x, float y, int size, boolean bold, String t) throws Exception {
        draw(cs, x, y, size, bold, t);
        return y - (size + 4);
    }

    private void draw(PDPageContentStream cs, float x, float y, int size, boolean bold, String t) throws Exception {
        cs.beginText();
        cs.setFont(bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, size);
        cs.newLineAtOffset(x, y);
        cs.showText(nvl(t).replace("\n", " ").replace("\r", " "));
        cs.endText();
    }

    private void line(PDPageContentStream cs, float x1, float x2, float y) throws Exception {
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private String cut(String s, int max) {
        String t = nvl(s).replace("\n", " ").replace("\r", " ");
        return t.length() <= max ? t : t.substring(0, Math.max(0, max - 1)) + "…";
    }
}