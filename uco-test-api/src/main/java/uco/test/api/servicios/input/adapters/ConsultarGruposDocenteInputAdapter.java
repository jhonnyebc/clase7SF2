package uco.test.api.servicios.input.adapters;

import org.springframework.stereotype.Component;
import uco.test.api.servicios.casosuso.CasoUsoConRetorno;
import uco.test.api.servicios.input.dto.response.GrupoResponse;
import uco.test.api.servicios.input.ports.ConsultarGruposDocenteInputPort;
import uco.test.api.servicios.output.ports.entidades.GrupoEntidad;

import java.util.List;
import java.util.UUID;

@Component
public class ConsultarGruposDocenteInputAdapter implements ConsultarGruposDocenteInputPort {

    private final CasoUsoConRetorno<UUID, List<GrupoEntidad>> casoUso;

    public ConsultarGruposDocenteInputAdapter(CasoUsoConRetorno<UUID, List<GrupoEntidad>> casoUso) {
        this.casoUso = casoUso;
    }

    @Override
    public List<GrupoResponse> consultar(UUID idDocente) {
        return casoUso.ejecutar(idDocente).stream()
                .map(grupo -> new GrupoResponse(grupo.getId(), grupo.getNombre()))
                .toList();
    }
}
