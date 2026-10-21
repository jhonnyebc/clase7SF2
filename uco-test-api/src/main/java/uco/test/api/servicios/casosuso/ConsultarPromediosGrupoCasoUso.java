package uco.test.api.servicios.casosuso;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uco.test.api.dominio.nota.estudiante.PonderacionDominio;
import uco.test.api.dominio.nota.estudiante.PromedioPonderadoDominio;
import uco.test.api.servicios.output.ports.entidades.*;
import uco.test.api.servicios.output.ports.repositorios.*;
import uco.test.api.transversal.excepcion.UcoTestApiException;

@Component
@Transactional(readOnly = true)
public class ConsultarPromediosGrupoCasoUso {
    public record EstudiantePromedio(EstudianteEntidad estudiante, Map<UUID, BigDecimal> valores,
                                    PromedioPonderadoDominio.Resultado promedio) {}
    public record Resultado(PonderacionDominio ponderacion, List<NotaEntidad> evaluaciones, List<EstudiantePromedio> estudiantes) {}
    private final ConsultarNotasGrupoCasoUso consultarNotas;
    private final EstudianteGrupoRepositorio estudiantes;
    private final NotaEstudianteRepositorio registros;
    public ConsultarPromediosGrupoCasoUso(ConsultarNotasGrupoCasoUso consultarNotas,
            EstudianteGrupoRepositorio estudiantes, NotaEstudianteRepositorio registros) {
        this.consultarNotas = consultarNotas; this.estudiantes = estudiantes; this.registros = registros;
    }
    public Resultado ejecutar(UUID idGrupo) {
        var notas = consultarNotas.ejecutar(idGrupo);
        var grupo = new GrupoEntidad(idGrupo, null, null);
        var matriculas = estudiantes.consultarPorFiltro(new EstudianteGrupoEntidad(null, null, grupo));
        var notasGuardadas = registros.consultarPorFiltro(new NotaEstudianteEntidad(null,
                new EstudianteGrupoEntidad(null, null, grupo), null, null));
        var idsNotas = new HashSet<UUID>();
        notas.forEach(n -> idsNotas.add(n.getId()));
        Map<UUID, Map<UUID, BigDecimal>> valores = new HashMap<>();
        for (var registro : notasGuardadas) {
            if (!idsNotas.contains(registro.getNota().getId())) continue;
            var porEstudiante = valores.computeIfAbsent(registro.getEstudiante().getId(), id -> new HashMap<>());
            if (porEstudiante.putIfAbsent(registro.getNota().getId(), registro.getValor()) != null)
                throw new UcoTestApiException("Hay notas duplicadas. Revisa los registros antes de calcular el promedio");
        }
        var resultados = matriculas.stream().map(matricula -> {
            var porEstudiante = valores.getOrDefault(matricula.getId(), Map.of());
            return new EstudiantePromedio(matricula.getEstudiante(), porEstudiante,
                    PromedioPonderadoDominio.calcular(notas, porEstudiante));
        }).sorted(Comparator.comparing((EstudiantePromedio e) -> e.estudiante().getApellidos())
                .thenComparing(e -> e.estudiante().getNombres())).toList();
        return new Resultado(PonderacionDominio.evaluar(notas.stream().map(NotaEntidad::getPorcentaje).toList()), notas, resultados);
    }
}
