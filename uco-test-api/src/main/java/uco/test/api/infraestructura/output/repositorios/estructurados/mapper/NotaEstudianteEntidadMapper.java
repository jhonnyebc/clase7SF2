package uco.test.api.infraestructura.output.repositorios.estructurados.mapper;

import org.springframework.stereotype.Component;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.NotaEstudianteEntidadJPA;
import uco.test.api.servicios.output.ports.entidades.NotaEstudianteEntidad;

@Component
public class NotaEstudianteEntidadMapper implements EntidadMapper<NotaEstudianteEntidad, NotaEstudianteEntidadJPA> {
    private final EstudianteGrupoEntidadMapper estudianteGrupoEntidadMapper;
    private final NotaEntidadMapper notaEntidadMapper;

    public NotaEstudianteEntidadMapper(EstudianteGrupoEntidadMapper estudianteGrupoEntidadMapper, NotaEntidadMapper notaEntidadMapper) {
        this.estudianteGrupoEntidadMapper = estudianteGrupoEntidadMapper;
        this.notaEntidadMapper = notaEntidadMapper;
    }

    @Override
    public NotaEstudianteEntidad convertirAEntidad(NotaEstudianteEntidadJPA entidadJPA) {
        if (entidadJPA == null) {
            return null;
        }
        return new NotaEstudianteEntidad(
                entidadJPA.getId(),
                estudianteGrupoEntidadMapper.convertirAEntidad(entidadJPA.getEstudiante()),
                notaEntidadMapper.convertirAEntidad(entidadJPA.getNota()),
                entidadJPA.getValor());
    }

    @Override
    public NotaEstudianteEntidadJPA convertirAEntidadJPA(NotaEstudianteEntidad entidad) {
        if (entidad == null) {
            return null;
        }
        return new NotaEstudianteEntidadJPA(
                entidad.getId(),
                estudianteGrupoEntidadMapper.convertirAEntidadJPA(entidad.getEstudiante()),
                notaEntidadMapper.convertirAEntidadJPA(entidad.getNota()),
                entidad.getValor());
    }
}
