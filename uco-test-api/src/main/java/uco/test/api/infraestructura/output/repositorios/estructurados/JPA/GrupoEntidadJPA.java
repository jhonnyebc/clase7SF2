package uco.test.api.infraestructura.output.repositorios.estructurados.JPA;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "grupo")
public class GrupoEntidadJPA {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private DocenteEntidadJPA docente;

    public GrupoEntidadJPA() {
    }

    public GrupoEntidadJPA(UUID id, String nombre, DocenteEntidadJPA docente) {
        this.id = id;
        this.nombre = nombre;
        this.docente = docente;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public DocenteEntidadJPA getDocente() {
        return docente;
    }

    public void setDocente(DocenteEntidadJPA docente) {
        this.docente = docente;
    }
}
