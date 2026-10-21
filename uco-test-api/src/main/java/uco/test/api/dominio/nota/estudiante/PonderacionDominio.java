package uco.test.api.dominio.nota.estudiante;

import java.math.BigDecimal;
import java.util.List;

public record PonderacionDominio(BigDecimal total, long sinPorcentaje, boolean valida, String mensaje) {
    public static PonderacionDominio evaluar(List<BigDecimal> porcentajes) {
        var total = porcentajes.stream().filter(p -> p != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        long faltantes = porcentajes.stream().filter(p -> p == null).count();
        boolean rangos = porcentajes.stream().allMatch(p -> p != null && p.signum() > 0 && p.compareTo(BigDecimal.valueOf(100)) <= 0);
        boolean valida = !porcentajes.isEmpty() && rangos && total.compareTo(BigDecimal.valueOf(100)) == 0;
        String mensaje = valida ? "Los porcentajes suman 100 %"
                : porcentajes.isEmpty() ? "Este grupo no tiene evaluaciones"
                : faltantes > 0 ? "Hay evaluaciones sin porcentaje"
                : !rangos ? "Cada porcentaje debe ser mayor que 0 y menor o igual a 100"
                : "Los porcentajes deben sumar 100 %";
        return new PonderacionDominio(total, faltantes, valida, mensaje);
    }
}
