package uco.test.api.servicios.input.adapters;

import org.springframework.stereotype.Component;
import uco.test.api.dominio.nota.estudiante.consultarestudiantesnota.EstudianteNotaDominio;
import uco.test.api.servicios.casosuso.CasoUsoConRetorno;
import uco.test.api.servicios.input.dto.response.EstudianteNotaResponse;
import uco.test.api.servicios.input.ports.ConsultarEstudiantesNotaInputPort;
import uco.test.api.servicios.output.ports.entidades.EstudianteEntidad;

import java.util.List;
import java.util.UUID;

@Component
public class ConsultarEstudiantesNotaInputAdapter implements ConsultarEstudiantesNotaInputPort {

    private final CasoUsoConRetorno<UUID, List<EstudianteNotaDominio>> casoUso;

    public ConsultarEstudiantesNotaInputAdapter(CasoUsoConRetorno<UUID, List<EstudianteNotaDominio>> casoUso) {
        this.casoUso = casoUso;
    }

    @Override
    public List<EstudianteNotaResponse> consultar(UUID idNota) {
        return casoUso.ejecutar(idNota).stream()
                .map(estudianteNota -> {
                    EstudianteEntidad estudiante = estudianteNota.getEstudiante();
                    return new EstudianteNotaResponse(estudiante.getId(), estudiante.getIdentificacion(),
                            estudiante.getNombres(), estudiante.getApellidos(), Boolean.TRUE.equals(estudiante.getActivo()),
                            estudianteNota.getValor(), estudianteNota.isRegistrada());
                })
                .toList();
    }
}
