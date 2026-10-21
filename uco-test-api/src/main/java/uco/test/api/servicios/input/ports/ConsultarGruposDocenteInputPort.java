package uco.test.api.servicios.input.ports;

import uco.test.api.servicios.input.dto.response.GrupoResponse;

import java.util.List;
import java.util.UUID;

public interface ConsultarGruposDocenteInputPort {
    List<GrupoResponse> consultar(UUID idDocente);
}
