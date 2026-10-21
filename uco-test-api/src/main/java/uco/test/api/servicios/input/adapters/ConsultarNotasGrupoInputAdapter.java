package uco.test.api.servicios.input.adapters;

import org.springframework.stereotype.Component;
import uco.test.api.dominio.nota.estudiante.registrarnotaestudiante.NotaDominio;
import uco.test.api.servicios.casosuso.CasoUsoConRetorno;
import uco.test.api.servicios.input.dto.response.NotaResponse;
import uco.test.api.servicios.input.ports.ConsultarNotasGrupoInputPort;
import uco.test.api.servicios.output.ports.entidades.NotaEntidad;

import java.util.List;
import java.util.UUID;

@Component
public class ConsultarNotasGrupoInputAdapter implements ConsultarNotasGrupoInputPort {

    private final CasoUsoConRetorno<UUID, List<NotaEntidad>> casoUso;
    private final NotaDominio notaDominio;

    public ConsultarNotasGrupoInputAdapter(CasoUsoConRetorno<UUID, List<NotaEntidad>> casoUso, NotaDominio notaDominio) {
        this.casoUso = casoUso;
        this.notaDominio = notaDominio;
    }

    @Override
    public List<NotaResponse> consultar(UUID idGrupo) {
        return casoUso.ejecutar(idGrupo).stream()
                .map(nota -> new NotaResponse(nota.getId(), nota.getNombre(), nota.getFechaRegistroDesde(),
                        nota.getFechaRegistroHasta(), notaDominio.esVigente(nota), nota.getPorcentaje()))
                .toList();
    }
}
