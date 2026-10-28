package uco.test.api.infraestructura.input.controladores;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import uco.test.api.infraestructura.input.configuracion.PermisosDocente;
import uco.test.api.servicios.output.ports.repositorios.DocenteRepositorio;
import uco.test.api.servicios.input.ports.ConsultarGruposDocenteInputPort;
import uco.test.api.servicios.input.dto.response.GrupoResponse;
import java.util.List;

/**
 * El login lo hace Auth0 desde Angular. Aqui solo se consulta quien es el docente del token.
 */
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
    @GetMapping("/auth/me")
    public Map<String, Object> actual() {
        var docente = docentes.consultarPorId(permisos.docenteActual());
        var respuesta = new HashMap<String, Object>();
        respuesta.put("idDocente", docente.getId());
        respuesta.put("nombres", docente.getNombres());
        respuesta.put("apellidos", docente.getApellidos());
        respuesta.put("correo", docente.getCorreo());
        return respuesta;
    }
    @GetMapping("/mis-grupos")
    public List<GrupoResponse> grupos() { return grupos.consultar(permisos.docenteActual()); }
}
