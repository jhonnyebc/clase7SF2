package uco.test.api.servicios.output.ports.entidades;

import java.util.UUID;

public class DocenteEntidad {
    private UUID id;
    private String identificacion;
    private String nombres;
    private String apellidos;

    public DocenteEntidad() {
    }

    public DocenteEntidad(UUID id, String identificacion, String nombres, String apellidos) {
        this.id = id;
        this.identificacion = identificacion;
        this.nombres = nombres;
        this.apellidos = apellidos;
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
