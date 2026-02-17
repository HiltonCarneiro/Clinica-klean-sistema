package br.com.clinica.service;

import br.com.clinica.model.Nota;
import br.com.clinica.model.NotaItem;
import br.com.clinica.model.TipoItemNota;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

public class NotaPdfService {

    private static final DateTimeFormatter DATA_HORA_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Gera um PDF da nota no arquivo destino.
     */
    public void gerarRecibo(Nota nota, File destino) throws IOException {
        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content =
                         new PDPageContentStream(document, page)) {

                float margin = 50f;
                float pageWidth = page.getMediaBox().getWidth();
                float y = page.getMediaBox().getHeight() - margin;
                float startX = margin;

                // LOGO
                y -= 10;
                y = desenharLogo(document, content, startX, y);
                y -= 20;

                // NOME DA CLÍNICA (sem endereço/telefone)
                y = escreverLinha(content, startX, y, 16, true, "Klean Saúde Integrativa");
                y -= 10;

                // TÍTULO E DADOS GERAIS
                y = escreverLinha(content, startX, y, 14, true, "RECIBO / NOTA DE ATENDIMENTO");
                y = escreverLinha(content, startX, y, 10, false,
                        "Data/Hora: " + nota.getDataHora().format(DATA_HORA_FORMATTER));
                y = escreverLinha(content, startX, y, 10, false,
                        "Paciente : " + nota.getPaciente().getNome());
                y = escreverLinha(content, startX, y, 10, false,
                        "Profissional: " + nota.getProfissional().getNome());
                y = escreverLinha(content, startX, y, 10, false,
                        "Forma de pagamento: " + nota.getFormaPagamento());

                y -= 15;
                desenharLinhaHorizontal(content, startX, pageWidth - margin, y);
                y -= 15;

                // CABEÇALHO DA TABELA
                float colItemX = startX;
                float colDescX = startX + 60;
                float colQtdX  = startX + 330;
                float colUnitX = startX + 380;
                float colTotalX= startX + 460;

                escreverTexto(content, colItemX, y, 11, true, "ITEM");
                escreverTexto(content, colDescX, y, 11, true, "DESCRIÇÃO");
                escreverTexto(content, colQtdX,  y, 11, true, "QTD");
                escreverTexto(content, colUnitX, y, 11, true, "VALOR UNIT.");
                escreverTexto(content, colTotalX,y, 11, true, "VALOR TOTAL");
                y -= 12;

                desenharLinhaHorizontal(content, startX, pageWidth - margin, y);
                y -= 14;

                // ITENS
                int index = 1;
                for (NotaItem item : nota.getItens()) {
                    // ITEM: apenas o número (1, 2, 3...)
                    escreverTexto(content, colItemX, y, 10, false, String.valueOf(index));

                    // DESCRIÇÃO: "Produto: X" ou "Procedimento: Y"
                    String tipo = (item.getTipoItem() == TipoItemNota.PRODUTO)
                            ? "Produto" : "Procedimento";
                    String descricaoFinal = tipo + ": " + item.getDescricao();
                    escreverTexto(content, colDescX, y, 10, false, descricaoFinal);

                    // Quantidade / Unitário / Total
                    escreverTexto(content, colQtdX,  y, 10, false,
                            String.format("%.2f", item.getQuantidade()));
                    escreverTexto(content, colUnitX, y, 10, false,
                            String.format("R$ %.2f", item.getValorUnitario()));
                    escreverTexto(content, colTotalX, y, 10, false,
                            String.format("R$ %.2f", item.getValorTotal()));

                    y -= 14;
                    index++;
                }

                y -= 5;
                desenharLinhaHorizontal(content, startX, pageWidth - margin, y);
                y -= 18;

                // TOTAIS
                escreverTexto(content, colUnitX, y, 11, false,
                        String.format("Subtotal: R$ %.2f", nota.getTotalBruto()));
                y -= 14;
                escreverTexto(content, colUnitX, y, 12, true,
                        String.format("TOTAL: R$ %.2f", nota.getTotalLiquido()));
                y -= 24;

                // OBSERVAÇÕES
                if (nota.getObservacao() != null && !nota.getObservacao().isBlank()) {
                    y = escreverLinha(content, startX, y, 11, true, "Observações:");
                    y = escreverLinha(content, startX, y, 10, false, nota.getObservacao());
                    y -= 10;
                }

                // ASSINATURA
                y -= 20;
                escreverTexto(content, startX, y, 10, false,
                        "__________________________________________");
                y -= 12;
                escreverTexto(content, startX, y, 10, false,
                        "Assinatura do responsável");
            }

            document.save(destino);
        }
    }

    // HELPERS

    private float desenharLogo(PDDocument document,
                               PDPageContentStream content,
                               float x, float y) throws IOException {
        try (InputStream is =
                     getClass().getResourceAsStream("/images/logo-klean.png")) {

            if (is == null) {
                return y;
            }

            BufferedImage bufferedImage = ImageIO.read(is);
            PDImageXObject pdImage =
                    LosslessFactory.createFromImage(document, bufferedImage);

            float maxHeight = 70f;
            float scale = maxHeight / pdImage.getHeight();
            float logoWidth = pdImage.getWidth() * scale;
            float logoHeight = pdImage.getHeight() * scale;

            content.drawImage(pdImage, x, y - logoHeight, logoWidth, logoHeight);

            return y - logoHeight; // posição depois da logo
        } catch (Exception e) {
            // se der erro, ignora a logo e segue
            return y;
        }
    }

    private float escreverLinha(PDPageContentStream content,
                                float x, float y,
                                float fontSize,
                                boolean bold,
                                String texto) throws IOException {
        escreverTexto(content, x, y, fontSize, bold, texto);
        return y - (fontSize + 4);
    }

    private void escreverTexto(PDPageContentStream content,
                               float x, float y,
                               float fontSize,
                               boolean bold,
                               String texto) throws IOException {
        content.beginText();
        content.setFont(bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA,
                fontSize);
        content.newLineAtOffset(x, y);
        content.showText(removerQuebras(texto));
        content.endText();
    }

    private void desenharLinhaHorizontal(PDPageContentStream content,
                                         float xInicio,
                                         float xFim,
                                         float y) throws IOException {
        content.moveTo(xInicio, y);
        content.lineTo(xFim, y);
        content.stroke();
    }

    private String removerQuebras(String s) {
        if (s == null) return "";
        return s.replace("\r", " ").replace("\n", " ");
    }
}
