package br.com.clinica.service;

import br.com.clinica.dao.NotaDAO;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RelatorioNotasPdfService {

    private static final Locale PT = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT);
    private static final DateTimeFormatter DH = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", PT);

    public void gerar(LocalDate ini, LocalDate fim,
                      String filtroPaciente, String filtroProf, String filtroForma,
                      List<NotaDAO.NotaResumo> itens,
                      File destino) throws Exception {

        double total = itens.stream().mapToDouble(NotaDAO.NotaResumo::getTotalLiquido).sum();

        Map<String, Double> porForma = new LinkedHashMap<>();
        for (NotaDAO.NotaResumo n : itens) {
            String forma = n.getFormaPagamento() == null ? "SEM FORMA" : n.getFormaPagamento();
            porForma.put(forma, porForma.getOrDefault(forma, 0.0) + n.getTotalLiquido());
        }

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 50f;
            float y = page.getMediaBox().getHeight() - margin;
            float w = page.getMediaBox().getWidth();

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                y = text(cs, margin, y, 14, true, "Klean Saúde Integrativa");
                y = text(cs, margin, y, 14, true, "RELATÓRIO DE NOTAS");
                y = text(cs, margin, y, 10, false, "Período: " + ini.format(D) + " até " + fim.format(D));
                if (filtroPaciente != null && !filtroPaciente.isBlank()) y = text(cs, margin, y, 10, false, "Paciente contém: " + filtroPaciente);
                if (filtroProf != null && !filtroProf.isBlank()) y = text(cs, margin, y, 10, false, "Profissional: " + filtroProf);
                if (filtroForma != null && !filtroForma.isBlank()) y = text(cs, margin, y, 10, false, "Forma: " + filtroForma);
                y = text(cs, margin, y, 10, false, "Gerado em: " + LocalDateTime.now().format(DH));

                y -= 6;
                line(cs, margin, w - margin, y);
                y -= 16;

                y = text(cs, margin, y, 12, true, String.format(PT, "Total no período: R$ %.2f", total));
                y -= 6;

                for (var e : porForma.entrySet()) {
                    y = text(cs, margin, y, 10, false, String.format(PT, "• %s: R$ %.2f", e.getKey(), e.getValue()));
                }

                y -= 8;
                line(cs, margin, w - margin, y);
                y -= 18;

                float xId = margin;
                float xDh = margin + 55;
                float xPac = margin + 175;
                float xProf = margin + 355;
                float xFormaTotal = margin + 470;

                tableHeader(cs, xId, xDh, xPac, xProf, xFormaTotal, y,
                        "ID", "DATA/HORA", "PACIENTE", "PROFISSIONAL", "FORMA / TOTAL");
                y -= 14;

                for (NotaDAO.NotaResumo n : itens) {
                    if (y < margin + 60) break;

                    String formaTotal = String.format(PT, "%s / R$ %.2f",
                            n.getFormaPagamento() == null ? "" : n.getFormaPagamento(),
                            n.getTotalLiquido());

                    drawRow(cs, xId, xDh, xPac, xProf, xFormaTotal, y,
                            String.valueOf(n.getId()),
                            cut(n.getDataHoraFmt(), 16),
                            cut(n.getPacienteNome(), 18),
                            cut(n.getProfissionalNome(), 18),
                            cut(formaTotal, 22)
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
        cs.showText((t == null ? "" : t).replace("\n", " ").replace("\r", " "));
        cs.endText();
    }

    private void line(PDPageContentStream cs, float x1, float x2, float y) throws Exception {
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
    }

    private String cut(String s, int max) {
        String t = (s == null ? "" : s).replace("\n", " ").replace("\r", " ");
        return t.length() <= max ? t : t.substring(0, Math.max(0, max - 1)) + "…";
    }
}