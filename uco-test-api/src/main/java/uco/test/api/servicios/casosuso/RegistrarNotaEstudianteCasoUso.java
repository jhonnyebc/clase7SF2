package uco.test.api.servicios.casosuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uco.test.api.dominio.nota.estudiante.registrarnotaestudiante.DocenteDominio;
import uco.test.api.dominio.nota.estudiante.registrarnotaestudiante.EstudianteDominio;
import uco.test.api.dominio.nota.estudiante.registrarnotaestudiante.NotaDominio;
import uco.test.api.dominio.nota.estudiante.registrarnotaestudiante.RegistrarNotaEstudianteDominio;
import uco.test.api.servicios.output.ports.entidades.DocenteEntidad;
import uco.test.api.servicios.output.ports.entidades.EstudianteEntidad;
import uco.test.api.servicios.output.ports.entidades.EstudianteGrupoEntidad;
import uco.test.api.servicios.output.ports.entidades.NotaEntidad;
import uco.test.api.servicios.output.ports.entidades.NotaEstudianteEntidad;
import uco.test.api.servicios.output.ports.repositorios.NotaEstudianteRepositorio;

import java.util.UUID;

@Component
@Transactional
public class RegistrarNotaEstudianteCasoUso implements CasoUsoConRetorno<RegistrarNotaEstudianteDominio, UUID> {

    private final EstudianteDominio estudianteDominio;
    private final DocenteDominio docenteDominio;
    private final NotaDominio notaDominio;
    private final NotaEstudianteRepositorio notaEstudianteRepositorio;

    public RegistrarNotaEstudianteCasoUso(EstudianteDominio estudianteDominio, DocenteDominio docenteDominio,
                                          NotaDominio notaDominio, NotaEstudianteRepositorio notaEstudianteRepositorio) {
        this.estudianteDominio = estudianteDominio;
        this.docenteDominio = docenteDominio;
        this.notaDominio = notaDominio;
        this.notaEstudianteRepositorio = notaEstudianteRepositorio;
    }

    @Override
    public UUID ejecutar(RegistrarNotaEstudianteDominio datos) {
        notaDominio.validarValor(datos.getValor());
        EstudianteEntidad estudiante = estudianteDominio.validarEstudianteExista(datos.getIdEstudiante());
        estudianteDominio.validarEstudianteActivo(estudiante);
        NotaEntidad nota = notaDominio.validarNotaExista(datos.getIdNota());
        notaDominio.validarNotaVigente(nota);
        DocenteEntidad docente = docenteDominio.validarDocenteExista(datos.getIdDocente());
        docenteDominio.validarDocenteSeaDelGrupo(docente, nota.getGrupo());
        EstudianteGrupoEntidad estudianteGrupo = estudianteDominio.validarEstudiantePertenezcaAlGrupo(datos.getIdEstudiante(), nota.getGrupo());
        notaDominio.validarNotaNoRegistrada(estudianteGrupo, nota);

        UUID id = UUID.randomUUID();
        notaEstudianteRepositorio.crear(new NotaEstudianteEntidad(id, estudianteGrupo, nota, datos.getValor()));
        return id;
    }
}
