package uco.test.api.dominio.nota.estudiante.registrarnotaestudiante;

import org.springframework.stereotype.Component;
import uco.test.api.servicios.output.ports.entidades.DocenteEntidad;
import uco.test.api.servicios.output.ports.entidades.GrupoEntidad;
import uco.test.api.servicios.output.ports.repositorios.DocenteRepositorio;
import uco.test.api.transversal.excepcion.UcoTestApiException;

import java.util.UUID;

@Component
public class DocenteDominio {

    private final DocenteRepositorio docenteRepositorio;

    public DocenteDominio(DocenteRepositorio docenteRepositorio) {
        this.docenteRepositorio = docenteRepositorio;
    }

    public DocenteEntidad validarDocenteExista(UUID id) {
        DocenteEntidad docente = docenteRepositorio.consultarPorId(id);
        if (docente == null) {
            throw new UcoTestApiException("El docente no existe");
        }
        return docente;
    }

    public void validarDocenteSeaDelGrupo(DocenteEntidad docente, GrupoEntidad grupo) {
        if (grupo.getDocente() == null || !docente.getId().equals(grupo.getDocente().getId())) {
            throw new UcoTestApiException("El docente no es el responsable del grupo de la nota");
        }
    }
}
