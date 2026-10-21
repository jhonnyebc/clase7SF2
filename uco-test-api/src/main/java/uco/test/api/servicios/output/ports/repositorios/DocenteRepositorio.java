package uco.test.api.servicios.output.ports.repositorios;

import uco.test.api.servicios.output.ports.entidades.DocenteEntidad;

import java.util.UUID;

public interface DocenteRepositorio extends CrearRepositorio<DocenteEntidad>, ModificarRepositorio<UUID, DocenteEntidad>,
        EliminarRepositorio<UUID>, ConsultarRepositorios<UUID, DocenteEntidad> {
}
