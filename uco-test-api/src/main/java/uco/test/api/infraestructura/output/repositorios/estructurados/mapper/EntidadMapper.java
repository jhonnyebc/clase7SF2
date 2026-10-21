package uco.test.api.infraestructura.output.repositorios.estructurados.mapper;

public interface EntidadMapper <E, JPA>{
    E convertirAEntidad(JPA entidadJPA);
    JPA convertirAEntidadJPA(E entidad);
}
