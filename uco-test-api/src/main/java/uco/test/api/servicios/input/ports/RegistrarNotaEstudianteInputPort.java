package uco.test.api.servicios.input.ports;

import uco.test.api.servicios.input.dto.request.RegistrarNotaEstudianteRequest;
import uco.test.api.servicios.input.dto.response.RegistrarNotaEstudianteResponse;

public interface RegistrarNotaEstudianteInputPort {
    RegistrarNotaEstudianteResponse registrar(RegistrarNotaEstudianteRequest request);
}
