package uco.test.api.servicios.output.ports.repositorios;

import uco.test.api.servicios.output.ports.entidades.NotaEntidad;

import java.util.UUID;

public interface NotaRepositorio extends CrearRepositorio<NotaEntidad>, ModificarRepositorio<UUID, NotaEntidad>,
        EliminarRepositorio<UUID>, ConsultarRepositorios<UUID, NotaEntidad> {
}
