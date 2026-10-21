package uco.test.api.servicios.casosuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uco.test.api.dominio.nota.estudiante.registrarnotaestudiante.DocenteDominio;
import uco.test.api.servicios.output.ports.entidades.DocenteEntidad;
import uco.test.api.servicios.output.ports.entidades.GrupoEntidad;
import uco.test.api.servicios.output.ports.repositorios.GrupoRepositorio;

import java.util.List;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class ConsultarGruposDocenteCasoUso implements CasoUsoConRetorno<UUID, List<GrupoEntidad>> {

    private final DocenteDominio docenteDominio;
    private final GrupoRepositorio grupoRepositorio;

    public ConsultarGruposDocenteCasoUso(DocenteDominio docenteDominio, GrupoRepositorio grupoRepositorio) {
        this.docenteDominio = docenteDominio;
        this.grupoRepositorio = grupoRepositorio;
    }

    @Override
    public List<GrupoEntidad> ejecutar(UUID idDocente) {
        docenteDominio.validarDocenteExista(idDocente);
        GrupoEntidad filtro = new GrupoEntidad(null, null, new DocenteEntidad(idDocente, null, null, null));
        return grupoRepositorio.consultarPorFiltro(filtro);
    }
}
