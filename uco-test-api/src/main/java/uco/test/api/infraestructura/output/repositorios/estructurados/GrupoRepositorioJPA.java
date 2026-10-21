package uco.test.api.infraestructura.output.repositorios.estructurados;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.GrupoEntidadJPA;

import java.util.UUID;

@Repository
public interface GrupoRepositorioJPA extends JpaRepository<GrupoEntidadJPA, UUID> {
}
