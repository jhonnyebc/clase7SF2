package uco.test.api.infraestructura.output.repositorios.estructurados.mapper;

import org.springframework.stereotype.Component;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.EstudianteGrupoEntidadJPA;
import uco.test.api.servicios.output.ports.entidades.EstudianteGrupoEntidad;

@Component
public class EstudianteGrupoEntidadMapper implements EntidadMapper<EstudianteGrupoEntidad, EstudianteGrupoEntidadJPA> {
    private final EstudianteEntidadMapper estudianteEntidadMapper;
    private final GrupoEntidadMapper grupoEntidadMapper;

    public EstudianteGrupoEntidadMapper(EstudianteEntidadMapper estudianteEntidadMapper, GrupoEntidadMapper grupoEntidadMapper) {
        this.estudianteEntidadMapper = estudianteEntidadMapper;
        this.grupoEntidadMapper = grupoEntidadMapper;
    }

    @Override
    public EstudianteGrupoEntidad convertirAEntidad(EstudianteGrupoEntidadJPA entidadJPA) {
        if (entidadJPA == null) {
            return null;
        }
        return new EstudianteGrupoEntidad(
                entidadJPA.getId(),
                estudianteEntidadMapper.convertirAEntidad(entidadJPA.getEstudiante()),
                grupoEntidadMapper.convertirAEntidad(entidadJPA.getGrupo()));
    }

    @Override
    public EstudianteGrupoEntidadJPA convertirAEntidadJPA(EstudianteGrupoEntidad entidad) {
        if (entidad == null) {
            return null;
        }
        return new EstudianteGrupoEntidadJPA(
                entidad.getId(),
                estudianteEntidadMapper.convertirAEntidadJPA(entidad.getEstudiante()),
                grupoEntidadMapper.convertirAEntidadJPA(entidad.getGrupo()));
    }
}
