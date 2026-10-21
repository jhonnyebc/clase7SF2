package uco.test.api.servicios.casosuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uco.test.api.dominio.nota.estudiante.consultarestudiantesnota.EstudianteNotaDominio;
import uco.test.api.dominio.nota.estudiante.registrarnotaestudiante.NotaDominio;
import uco.test.api.servicios.output.ports.entidades.EstudianteGrupoEntidad;
import uco.test.api.servicios.output.ports.entidades.GrupoEntidad;
import uco.test.api.servicios.output.ports.entidades.NotaEntidad;
import uco.test.api.servicios.output.ports.entidades.NotaEstudianteEntidad;
import uco.test.api.servicios.output.ports.repositorios.EstudianteGrupoRepositorio;
import uco.test.api.servicios.output.ports.repositorios.NotaEstudianteRepositorio;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class ConsultarEstudiantesNotaCasoUso implements CasoUsoConRetorno<UUID, List<EstudianteNotaDominio>> {

    private final NotaDominio notaDominio;
    private final EstudianteGrupoRepositorio estudianteGrupoRepositorio;
    private final NotaEstudianteRepositorio notaEstudianteRepositorio;

    public ConsultarEstudiantesNotaCasoUso(NotaDominio notaDominio, EstudianteGrupoRepositorio estudianteGrupoRepositorio,
                                           NotaEstudianteRepositorio notaEstudianteRepositorio) {
        this.notaDominio = notaDominio;
        this.estudianteGrupoRepositorio = estudianteGrupoRepositorio;
        this.notaEstudianteRepositorio = notaEstudianteRepositorio;
    }

    @Override
    public List<EstudianteNotaDominio> ejecutar(UUID idNota) {
        NotaEntidad nota = notaDominio.validarNotaExista(idNota);

        EstudianteGrupoEntidad filtroGrupo = new EstudianteGrupoEntidad(null, null, new GrupoEntidad(nota.getGrupo().getId(), null, null));
        List<EstudianteGrupoEntidad> estudiantesGrupo = estudianteGrupoRepositorio.consultarPorFiltro(filtroGrupo);

        NotaEstudianteEntidad filtroNota = new NotaEstudianteEntidad(null, null, new NotaEntidad(idNota, null, null, null, null), null);
        Map<UUID, BigDecimal> valoresPorEstudianteGrupo = notaEstudianteRepositorio.consultarPorFiltro(filtroNota).stream()
                .collect(Collectors.toMap(notaEstudiante -> notaEstudiante.getEstudiante().getId(), NotaEstudianteEntidad::getValor));

        return estudiantesGrupo.stream()
                .map(estudianteGrupo -> new EstudianteNotaDominio(estudianteGrupo.getEstudiante(), valoresPorEstudianteGrupo.get(estudianteGrupo.getId())))
                .sorted(Comparator.comparing((EstudianteNotaDominio e) -> e.getEstudiante().getApellidos())
                        .thenComparing(e -> e.getEstudiante().getNombres()))
                .toList();
    }
}
