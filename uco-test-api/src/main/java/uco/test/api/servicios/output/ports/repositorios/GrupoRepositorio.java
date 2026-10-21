package uco.test.api.servicios.output.ports.repositorios;

import uco.test.api.servicios.output.ports.entidades.GrupoEntidad;

import java.util.UUID;

public interface GrupoRepositorio extends CrearRepositorio<GrupoEntidad>, ModificarRepositorio<UUID, GrupoEntidad>,
        EliminarRepositorio<UUID>, ConsultarRepositorios<UUID, GrupoEntidad> {
}
