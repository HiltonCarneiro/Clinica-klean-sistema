package br.com.clinica.service;

import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.enums.TipoMovimento;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class RelatorioCaixaPdfService {

    private static final Locale PT = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT);
    private static final DateTimeFormatter DH = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", PT);

    public void gerar(LocalDate ini, LocalDate fim, List<MovimentoCaixa> itens, File destino) throws Exception {

        double entradas = itens.stream().filter(m -> m.getTipo() == TipoMovimento.ENTRADA).mapToDouble(MovimentoCaixa::getValor).sum();
        double saidas   = itens.stream().filter(m -> m.getTipo() == TipoMovimento.SAIDA).mapToDouble(MovimentoCaixa::getValor).sum();
        double saldo = entradas - saidas;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 50f;
            float y = page.getMediaBox().getHeight() - margin;
            float w = page.getMediaBox().getWidth();

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                y = header(doc, cs, margin, y, "RELATÓRIO DO CAIXA", ini, fim);
                y -= 6;
                line(cs, margin, w - margin, y);
                y -= 16;

                y = text(cs, margin, y, 11, true,  String.format(PT, "Entradas: R$ %.2f", entradas));
                y = text(cs, margin, y, 11, true,  String.format(PT, "Saídas:   R$ %.2f", saidas));
                y = text(cs, margin, y, 12, true,  String.format(PT, "Saldo:    R$ %.2f", saldo));

                y -= 6;
                line(cs, margin, w - margin, y);
                y -= 18;

                float xData = margin;
                float xTipo = margin + 85;
                float xForma = margin + 150;
                float xValor = margin + 265;
                float xDesc = margin + 350;

                tableHeader(cs, xData, xTipo, xForma, xValor, xDesc, y,
                        "DATA", "TIPO", "FORMA", "VALOR", "DESCRIÇÃO / PACIENTE");
                y -= 14;

                for (MovimentoCaixa m : itens) {
                    if (y < margin + 60) break; // paginação pode ser adicionada depois, se necessário

                    drawRow(cs, xData, xTipo, xForma, xValor, xDesc, y,
                            m.getData() != null ? m.getData().format(D) : "",
                            m.getTipo() != null ? m.getTipo().name() : "",
                            cut(nvl(m.getFormaPagamento()), 12),
                            String.format(PT, "R$ %.2f", m.getValor()),
                            cut(nvl(m.getDescricao()) + (blank(m.getPacienteNome()) ? "" : " - " + m.getPacienteNome()), 45)
                    );
                    y -= 12;
                }
            }

            doc.save(destino);
        }
    }

    private float header(PDDocument doc, PDPageContentStream cs, float x, float y, String titulo,
                         LocalDate ini, LocalDate fim) throws Exception {
        y = drawLogo(doc, cs, x, y);
        y = text(cs, x, y, 14, true, "Klean Saúde Integrativa");
        y = text(cs, x, y, 14, true, titulo);
        y = text(cs, x, y, 10, false, "Período: " + ini.format(D) + " até " + fim.format(D));
        y = text(cs, x, y, 10, false, "Gerado em: " + LocalDateTime.now().format(DH));
        return y;
    }

    private float drawLogo(PDDocument doc, PDPageContentStream cs, float x, float y) {
        try (InputStream is = getClass().getResourceAsStream("/images/logo-klean.png")) {
            if (is == null) return y;
            BufferedImage img = ImageIO.read(is);
            PDImageXObject pdImg = LosslessFactory.createFromImage(doc, img);

            float maxH = 50f;
            float scale = maxH / pdImg.getHeight();
            float w = pdImg.getWidth() * scale;
            float h = pdImg.getHeight() * scale;
            cs.drawImage(pdImg, x, y - h, w, h);
            return y - h - 6;
        } catch (Exception e) {
            return y;
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

    private boolean blank(String s) { return s == null || s.trim().isEmpty(); }
    private String nvl(String s) { return s == null ? "" : s; }

    private String cut(String s, int max) {
        String t = nvl(s).replace("\n", " ").replace("\r", " ");
        return t.length() <= max ? t : t.substring(0, Math.max(0, max - 1)) + "…";
    }
}