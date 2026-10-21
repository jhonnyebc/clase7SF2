package uco.test.api.infraestructura.output.repositorios.estructurados.JPA;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "estudiante")
public class EstudianteEntidadJPA {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "identificacion", nullable = false)
    private String identificacion;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "apellidos", nullable = false)
    private String apellidos;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    public EstudianteEntidadJPA() {
    }

    public EstudianteEntidadJPA(UUID id, String identificacion, String nombres, String apellidos, Boolean activo) {
        this.id = id;
        this.identificacion = identificacion;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.activo = activo;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
