package uco.test.api.servicios.casosuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uco.test.api.servicios.output.ports.entidades.GrupoEntidad;
import uco.test.api.servicios.output.ports.entidades.NotaEntidad;
import uco.test.api.servicios.output.ports.repositorios.GrupoRepositorio;
import uco.test.api.servicios.output.ports.repositorios.NotaRepositorio;
import uco.test.api.transversal.excepcion.UcoTestApiException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class ConsultarNotasGrupoCasoUso implements CasoUsoConRetorno<UUID, List<NotaEntidad>> {

    private final GrupoRepositorio grupoRepositorio;
    private final NotaRepositorio notaRepositorio;

    public ConsultarNotasGrupoCasoUso(GrupoRepositorio grupoRepositorio, NotaRepositorio notaRepositorio) {
        this.grupoRepositorio = grupoRepositorio;
        this.notaRepositorio = notaRepositorio;
    }

    @Override
    public List<NotaEntidad> ejecutar(UUID idGrupo) {
        if (grupoRepositorio.consultarPorId(idGrupo) == null) {
            throw new UcoTestApiException("El grupo no existe");
        }
        NotaEntidad filtro = new NotaEntidad(null, new GrupoEntidad(idGrupo, null, null), null, null, null);
        return notaRepositorio.consultarPorFiltro(filtro).stream()
                .sorted(Comparator.comparing(NotaEntidad::getFechaRegistroDesde))
                .toList();
    }
}
