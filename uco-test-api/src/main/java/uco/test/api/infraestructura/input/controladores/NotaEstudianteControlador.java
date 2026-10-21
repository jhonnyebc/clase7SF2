package uco.test.api.infraestructura.input.controladores;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uco.test.api.servicios.input.dto.request.RegistrarNotaEstudianteRequest;
import uco.test.api.servicios.input.dto.response.RegistrarNotaEstudianteResponse;
import uco.test.api.servicios.input.ports.RegistrarNotaEstudianteInputPort;

@RestController
@RequestMapping("/api/v1/notas-estudiantes")
public class NotaEstudianteControlador {

    private final RegistrarNotaEstudianteInputPort registrarNotaEstudianteInputPort;

    public NotaEstudianteControlador(RegistrarNotaEstudianteInputPort registrarNotaEstudianteInputPort) {
        this.registrarNotaEstudianteInputPort = registrarNotaEstudianteInputPort;
    }

    @PreAuthorize("#request != null and (#request.idNota == null or @permisos.nota(#request.idNota))")
    @PostMapping
    public ResponseEntity<RegistrarNotaEstudianteResponse> registrar(@RequestBody RegistrarNotaEstudianteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrarNotaEstudianteInputPort.registrar(request));
    }
}

