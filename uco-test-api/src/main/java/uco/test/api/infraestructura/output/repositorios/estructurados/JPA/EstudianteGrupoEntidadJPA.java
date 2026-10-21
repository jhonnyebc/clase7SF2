package uco.test.api.infraestructura.output.repositorios.estructurados.JPA;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "estudiante_grupo")
public class EstudianteGrupoEntidadJPA {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private EstudianteEntidadJPA estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private GrupoEntidadJPA grupo;

    public EstudianteGrupoEntidadJPA() {
    }

    public EstudianteGrupoEntidadJPA(UUID id, EstudianteEntidadJPA estudiante, GrupoEntidadJPA grupo) {
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

    public EstudianteEntidadJPA getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(EstudianteEntidadJPA estudiante) {
        this.estudiante = estudiante;
    }

    public GrupoEntidadJPA getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoEntidadJPA grupo) {
        this.grupo = grupo;
    }
}
