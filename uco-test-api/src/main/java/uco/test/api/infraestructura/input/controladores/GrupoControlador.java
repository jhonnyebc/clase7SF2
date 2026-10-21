package uco.test.api.infraestructura.input.controladores;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uco.test.api.servicios.input.dto.response.NotaResponse;
import uco.test.api.servicios.input.dto.response.PonderacionResponse;
import uco.test.api.servicios.input.ports.ConsultarNotasGrupoInputPort;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/grupos")
public class GrupoControlador {

    private final ConsultarNotasGrupoInputPort consultarNotasGrupoInputPort;
    private final uco.test.api.servicios.input.ports.ConsultarPromediosGrupoInputPort promedios;

    public GrupoControlador(ConsultarNotasGrupoInputPort consultarNotasGrupoInputPort,
            uco.test.api.servicios.input.ports.ConsultarPromediosGrupoInputPort promedios) {
        this.consultarNotasGrupoInputPort = consultarNotasGrupoInputPort;
        this.promedios = promedios;
    }

    @PreAuthorize("@permisos.grupo(#idGrupo)")
    @GetMapping("/{idGrupo}/promedios")
    public uco.test.api.servicios.input.dto.response.PromediosGrupoResponse consultarPromedios(@PathVariable UUID idGrupo) {
        return promedios.consultar(idGrupo);
    }

    @PreAuthorize("@permisos.grupo(#idGrupo)")
    @GetMapping("/{idGrupo}/notas")
    public List<NotaResponse> consultarNotas(@PathVariable UUID idGrupo) {
        return consultarNotasGrupoInputPort.consultar(idGrupo);
    }

    @PreAuthorize("@permisos.grupo(#idGrupo)")
    @GetMapping("/{idGrupo}/ponderacion")
    public PonderacionResponse consultarPonderacion(@PathVariable UUID idGrupo) {
        return PonderacionResponse.desde(consultarNotasGrupoInputPort.consultar(idGrupo));
    }
}

