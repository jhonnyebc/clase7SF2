package uco.test.api.infraestructura.output.repositorios.estructurados;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.UsuarioEntidadJPA;

public interface UsuarioRepositorioJPA extends JpaRepository<UsuarioEntidadJPA, UUID> {
    Optional<UsuarioEntidadJPA> findByUsuario(String usuario);
}
