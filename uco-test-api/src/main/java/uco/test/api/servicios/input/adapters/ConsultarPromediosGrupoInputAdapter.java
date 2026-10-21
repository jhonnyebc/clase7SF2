package uco.test.api.servicios.input.adapters;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.stereotype.Component;
import uco.test.api.servicios.casosuso.ConsultarPromediosGrupoCasoUso;
import uco.test.api.servicios.input.ports.ConsultarPromediosGrupoInputPort;
import uco.test.api.servicios.input.dto.response.*;
import uco.test.api.dominio.nota.estudiante.registrarnotaestudiante.NotaDominio;

@Component
public class ConsultarPromediosGrupoInputAdapter implements ConsultarPromediosGrupoInputPort {
    private final ConsultarPromediosGrupoCasoUso casoUso;
    private final NotaDominio notaDominio;
    public ConsultarPromediosGrupoInputAdapter(ConsultarPromediosGrupoCasoUso casoUso, NotaDominio notaDominio) {
        this.casoUso = casoUso; this.notaDominio = notaDominio;
    }
    private BigDecimal presentar(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }
    public PromediosGrupoResponse consultar(UUID idGrupo) {
        var resultado = casoUso.ejecutar(idGrupo);
        var p = resultado.ponderacion();
        var estudiantes = resultado.estudiantes().stream().map(e -> {
            var estudiante = e.estudiante();
            var promedio = e.promedio();
            var detalle = resultado.evaluaciones().stream().map(n -> {
                var valor = e.valores().get(n.getId());
                // El aporte se conserva exacto. Solo el total presentado se redondea.
                var aporte = valor == null || !p.valida() ? null : valor.multiply(n.getPorcentaje()).movePointLeft(2);
                return new PromediosGrupoResponse.Detalle(n.getId(), n.getNombre(), n.getPorcentaje(), valor,
                        aporte, notaDominio.esVigente(n));
            }).toList();
            return new PromediosGrupoResponse.Estudiante(estudiante.getId(), estudiante.getIdentificacion(),
                    estudiante.getNombres(), estudiante.getApellidos(), Boolean.TRUE.equals(estudiante.getActivo()),
                    promedio.estado().name(), presentar(promedio.acumulado()), presentar(promedio.definitiva()),
                    promedio.porcentajeEvaluado(), promedio.registradas(), promedio.pendientes(), detalle);
        }).toList();
        return new PromediosGrupoResponse(new PonderacionResponse(p.total(), p.sinPorcentaje(), p.valida(), p.mensaje()), estudiantes);
    }
}
