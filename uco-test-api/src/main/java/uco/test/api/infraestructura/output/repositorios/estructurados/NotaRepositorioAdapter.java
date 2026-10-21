package uco.test.api.infraestructura.output.repositorios.estructurados;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.NotaEntidadJPA;
import uco.test.api.infraestructura.output.repositorios.estructurados.mapper.NotaEntidadMapper;
import uco.test.api.servicios.output.ports.entidades.NotaEntidad;
import uco.test.api.servicios.output.ports.repositorios.NotaRepositorio;

import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class NotaRepositorioAdapter implements NotaRepositorio {
    private final NotaRepositorioJPA notaRepositorioJPA;
    private final NotaEntidadMapper notaEntidadMapper;

    public NotaRepositorioAdapter(NotaRepositorioJPA notaRepositorioJPA, NotaEntidadMapper notaEntidadMapper) {
        this.notaRepositorioJPA = notaRepositorioJPA;
        this.notaEntidadMapper = notaEntidadMapper;
    }

    @Override
    public void crear(NotaEntidad entidad) {
        notaRepositorioJPA.save(notaEntidadMapper.convertirAEntidadJPA(entidad));
    }

    @Override
    public void modificar(UUID id, NotaEntidad entidad) {
        if (!notaRepositorioJPA.existsById(id)) {
            throw new IllegalArgumentException("No existe nota con id " + id);
        }
        entidad.setId(id);
        notaRepositorioJPA.save(notaEntidadMapper.convertirAEntidadJPA(entidad));
    }

    @Override
    public void eliminar(UUID id) {
        notaRepositorioJPA.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public NotaEntidad consultarPorId(UUID id) {
        return notaRepositorioJPA.findById(id)
                .map(notaEntidadMapper::convertirAEntidad)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaEntidad> consultarTodos() {
        return notaRepositorioJPA.findAll().stream()
                .map(notaEntidadMapper::convertirAEntidad)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaEntidad> consultarPorFiltro(NotaEntidad filtro) {
        if (filtro == null) {
            return consultarTodos();
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<NotaEntidadJPA> ejemplo = Example.of(notaEntidadMapper.convertirAEntidadJPA(filtro), matcher);
        return notaRepositorioJPA.findAll(ejemplo).stream()
                .map(notaEntidadMapper::convertirAEntidad)
                .toList();
    }
}
