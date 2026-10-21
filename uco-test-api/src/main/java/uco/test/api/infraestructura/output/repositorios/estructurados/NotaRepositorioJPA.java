package uco.test.api.infraestructura.output.repositorios.estructurados;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.NotaEntidadJPA;

import java.util.UUID;

@Repository
public interface NotaRepositorioJPA extends JpaRepository<NotaEntidadJPA, UUID> {
}
