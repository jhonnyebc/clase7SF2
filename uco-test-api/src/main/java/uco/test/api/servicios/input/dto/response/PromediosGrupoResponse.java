package uco.test.api.servicios.input.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PromediosGrupoResponse(PonderacionResponse ponderacion, List<Estudiante> estudiantes) {
    public record Estudiante(UUID idEstudiante, String identificacion, String nombres, String apellidos,
            boolean activo, String estado, BigDecimal acumulado, BigDecimal definitiva,
            BigDecimal porcentajeEvaluado, int registradas, int pendientes, List<Detalle> evaluaciones) {}
    public record Detalle(UUID idNota, String nombre, BigDecimal porcentaje, BigDecimal valor,
                          BigDecimal aporte, boolean vigente) {}
}
