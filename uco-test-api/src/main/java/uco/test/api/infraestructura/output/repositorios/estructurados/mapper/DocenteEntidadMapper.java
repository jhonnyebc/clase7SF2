package uco.test.api.infraestructura.output.repositorios.estructurados.mapper;

import org.springframework.stereotype.Component;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.DocenteEntidadJPA;
import uco.test.api.servicios.output.ports.entidades.DocenteEntidad;

@Component
public class DocenteEntidadMapper implements EntidadMapper<DocenteEntidad, DocenteEntidadJPA> {
    @Override
    public DocenteEntidad convertirAEntidad(DocenteEntidadJPA entidadJPA) {
        if (entidadJPA == null) {
            return null;
        }
        return new DocenteEntidad(
                entidadJPA.getId(),
                entidadJPA.getIdentificacion(),
                entidadJPA.getNombres(),
                entidadJPA.getApellidos());
    }

    @Override
    public DocenteEntidadJPA convertirAEntidadJPA(DocenteEntidad entidad) {
        if (entidad == null) {
            return null;
        }
        return new DocenteEntidadJPA(
                entidad.getId(),
                entidad.getIdentificacion(),
                entidad.getNombres(),
                entidad.getApellidos());
    }
}
