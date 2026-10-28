package uco.test.api.infraestructura.input.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import uco.test.api.infraestructura.output.repositorios.estructurados.DocenteRepositorioJPA;
import uco.test.api.servicios.input.ports.SesionDocente;
import uco.test.api.servicios.output.ports.repositorios.GrupoRepositorio;
import uco.test.api.servicios.output.ports.repositorios.NotaRepositorio;

import java.util.UUID;

/**
 * Identifica al docente a partir del token de Auth0: el correo viaja en un claim propio
 * (lo agrega la Action "Agregar correo al token") y se busca el docente con ese correo exacto.
 */
@Component("permisos")
public class PermisosDocente implements SesionDocente {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PermisosDocente.class);
    private final DocenteRepositorioJPA docentes;
    private final GrupoRepositorio grupos;
    private final NotaRepositorio notas;
    private final String claimCorreo;

    public PermisosDocente(DocenteRepositorioJPA docentes, GrupoRepositorio grupos, NotaRepositorio notas,
                           @Value("${app.auth.claim-correo}") String claimCorreo) {
        this.docentes = docentes;
        this.grupos = grupos;
        this.notas = notas;
        this.claimCorreo = claimCorreo;
    }

    @Override
    public UUID docenteActual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken token)) {
            throw new AccessDeniedException("Debes iniciar sesion");
        }
        String correo = token.getToken().getClaimAsString(claimCorreo);
        if (correo == null || correo.isBlank()) {
            throw new AccessDeniedException(
                    "Auth0 no envio tu correo. Revisa que la Action 'Agregar correo al token' este conectada al login");
        }
        String correoLimpio = correo.trim();
        return docentes.findByCorreoIgnoreCase(correoLimpio)
                .orElseThrow(() -> {
                    log.warn("Inicio de sesion con un correo sin docente asociado: {}", correoLimpio);
                    return new AccessDeniedException(
                            "Tu cuenta (" + correoLimpio + ") no esta asociada a ningun docente");
                })
                .getId();
    }

    public boolean docente(UUID id) { return docenteActual().equals(id); }

    public boolean grupo(UUID id) {
        if (id == null) return false;
        var grupo = grupos.consultarPorId(id);
        return grupo != null && grupo.getDocente() != null && docente(grupo.getDocente().getId());
    }

    public boolean nota(UUID id) {
        if (id == null) return false;
        var nota = notas.consultarPorId(id);
        return nota != null && grupo(nota.getGrupo().getId());
    }
}
