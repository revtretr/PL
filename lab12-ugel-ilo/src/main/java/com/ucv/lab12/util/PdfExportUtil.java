package com.ucv.lab12.util;

import com.ucv.lab12.model.DeudaDocente;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PdfExportUtil {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final PDRectangle A4_APAISADO =
        new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());

    private static final float MARGEN = 40f;
    private static final float ALTO_FILA = 16f;
    private static final int FILAS_POR_PAGINA = 32;

    private static final float[] ANCHOS = {30, 130, 70, 90, 60, 65, 65, 80, 65, 55};
    private static final String[] ENCABEZADOS = {
        "ID", "Docente", "DNI", "Tipo Deuda", "Monto",
        "F. Deuda", "F. Vence", "Situación", "Estado", "Riesgo"
    };

    private PdfExportUtil() {}

    public static void exportar(List<DeudaDocente> registros, File destino) throws IOException {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(A4_APAISADO);
            documento.addPage(pagina);
            PDPageContentStream cs = new PDPageContentStream(documento, pagina);

            float y = escribirEncabezadoPagina(cs, pagina, registros.size());
            int filasEnPagina = 0;

            for (DeudaDocente d : registros) {
                if (filasEnPagina >= FILAS_POR_PAGINA) {
                    cs.close();
                    pagina = new PDPage(A4_APAISADO);
                    documento.addPage(pagina);
                    cs = new PDPageContentStream(documento, pagina);
                    y = escribirEncabezadoPagina(cs, pagina, registros.size());
                    filasEnPagina = 0;
                }
                y -= ALTO_FILA;
                escribirFila(cs, y, new String[] {
                    String.valueOf(d.getIdDeuda()),
                    safe(d.getNombreDocente()),
                    CryptoUtil.enmascarar(d.getDni()),
                    safe(d.getTipoDeuda()),
                    String.format("S/ %.2f", d.getMonto()),
                    d.getFechaDeuda() != null ? d.getFechaDeuda().format(FMT) : "-",
                    d.getFechaVencimiento() != null ? d.getFechaVencimiento().format(FMT) : "-",
                    safe(d.getSituacionLaboral()),
                    safe(d.getEstado()),
                    safe(d.getNivelRiesgo())
                });
                filasEnPagina++;
            }
            cs.close();
            documento.save(destino);
        }
    }

    private static float escribirEncabezadoPagina(PDPageContentStream cs, PDPage pagina, int total) throws IOException {
        float y = pagina.getMediaBox().getHeight() - MARGEN;

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(MARGEN, y);
        cs.showText("UGEL - ILO | Reporte de Deudas Administrativas de Docentes");
        cs.endText();
        y -= 18;

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 9);
        cs.newLineAtOffset(MARGEN, y);
        cs.showText("Generado: " + LocalDate.now().format(FMT) + "   |   Total de registros: " + total);
        cs.endText();
        y -= 16;

        escribirFila(cs, y, ENCABEZADOS, true);
        y -= 4;
        cs.moveTo(MARGEN, y);
        cs.lineTo(pagina.getMediaBox().getWidth() - MARGEN, y);
        cs.stroke();

        return y;
    }

    private static void escribirFila(PDPageContentStream cs, float y, String[] valores) throws IOException {
        escribirFila(cs, y, valores, false);
    }

    private static void escribirFila(PDPageContentStream cs, float y, String[] valores, boolean negrita) throws IOException {
        float x = MARGEN;
        for (int i = 0; i < valores.length; i++) {
            cs.beginText();
            cs.setFont(negrita ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, 8);
            cs.newLineAtOffset(x, y);
            cs.showText(truncar(valores[i], ANCHOS[i]));
            cs.endText();
            x += ANCHOS[i];
        }
    }

    private static String truncar(String texto, float anchoMax) {
        int maxChars = (int) (anchoMax / 4.3);
        if (texto == null) return "";
        return texto.length() > maxChars ? texto.substring(0, Math.max(0, maxChars - 1)) + "." : texto;
    }

    private static String safe(String s) {
        return s == null ? "-" : s;
    }
}
