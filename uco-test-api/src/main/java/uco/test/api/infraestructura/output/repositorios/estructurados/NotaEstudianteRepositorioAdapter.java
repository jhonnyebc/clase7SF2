package uco.test.api.infraestructura.output.repositorios.estructurados;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.NotaEstudianteEntidadJPA;
import uco.test.api.infraestructura.output.repositorios.estructurados.mapper.NotaEstudianteEntidadMapper;
import uco.test.api.servicios.output.ports.entidades.NotaEstudianteEntidad;
import uco.test.api.servicios.output.ports.repositorios.NotaEstudianteRepositorio;

import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class NotaEstudianteRepositorioAdapter implements NotaEstudianteRepositorio {
    private final NotaEstudianteRepositorioJPA notaEstudianteRepositorioJPA;
    private final NotaEstudianteEntidadMapper notaEstudianteEntidadMapper;

    public NotaEstudianteRepositorioAdapter(NotaEstudianteRepositorioJPA notaEstudianteRepositorioJPA, NotaEstudianteEntidadMapper notaEstudianteEntidadMapper) {
        this.notaEstudianteRepositorioJPA = notaEstudianteRepositorioJPA;
        this.notaEstudianteEntidadMapper = notaEstudianteEntidadMapper;
    }

    @Override
    public void crear(NotaEstudianteEntidad entidad) {
        try {
            // Fuerza la inserción dentro de este bloque para traducir solo la unicidad esperada.
            notaEstudianteRepositorioJPA.saveAndFlush(notaEstudianteEntidadMapper.convertirAEntidadJPA(entidad));
        } catch (org.springframework.dao.DataIntegrityViolationException error) {
            for (Throwable causa = error; causa != null; causa = causa.getCause()) {
                if (causa instanceof org.hibernate.exception.ConstraintViolationException violacion
                        && violacion.getConstraintName() != null
                        && violacion.getConstraintName().toLowerCase(java.util.Locale.ROOT)
                            .contains("uk_nota_estudiante_evaluacion")) {
                    throw new uco.test.api.transversal.excepcion.NotaYaRegistradaException();
                }
            }
            throw error;
        }
    }

    @Override
    public void modificar(UUID id, NotaEstudianteEntidad entidad) {
        if (!notaEstudianteRepositorioJPA.existsById(id)) {
            throw new IllegalArgumentException("No existe notaEstudiante con id " + id);
        }
        entidad.setId(id);
        notaEstudianteRepositorioJPA.save(notaEstudianteEntidadMapper.convertirAEntidadJPA(entidad));
    }

    @Override
    public void eliminar(UUID id) {
        notaEstudianteRepositorioJPA.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public NotaEstudianteEntidad consultarPorId(UUID id) {
        return notaEstudianteRepositorioJPA.findById(id)
                .map(notaEstudianteEntidadMapper::convertirAEntidad)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaEstudianteEntidad> consultarTodos() {
        return notaEstudianteRepositorioJPA.findAll().stream()
                .map(notaEstudianteEntidadMapper::convertirAEntidad)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaEstudianteEntidad> consultarPorFiltro(NotaEstudianteEntidad filtro) {
        if (filtro == null) {
            return consultarTodos();
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<NotaEstudianteEntidadJPA> ejemplo = Example.of(notaEstudianteEntidadMapper.convertirAEntidadJPA(filtro), matcher);
        return notaEstudianteRepositorioJPA.findAll(ejemplo).stream()
                .map(notaEstudianteEntidadMapper::convertirAEntidad)
                .toList();
    }
}
