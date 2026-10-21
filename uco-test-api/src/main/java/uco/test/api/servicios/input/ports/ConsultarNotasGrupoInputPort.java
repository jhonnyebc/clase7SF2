package uco.test.api.servicios.input.ports;

import uco.test.api.servicios.input.dto.response.NotaResponse;

import java.util.List;
import java.util.UUID;

public interface ConsultarNotasGrupoInputPort {
    List<NotaResponse> consultar(UUID idGrupo);
}
