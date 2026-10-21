package uco.test.api.servicios.input.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PonderacionResponse(BigDecimal total, long sinPorcentaje, boolean valida, String mensaje) {
    public static PonderacionResponse desde(List<NotaResponse> notas) {
        var resultado = uco.test.api.dominio.nota.estudiante.PonderacionDominio.evaluar(notas.stream().map(NotaResponse::getPorcentaje).toList());
        return new PonderacionResponse(resultado.total(), resultado.sinPorcentaje(), resultado.valida(), resultado.mensaje());
    }
}
