package uco.test.api.infraestructura.output.repositorios.estructurados.mapper;

import org.springframework.stereotype.Component;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.NotaEntidadJPA;
import uco.test.api.servicios.output.ports.entidades.NotaEntidad;

@Component
public class NotaEntidadMapper implements EntidadMapper<NotaEntidad, NotaEntidadJPA> {
    private final GrupoEntidadMapper grupoEntidadMapper;

    public NotaEntidadMapper(GrupoEntidadMapper grupoEntidadMapper) {
        this.grupoEntidadMapper = grupoEntidadMapper;
    }

    @Override
    public NotaEntidad convertirAEntidad(NotaEntidadJPA entidadJPA) {
        if (entidadJPA == null) {
            return null;
        }
        return new NotaEntidad(
                entidadJPA.getId(),
                grupoEntidadMapper.convertirAEntidad(entidadJPA.getGrupo()),
                entidadJPA.getNombre(),
                entidadJPA.getFechaRegistroDesde(),
                entidadJPA.getFechaRegistroHasta(), entidadJPA.getPorcentaje());
    }

    @Override
    public NotaEntidadJPA convertirAEntidadJPA(NotaEntidad entidad) {
        if (entidad == null) {
            return null;
        }
        return new NotaEntidadJPA(
                entidad.getId(),
                grupoEntidadMapper.convertirAEntidadJPA(entidad.getGrupo()),
                entidad.getNombre(),
                entidad.getFechaRegistroDesde(),
                entidad.getFechaRegistroHasta(), entidad.getPorcentaje());
    }
}
