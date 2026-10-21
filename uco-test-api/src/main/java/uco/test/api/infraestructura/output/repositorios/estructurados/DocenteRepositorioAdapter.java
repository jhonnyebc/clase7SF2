package uco.test.api.infraestructura.output.repositorios.estructurados;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.DocenteEntidadJPA;
import uco.test.api.infraestructura.output.repositorios.estructurados.mapper.DocenteEntidadMapper;
import uco.test.api.servicios.output.ports.entidades.DocenteEntidad;
import uco.test.api.servicios.output.ports.repositorios.DocenteRepositorio;

import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class DocenteRepositorioAdapter implements DocenteRepositorio {
    private final DocenteRepositorioJPA docenteRepositorioJPA;
    private final DocenteEntidadMapper docenteEntidadMapper;

    public DocenteRepositorioAdapter(DocenteRepositorioJPA docenteRepositorioJPA, DocenteEntidadMapper docenteEntidadMapper) {
        this.docenteRepositorioJPA = docenteRepositorioJPA;
        this.docenteEntidadMapper = docenteEntidadMapper;
    }

    @Override
    public void crear(DocenteEntidad entidad) {
        docenteRepositorioJPA.save(docenteEntidadMapper.convertirAEntidadJPA(entidad));
    }

    @Override
    public void modificar(UUID id, DocenteEntidad entidad) {
        if (!docenteRepositorioJPA.existsById(id)) {
            throw new IllegalArgumentException("No existe docente con id " + id);
        }
        entidad.setId(id);
        docenteRepositorioJPA.save(docenteEntidadMapper.convertirAEntidadJPA(entidad));
    }

    @Override
    public void eliminar(UUID id) {
        docenteRepositorioJPA.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DocenteEntidad consultarPorId(UUID id) {
        return docenteRepositorioJPA.findById(id)
                .map(docenteEntidadMapper::convertirAEntidad)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocenteEntidad> consultarTodos() {
        return docenteRepositorioJPA.findAll().stream()
                .map(docenteEntidadMapper::convertirAEntidad)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocenteEntidad> consultarPorFiltro(DocenteEntidad filtro) {
        if (filtro == null) {
            return consultarTodos();
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<DocenteEntidadJPA> ejemplo = Example.of(docenteEntidadMapper.convertirAEntidadJPA(filtro), matcher);
        return docenteRepositorioJPA.findAll(ejemplo).stream()
                .map(docenteEntidadMapper::convertirAEntidad)
                .toList();
    }
}
