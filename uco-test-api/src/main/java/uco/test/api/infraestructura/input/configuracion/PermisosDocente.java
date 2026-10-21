package uco.test.api.infraestructura.input.configuracion;

import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uco.test.api.infraestructura.output.repositorios.estructurados.UsuarioRepositorioJPA;
import uco.test.api.servicios.output.ports.repositorios.GrupoRepositorio;
import uco.test.api.servicios.output.ports.repositorios.NotaRepositorio;

@Component("permisos")
public class PermisosDocente implements uco.test.api.servicios.input.ports.SesionDocente {
    private final UsuarioRepositorioJPA usuarios;
    private final GrupoRepositorio grupos;
    private final NotaRepositorio notas;
    public PermisosDocente(UsuarioRepositorioJPA usuarios, GrupoRepositorio grupos, NotaRepositorio notas) {
        this.usuarios = usuarios;
        this.grupos = grupos;
        this.notas = notas;
    }
    public UUID docenteActual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new AccessDeniedException("Debes iniciar sesion");
        return usuarios.findByUsuario(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario no disponible")).getDocente().getId();
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
