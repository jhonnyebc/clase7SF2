package uco.test.api.infraestructura.output.repositorios.estructurados;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.EstudianteEntidadJPA;
import uco.test.api.infraestructura.output.repositorios.estructurados.mapper.EstudianteEntidadMapper;
import uco.test.api.servicios.output.ports.entidades.EstudianteEntidad;
import uco.test.api.servicios.output.ports.repositorios.EstudianteRepositorio;

import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class EstudianteRepositorioAdapter implements EstudianteRepositorio {
    private final EstudianteRepositorioJPA estudianteRepositorioJPA;
    private final EstudianteEntidadMapper estudianteEntidadMapper;

    public EstudianteRepositorioAdapter(EstudianteRepositorioJPA estudianteRepositorioJPA, EstudianteEntidadMapper estudianteEntidadMapper) {
        this.estudianteRepositorioJPA = estudianteRepositorioJPA;
        this.estudianteEntidadMapper = estudianteEntidadMapper;
    }

    @Override
    public void crear(EstudianteEntidad entidad) {
        estudianteRepositorioJPA.save(estudianteEntidadMapper.convertirAEntidadJPA(entidad));
    }

    @Override
    public void modificar(UUID id, EstudianteEntidad entidad) {
        if (!estudianteRepositorioJPA.existsById(id)) {
            throw new IllegalArgumentException("No existe estudiante con id " + id);
        }
        entidad.setId(id);
        estudianteRepositorioJPA.save(estudianteEntidadMapper.convertirAEntidadJPA(entidad));
    }

    @Override
    public void eliminar(UUID id) {
        estudianteRepositorioJPA.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteEntidad consultarPorId(UUID id) {
        return estudianteRepositorioJPA.findById(id)
                .map(estudianteEntidadMapper::convertirAEntidad)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteEntidad> consultarTodos() {
        return estudianteRepositorioJPA.findAll().stream()
                .map(estudianteEntidadMapper::convertirAEntidad)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteEntidad> consultarPorFiltro(EstudianteEntidad filtro) {
        if (filtro == null) {
            return consultarTodos();
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<EstudianteEntidadJPA> ejemplo = Example.of(estudianteEntidadMapper.convertirAEntidadJPA(filtro), matcher);
        return estudianteRepositorioJPA.findAll(ejemplo).stream()
                .map(estudianteEntidadMapper::convertirAEntidad)
                .toList();
    }
}
