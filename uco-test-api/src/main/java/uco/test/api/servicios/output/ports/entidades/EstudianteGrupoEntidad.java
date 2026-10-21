package uco.test.api.servicios.output.ports.entidades;

import java.util.UUID;

public class EstudianteGrupoEntidad {
    private UUID id;

    private EstudianteEntidad estudiante;
    private GrupoEntidad grupo;

    public EstudianteGrupoEntidad() {
    }

    public EstudianteGrupoEntidad(UUID id, EstudianteEntidad estudiante, GrupoEntidad grupo) {
        this.id = id;
        this.estudiante = estudiante;
        this.grupo = grupo;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EstudianteEntidad getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(EstudianteEntidad estudiante) {
        this.estudiante = estudiante;
    }

    public GrupoEntidad getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoEntidad grupo) {
        this.grupo = grupo;
    }
}
