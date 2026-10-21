package uco.test.api.servicios.input.dto.response;

import java.util.UUID;

public class RegistrarNotaEstudianteResponse {
    private UUID id;
    private String mensaje;

    public RegistrarNotaEstudianteResponse(UUID id, String mensaje) {
        this.id = id;
        this.mensaje = mensaje;
    }

    public UUID getId() {
        return id;
    }

    public String getMensaje() {
        return mensaje;
    }
}
