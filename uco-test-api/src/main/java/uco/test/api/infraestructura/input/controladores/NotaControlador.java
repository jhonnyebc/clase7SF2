package uco.test.api.infraestructura.input.controladores;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uco.test.api.servicios.input.dto.response.EstudianteNotaResponse;
import uco.test.api.servicios.input.ports.ConsultarEstudiantesNotaInputPort;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notas")
public class NotaControlador {

    private final ConsultarEstudiantesNotaInputPort consultarEstudiantesNotaInputPort;

    public NotaControlador(ConsultarEstudiantesNotaInputPort consultarEstudiantesNotaInputPort) {
        this.consultarEstudiantesNotaInputPort = consultarEstudiantesNotaInputPort;
    }

    @PreAuthorize("@permisos.nota(#idNota)")
    @GetMapping("/{idNota}/estudiantes")
    public List<EstudianteNotaResponse> consultarEstudiantes(@PathVariable UUID idNota) {
        return consultarEstudiantesNotaInputPort.consultar(idNota);
    }
}

