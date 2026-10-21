package uco.test.api.infraestructura.output.repositorios.estructurados.mapper;

import org.springframework.stereotype.Component;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.GrupoEntidadJPA;
import uco.test.api.servicios.output.ports.entidades.GrupoEntidad;

@Component
public class GrupoEntidadMapper implements EntidadMapper<GrupoEntidad, GrupoEntidadJPA> {
    private final DocenteEntidadMapper docenteEntidadMapper;

    public GrupoEntidadMapper(DocenteEntidadMapper docenteEntidadMapper) {
        this.docenteEntidadMapper = docenteEntidadMapper;
    }

    @Override
    public GrupoEntidad convertirAEntidad(GrupoEntidadJPA entidadJPA) {
        if (entidadJPA == null) {
            return null;
        }
        return new GrupoEntidad(
                entidadJPA.getId(),
                entidadJPA.getNombre(),
                docenteEntidadMapper.convertirAEntidad(entidadJPA.getDocente()));
    }

    @Override
    public GrupoEntidadJPA convertirAEntidadJPA(GrupoEntidad entidad) {
        if (entidad == null) {
            return null;
        }
        return new GrupoEntidadJPA(
                entidad.getId(),
                entidad.getNombre(),
                docenteEntidadMapper.convertirAEntidadJPA(entidad.getDocente()));
    }
}
