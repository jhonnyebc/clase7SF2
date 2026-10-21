package uco.test.api.servicios.output.ports.entidades;

import java.util.UUID;

public class GrupoEntidad {
    private UUID id;
    private String nombre;
    private DocenteEntidad docente;

    public GrupoEntidad() {
    }

    public GrupoEntidad(UUID id, String nombre, DocenteEntidad docente) {
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

    public DocenteEntidad getDocente() {
        return docente;
    }

    public void setDocente(DocenteEntidad docente) {
        this.docente = docente;
    }
}
