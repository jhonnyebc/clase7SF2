package uco.test.api.servicios.input.dto.response;

import java.util.UUID;

public class GrupoResponse {
    private UUID id;
    private String nombre;

    public GrupoResponse(UUID id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
