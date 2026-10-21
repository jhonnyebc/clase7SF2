package uco.test.api.servicios.output.ports.repositorios;

import uco.test.api.servicios.output.ports.entidades.EstudianteEntidad;

import java.util.UUID;

public interface EstudianteRepositorio extends CrearRepositorio<EstudianteEntidad>, ModificarRepositorio<UUID, EstudianteEntidad>,
        EliminarRepositorio<UUID>, ConsultarRepositorios<UUID, EstudianteEntidad> {
}
