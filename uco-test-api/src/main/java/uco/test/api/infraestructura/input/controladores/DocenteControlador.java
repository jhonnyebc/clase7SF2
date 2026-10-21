package uco.test.api.infraestructura.input.controladores;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uco.test.api.servicios.input.dto.response.GrupoResponse;
import uco.test.api.servicios.input.ports.ConsultarGruposDocenteInputPort;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/docentes")
public class DocenteControlador {

    private final ConsultarGruposDocenteInputPort consultarGruposDocenteInputPort;

    public DocenteControlador(ConsultarGruposDocenteInputPort consultarGruposDocenteInputPort) {
        this.consultarGruposDocenteInputPort = consultarGruposDocenteInputPort;
    }

    @PreAuthorize("@permisos.docente(#idDocente)")
    @GetMapping("/{idDocente}/grupos")
    public List<GrupoResponse> consultarGrupos(@PathVariable UUID idDocente) {
        return consultarGruposDocenteInputPort.consultar(idDocente);
    }
}

