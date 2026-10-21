package uco.test.api.servicios.input.ports;

import uco.test.api.servicios.input.dto.response.EstudianteNotaResponse;

import java.util.List;
import java.util.UUID;

public interface ConsultarEstudiantesNotaInputPort {
    List<EstudianteNotaResponse> consultar(UUID idNota);
}
