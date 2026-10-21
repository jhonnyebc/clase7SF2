package uco.test.api.servicios.output.ports.repositorios;

import uco.test.api.servicios.output.ports.entidades.EstudianteGrupoEntidad;

import java.util.UUID;

public interface EstudianteGrupoRepositorio extends CrearRepositorio<EstudianteGrupoEntidad>, ModificarRepositorio<UUID, EstudianteGrupoEntidad>,
        EliminarRepositorio<UUID>, ConsultarRepositorios<UUID, EstudianteGrupoEntidad> {
}
