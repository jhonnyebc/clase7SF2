package uco.test.api.servicios.input.ports;

import java.util.UUID;
import uco.test.api.servicios.input.dto.response.PromediosGrupoResponse;

public interface ConsultarPromediosGrupoInputPort {
    PromediosGrupoResponse consultar(UUID idGrupo);
}
