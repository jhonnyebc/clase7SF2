package uco.test.api.infraestructura.output.repositorios.estructurados.JPA;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "docente")
public class DocenteEntidadJPA {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "identificacion", nullable = false)
    private String identificacion;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "apellidos", nullable = false)
    private String apellidos;

    // Correo con el que el docente inicia sesion en Auth0.
    @Column(name = "correo", unique = true)
    private String correo;

    public DocenteEntidadJPA() {
    }

    public DocenteEntidadJPA(UUID id, String identificacion, String nombres, String apellidos) {
        this(id, identificacion, nombres, apellidos, null);
    }

    public DocenteEntidadJPA(UUID id, String identificacion, String nombres, String apellidos, String correo) {
        this.id = id;
        this.identificacion = identificacion;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
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
}
