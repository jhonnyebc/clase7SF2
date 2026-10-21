package uco.test.api.servicios.output.ports.repositorios;

import uco.test.api.servicios.output.ports.entidades.NotaEstudianteEntidad;

import java.util.UUID;

public interface NotaEstudianteRepositorio extends CrearRepositorio<NotaEstudianteEntidad>, ModificarRepositorio<UUID, NotaEstudianteEntidad>,
        EliminarRepositorio<UUID>, ConsultarRepositorios<UUID, NotaEstudianteEntidad> {
}
