package uco.test.api.infraestructura.output.repositorios.estructurados;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.EstudianteEntidadJPA;

import java.util.UUID;

@Repository
public interface EstudianteRepositorioJPA extends JpaRepository<EstudianteEntidadJPA, UUID> {
}
