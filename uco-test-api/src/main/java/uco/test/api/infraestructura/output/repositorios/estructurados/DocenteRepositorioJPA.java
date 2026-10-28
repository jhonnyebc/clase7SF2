package uco.test.api.infraestructura.output.repositorios.estructurados;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.DocenteEntidadJPA;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocenteRepositorioJPA extends JpaRepository<DocenteEntidadJPA, UUID> {
    Optional<DocenteEntidadJPA> findByCorreoIgnoreCase(String correo);
}
