package uco.test.api.infraestructura.input.controladores;

import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import uco.test.api.infraestructura.input.configuracion.PermisosDocente;
import uco.test.api.servicios.output.ports.repositorios.DocenteRepositorio;
import uco.test.api.servicios.input.ports.ConsultarGruposDocenteInputPort;
import uco.test.api.servicios.input.dto.response.GrupoResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AutenticacionControlador {
    private final PermisosDocente permisos;
    private final DocenteRepositorio docentes;
    private final ConsultarGruposDocenteInputPort grupos;
    public AutenticacionControlador(PermisosDocente permisos, DocenteRepositorio docentes, ConsultarGruposDocenteInputPort grupos) {
        this.permisos = permisos;
        this.docentes = docentes;
        this.grupos = grupos;
    }
    @GetMapping("/auth/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }
    @GetMapping("/auth/me")
    public Map<String, Object> actual() {
        var docente = docentes.consultarPorId(permisos.docenteActual());
        return Map.of("idDocente", docente.getId(), "nombres", docente.getNombres(), "apellidos", docente.getApellidos());
    }
    @GetMapping("/mis-grupos")
    public List<GrupoResponse> grupos() { return grupos.consultar(permisos.docenteActual()); }
}
