package uco.test.api.servicios.output.ports.repositorios;

public interface ModificarRepositorio <ID, E> {
    void modificar(ID id, E entidad);
}
