package uco.test.api.servicios.output.ports.repositorios;

import java.util.List;

public interface ConsultarRepositorios <ID, E>{
    E consultarPorId (ID id);
    List<E> consultarTodos();
    List<E> consultarPorFiltro(E filtro);
}
