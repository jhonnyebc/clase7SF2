package uco.test.api.infraestructura.output.repositorios.estructurados;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.EstudianteGrupoEntidadJPA;
import uco.test.api.infraestructura.output.repositorios.estructurados.mapper.EstudianteGrupoEntidadMapper;
import uco.test.api.servicios.output.ports.entidades.EstudianteGrupoEntidad;
import uco.test.api.servicios.output.ports.repositorios.EstudianteGrupoRepositorio;

import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class EstudianteGrupoRepositorioAdapter implements EstudianteGrupoRepositorio {
    private final EstudianteGrupoRepositorioJPA estudianteGrupoRepositorioJPA;
    private final EstudianteGrupoEntidadMapper estudianteGrupoEntidadMapper;

    public EstudianteGrupoRepositorioAdapter(EstudianteGrupoRepositorioJPA estudianteGrupoRepositorioJPA, EstudianteGrupoEntidadMapper estudianteGrupoEntidadMapper) {
        this.estudianteGrupoRepositorioJPA = estudianteGrupoRepositorioJPA;
        this.estudianteGrupoEntidadMapper = estudianteGrupoEntidadMapper;
    }

    @Override
    public void crear(EstudianteGrupoEntidad entidad) {
        estudianteGrupoRepositorioJPA.save(estudianteGrupoEntidadMapper.convertirAEntidadJPA(entidad));
    }

    @Override
    public void modificar(UUID id, EstudianteGrupoEntidad entidad) {
        if (!estudianteGrupoRepositorioJPA.existsById(id)) {
            throw new IllegalArgumentException("No existe estudianteGrupo con id " + id);
        }
        entidad.setId(id);
        estudianteGrupoRepositorioJPA.save(estudianteGrupoEntidadMapper.convertirAEntidadJPA(entidad));
    }

    @Override
    public void eliminar(UUID id) {
        estudianteGrupoRepositorioJPA.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteGrupoEntidad consultarPorId(UUID id) {
        return estudianteGrupoRepositorioJPA.findById(id)
                .map(estudianteGrupoEntidadMapper::convertirAEntidad)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteGrupoEntidad> consultarTodos() {
        return estudianteGrupoRepositorioJPA.findAll().stream()
                .map(estudianteGrupoEntidadMapper::convertirAEntidad)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteGrupoEntidad> consultarPorFiltro(EstudianteGrupoEntidad filtro) {
        if (filtro == null) {
            return consultarTodos();
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<EstudianteGrupoEntidadJPA> ejemplo = Example.of(estudianteGrupoEntidadMapper.convertirAEntidadJPA(filtro), matcher);
        return estudianteGrupoRepositorioJPA.findAll(ejemplo).stream()
                .map(estudianteGrupoEntidadMapper::convertirAEntidad)
                .toList();
    }
}
