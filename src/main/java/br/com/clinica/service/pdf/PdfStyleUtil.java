package br.com.clinica.service.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PdfStyleUtil {

    public static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color TEXT = new Color(15, 23, 42);
    public static final Color MUTED = new Color(100, 116, 139);
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color LIGHT_BG = new Color(248, 250, 252);

    private static final DateTimeFormatter DH =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", PT_BR);

    private PdfStyleUtil() {
    }

    public static float drawHeader(
            PDDocument doc,
            PDPageContentStream cs,
            float margin,
            float y,
            String titulo,
            String subtitulo
    ) throws Exception {

        float logoY = y;

        try (InputStream is = PdfStyleUtil.class.getResourceAsStream("/images/logo-klean.png")) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                PDImageXObject pdImg = LosslessFactory.createFromImage(doc, img);

                float maxH = 52f;
                float scale = maxH / pdImg.getHeight();
                float w = pdImg.getWidth() * scale;
                float h = pdImg.getHeight() * scale;

                cs.drawImage(pdImg, margin, logoY - h, w, h);
            }
        } catch (Exception ignored) {
        }

        drawText(cs, margin + 95, y - 8, 16, true, "Clínica Klean", TEXT);
        drawText(cs, margin + 95, y - 26, 10, false, "Saúde Integrativa", MUTED);

        drawText(cs, margin, y - 78, 18, true, titulo, TEXT);

        if (subtitulo != null && !subtitulo.isBlank()) {
            drawText(cs, margin, y - 96, 10, false, subtitulo, MUTED);
        }

        drawText(cs, margin, y - 114, 9, false,
                "Emitido em: " + LocalDateTime.now().format(DH), MUTED);

        return y - 135;
    }

    public static void drawFooter(
            PDPageContentStream cs,
            float margin,
            float pageWidth,
            float y,
            String texto
    ) throws Exception {
        line(cs, margin, pageWidth - margin, y, BORDER);

        drawText(
                cs,
                margin,
                y - 16,
                8,
                false,
                texto == null || texto.isBlank()
                        ? "Documento gerado pelo sistema Clínica Klean."
                        : texto,
                MUTED
        );
    }

    public static void drawSummaryCard(
            PDPageContentStream cs,
            float x,
            float y,
            float width,
            float height,
            String titulo,
            String valor,
            Color destaque
    ) throws Exception {

        cs.setNonStrokingColor(LIGHT_BG);
        cs.addRect(x, y - height, width, height);
        cs.fill();

        cs.setStrokingColor(BORDER);
        cs.addRect(x, y - height, width, height);
        cs.stroke();

        drawText(cs, x + 12, y - 18, 9, true, titulo, MUTED);
        drawText(cs, x + 12, y - 42, 15, true, valor, destaque == null ? TEXT : destaque);
    }

    public static void tableHeader(
            PDPageContentStream cs,
            float x,
            float y,
            float width,
            String[] headers,
            float[] colWidths
    ) throws Exception {

        cs.setNonStrokingColor(new Color(239, 246, 255));
        cs.addRect(x, y - 18, width, 18);
        cs.fill();

        cs.setStrokingColor(BORDER);
        cs.addRect(x, y - 18, width, 18);
        cs.stroke();

        float cx = x + 6;

        for (int i = 0; i < headers.length; i++) {
            drawText(cs, cx, y - 12, 8, true, headers[i], TEXT);
            cx += colWidths[i];
        }
    }

    public static void tableRow(
            PDPageContentStream cs,
            float x,
            float y,
            float width,
            String[] values,
            float[] colWidths,
            boolean zebra
    ) throws Exception {

        if (zebra) {
            cs.setNonStrokingColor(new Color(248, 250, 252));
            cs.addRect(x, y - 18, width, 18);
            cs.fill();
        }

        cs.setStrokingColor(new Color(241, 245, 249));
        cs.addRect(x, y - 18, width, 18);
        cs.stroke();

        float cx = x + 6;

        for (int i = 0; i < values.length; i++) {
            drawText(cs, cx, y - 12, 8, false, cut(values[i], maxChars(colWidths[i])), TEXT);
            cx += colWidths[i];
        }
    }

    public static void drawText(
            PDPageContentStream cs,
            float x,
            float y,
            float size,
            boolean bold,
            String text,
            Color color
    ) throws Exception {
        cs.beginText();
        cs.setNonStrokingColor(color == null ? TEXT : color);
        cs.setFont(bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, size);
        cs.newLineAtOffset(x, y);
        cs.showText(clean(text));
        cs.endText();
    }

    public static void line(
            PDPageContentStream cs,
            float x1,
            float x2,
            float y,
            Color color
    ) throws Exception {
        cs.setStrokingColor(color == null ? BORDER : color);
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
    }

    public static String money(double value) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(value);
    }

    public static String clean(String s) {
        if (s == null) {
            return "";
        }

        return s
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("–", "-")
                .replace("—", "-")
                .replace("•", "-")
                .replace("₂", "2")
                .trim();
    }

    public static String cut(String s, int max) {
        String t = clean(s);

        if (t.length() <= max) {
            return t;
        }

        return t.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static int maxChars(float width) {
        return Math.max(6, (int) (width / 4.6f));
    }
}