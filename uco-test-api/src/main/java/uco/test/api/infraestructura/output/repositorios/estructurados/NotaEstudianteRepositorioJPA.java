package uco.test.api.infraestructura.output.repositorios.estructurados;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.NotaEstudianteEntidadJPA;

import java.util.UUID;

@Repository
public interface NotaEstudianteRepositorioJPA extends JpaRepository<NotaEstudianteEntidadJPA, UUID> {
}
