package uco.test.api.infraestructura.output.repositorios.estructurados;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.GrupoEntidadJPA;
import uco.test.api.infraestructura.output.repositorios.estructurados.mapper.GrupoEntidadMapper;
import uco.test.api.servicios.output.ports.entidades.GrupoEntidad;
import uco.test.api.servicios.output.ports.repositorios.GrupoRepositorio;

import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class GrupoRepositorioAdapter implements GrupoRepositorio {
    private final GrupoRepositorioJPA grupoRepositorioJPA;
    private final GrupoEntidadMapper grupoEntidadMapper;

    public GrupoRepositorioAdapter(GrupoRepositorioJPA grupoRepositorioJPA, GrupoEntidadMapper grupoEntidadMapper) {
        this.grupoRepositorioJPA = grupoRepositorioJPA;
        this.grupoEntidadMapper = grupoEntidadMapper;
    }

    @Override
    public void crear(GrupoEntidad entidad) {
        grupoRepositorioJPA.save(grupoEntidadMapper.convertirAEntidadJPA(entidad));
    }

    @Override
    public void modificar(UUID id, GrupoEntidad entidad) {
        if (!grupoRepositorioJPA.existsById(id)) {
            throw new IllegalArgumentException("No existe grupo con id " + id);
        }
        entidad.setId(id);
        grupoRepositorioJPA.save(grupoEntidadMapper.convertirAEntidadJPA(entidad));
    }

    @Override
    public void eliminar(UUID id) {
        grupoRepositorioJPA.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public GrupoEntidad consultarPorId(UUID id) {
        return grupoRepositorioJPA.findById(id)
                .map(grupoEntidadMapper::convertirAEntidad)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoEntidad> consultarTodos() {
        return grupoRepositorioJPA.findAll().stream()
                .map(grupoEntidadMapper::convertirAEntidad)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoEntidad> consultarPorFiltro(GrupoEntidad filtro) {
        if (filtro == null) {
            return consultarTodos();
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<GrupoEntidadJPA> ejemplo = Example.of(grupoEntidadMapper.convertirAEntidadJPA(filtro), matcher);
        return grupoRepositorioJPA.findAll(ejemplo).stream()
                .map(grupoEntidadMapper::convertirAEntidad)
                .toList();
    }
}
