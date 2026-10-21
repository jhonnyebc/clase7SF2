package uco.test.api.infraestructura.output.repositorios.estructurados.mapper;

import org.springframework.stereotype.Component;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.EstudianteEntidadJPA;
import uco.test.api.servicios.output.ports.entidades.EstudianteEntidad;

@Component
public class EstudianteEntidadMapper implements EntidadMapper<EstudianteEntidad, EstudianteEntidadJPA> {
    @Override
    public EstudianteEntidad convertirAEntidad(EstudianteEntidadJPA entidadJPA) {
        if (entidadJPA == null) {
            return null;
        }
        return new EstudianteEntidad(
                entidadJPA.getId(),
                entidadJPA.getIdentificacion(),
                entidadJPA.getNombres(),
                entidadJPA.getApellidos(),
                entidadJPA.getActivo());
    }

    @Override
    public EstudianteEntidadJPA convertirAEntidadJPA(EstudianteEntidad entidad) {
        if (entidad == null) {
            return null;
        }
        return new EstudianteEntidadJPA(
                entidad.getId(),
                entidad.getIdentificacion(),
                entidad.getNombres(),
                entidad.getApellidos(),
                entidad.getActivo());
    }
}
