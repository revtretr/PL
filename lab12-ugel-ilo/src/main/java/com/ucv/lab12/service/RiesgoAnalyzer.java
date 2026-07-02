package com.ucv.lab12.service;

import com.ucv.lab12.model.DeudaDocente;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RiesgoAnalyzer {

    private static final double PESO_MONTO   = 0.45;
    private static final double PESO_MORA    = 0.40;
    private static final double PESO_LABORAL = 0.15;

    private static final double MONTO_REFERENCIA_ALTO = 5000.0; // S/ 5000 = score máximo por monto
    private static final int    DIAS_MORA_REFERENCIA_ALTO = 180; // 6 meses = score máximo por mora

    public String calcularRiesgo(DeudaDocente d) {
        return clasificar(calcularScore(d));
    }

    /** Devuelve un puntaje de 0 (sin riesgo) a 100 (riesgo crítico). */
    public double calcularScore(DeudaDocente d) {
        if (d == null) return 0;

        // Si la deuda ya está pagada, el riesgo es nulo.
        if ("Pagada".equalsIgnoreCase(d.getEstado())) return 0;

        double scoreMonto = Math.min(1.0, d.getMonto() / MONTO_REFERENCIA_ALTO) * 100;

        long diasMora = 0;
        if (d.getFechaVencimiento() != null && d.getFechaVencimiento().isBefore(LocalDate.now())) {
            diasMora = ChronoUnit.DAYS.between(d.getFechaVencimiento(), LocalDate.now());
        }
        double scoreMora = Math.min(1.0, diasMora / (double) DIAS_MORA_REFERENCIA_ALTO) * 100;

        double scoreLaboral = puntajeSituacionLaboral(d.getSituacionLaboral());

        double total = (scoreMonto * PESO_MONTO) + (scoreMora * PESO_MORA) + (scoreLaboral * PESO_LABORAL);
        return Math.round(total * 100.0) / 100.0;
    }

    private double puntajeSituacionLaboral(String situacion) {
        if (situacion == null) return 50;
        return switch (situacion.toUpperCase()) {
            case "CESANTE" -> 100;     // mayor riesgo de no pago: ya no percibe haberes
            case "CONTRATADO" -> 65;   // vínculo laboral temporal
            case "ENCARGADO" -> 45;
            case "NOMBRADO" -> 20;     // menor riesgo: descuento por planilla estable
            default -> 50;
        };
    }

    private String clasificar(double score) {
        if (score >= 66) return "ALTO";
        if (score >= 33) return "MEDIO";
        return "BAJO";
    }
}
