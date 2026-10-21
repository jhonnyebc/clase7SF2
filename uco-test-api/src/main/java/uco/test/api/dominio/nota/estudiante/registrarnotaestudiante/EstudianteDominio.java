package uco.test.api.dominio.nota.estudiante.registrarnotaestudiante;

import org.springframework.stereotype.Component;
import uco.test.api.servicios.output.ports.entidades.EstudianteEntidad;
import uco.test.api.servicios.output.ports.entidades.EstudianteGrupoEntidad;
import uco.test.api.servicios.output.ports.entidades.GrupoEntidad;
import uco.test.api.servicios.output.ports.repositorios.EstudianteGrupoRepositorio;
import uco.test.api.servicios.output.ports.repositorios.EstudianteRepositorio;
import uco.test.api.transversal.excepcion.UcoTestApiException;

import java.util.List;
import java.util.UUID;

@Component
public class EstudianteDominio {

    private final EstudianteRepositorio estudianteRepositorio;
    private final EstudianteGrupoRepositorio estudianteGrupoRepositorio;

    public EstudianteDominio(EstudianteRepositorio estudianteRepositorio, EstudianteGrupoRepositorio estudianteGrupoRepositorio) {
        this.estudianteRepositorio = estudianteRepositorio;
        this.estudianteGrupoRepositorio = estudianteGrupoRepositorio;
    }

    public EstudianteEntidad validarEstudianteExista(UUID id) {
        EstudianteEntidad estudiante = estudianteRepositorio.consultarPorId(id);
        if (estudiante == null) {
            throw new UcoTestApiException("El estudiante no existe");
        }
        return estudiante;
    }

    public void validarEstudianteActivo(EstudianteEntidad estudiante) {
        if (!Boolean.TRUE.equals(estudiante.getActivo())) {
            throw new UcoTestApiException("El estudiante no esta activo");
        }
    }

    public EstudianteGrupoEntidad validarEstudiantePertenezcaAlGrupo(UUID idEstudiante, GrupoEntidad grupo) {
        EstudianteGrupoEntidad filtro = new EstudianteGrupoEntidad(null, new EstudianteEntidad(idEstudiante, null, null, null, null), new GrupoEntidad(grupo.getId(), null, null));
        List<EstudianteGrupoEntidad> resultado = estudianteGrupoRepositorio.consultarPorFiltro(filtro);
        if (resultado.isEmpty()) {
            throw new UcoTestApiException("El estudiante no pertenece al grupo de la nota");
        }
        return resultado.getFirst();
    }
}
