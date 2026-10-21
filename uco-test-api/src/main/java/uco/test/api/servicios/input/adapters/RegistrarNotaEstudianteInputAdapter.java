package uco.test.api.servicios.input.adapters;

import org.springframework.stereotype.Component;
import uco.test.api.servicios.input.ports.SesionDocente;
import uco.test.api.dominio.nota.estudiante.registrarnotaestudiante.RegistrarNotaEstudianteDominio;
import uco.test.api.servicios.casosuso.CasoUsoConRetorno;
import uco.test.api.servicios.input.dto.request.RegistrarNotaEstudianteRequest;
import uco.test.api.servicios.input.dto.response.RegistrarNotaEstudianteResponse;
import uco.test.api.servicios.input.ports.RegistrarNotaEstudianteInputPort;
import uco.test.api.transversal.excepcion.UcoTestApiException;

import java.util.UUID;

@Component
public class RegistrarNotaEstudianteInputAdapter implements RegistrarNotaEstudianteInputPort {

    private final CasoUsoConRetorno<RegistrarNotaEstudianteDominio, UUID> casoUso;
    private final SesionDocente permisos;

    public RegistrarNotaEstudianteInputAdapter(CasoUsoConRetorno<RegistrarNotaEstudianteDominio, UUID> casoUso, SesionDocente permisos) {
        this.casoUso = casoUso;
        this.permisos = permisos;
    }

    @Override
    public RegistrarNotaEstudianteResponse registrar(RegistrarNotaEstudianteRequest request) {
        if (request == null || request.getIdEstudiante() == null || request.getIdNota() == null
                || request.getValor() == null) {
            throw new UcoTestApiException("idEstudiante, idNota y valor son obligatorios");
        }
        RegistrarNotaEstudianteDominio dominio = new RegistrarNotaEstudianteDominio(
                request.getIdEstudiante(), request.getIdNota(), permisos.docenteActual(), request.getValor());
        UUID id = casoUso.ejecutar(dominio);
        return new RegistrarNotaEstudianteResponse(id, "Nota registrada correctamente");
    }
}
