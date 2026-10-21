package uco.test.api.dominio.nota.estudiante;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import uco.test.api.servicios.output.ports.entidades.NotaEntidad;

public final class PromedioPonderadoDominio {
    private PromedioPonderadoDominio() {}
    public enum Estado { SIN_NOTAS, PARCIAL, DEFINITIVA, CONFIGURACION_INVALIDA }
    public record Resultado(Estado estado, BigDecimal acumulado, BigDecimal definitiva,
                            BigDecimal porcentajeEvaluado, int registradas, int pendientes) {}

    public static Resultado calcular(List<NotaEntidad> evaluaciones, Map<UUID, BigDecimal> valores) {
        boolean valida = PonderacionDominio.evaluar(evaluaciones.stream().map(NotaEntidad::getPorcentaje).toList()).valida();
        int registradas = 0;
        BigDecimal acumulado = BigDecimal.ZERO, porcentaje = BigDecimal.ZERO;
        for (var evaluacion : evaluaciones) {
            BigDecimal valor = valores.get(evaluacion.getId());
            if (valor == null) continue;
            registradas++;
            if (valida) {
                acumulado = acumulado.add(valor.multiply(evaluacion.getPorcentaje()).movePointLeft(2));
                porcentaje = porcentaje.add(evaluacion.getPorcentaje());
            }
        }
        int pendientes = evaluaciones.size() - registradas;
        if (!valida) return new Resultado(Estado.CONFIGURACION_INVALIDA, null, null, null, registradas, pendientes);
        if (registradas == 0) return new Resultado(Estado.SIN_NOTAS, null, null, BigDecimal.ZERO, 0, pendientes);
        return new Resultado(pendientes == 0 ? Estado.DEFINITIVA : Estado.PARCIAL, acumulado,
                pendientes == 0 ? acumulado : null, porcentaje, registradas, pendientes);
    }
}
