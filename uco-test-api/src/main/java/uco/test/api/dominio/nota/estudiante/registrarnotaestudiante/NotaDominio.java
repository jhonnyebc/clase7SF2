package uco.test.api.dominio.nota.estudiante.registrarnotaestudiante;

import org.springframework.stereotype.Component;
import uco.test.api.servicios.output.ports.entidades.EstudianteGrupoEntidad;
import uco.test.api.servicios.output.ports.entidades.NotaEntidad;
import uco.test.api.servicios.output.ports.entidades.NotaEstudianteEntidad;
import uco.test.api.servicios.output.ports.repositorios.NotaEstudianteRepositorio;
import uco.test.api.servicios.output.ports.repositorios.NotaRepositorio;
import uco.test.api.transversal.excepcion.UcoTestApiException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class NotaDominio {

    private static final BigDecimal VALOR_MINIMO = BigDecimal.ZERO;
    private static final BigDecimal VALOR_MAXIMO = BigDecimal.valueOf(5);
    private static final int DECIMALES_PERMITIDOS = 1;

    private final NotaRepositorio notaRepositorio;
    private final NotaEstudianteRepositorio notaEstudianteRepositorio;

    public NotaDominio(NotaRepositorio notaRepositorio, NotaEstudianteRepositorio notaEstudianteRepositorio) {
        this.notaRepositorio = notaRepositorio;
        this.notaEstudianteRepositorio = notaEstudianteRepositorio;
    }

    public NotaEntidad validarNotaExista(UUID id) {
        NotaEntidad nota = notaRepositorio.consultarPorId(id);
        if (nota == null) {
            throw new UcoTestApiException("La nota no existe");
        }
        return nota;
    }

    public boolean esVigente(NotaEntidad nota) {
        Instant ahora = Instant.now();
        return !ahora.isBefore(nota.getFechaRegistroDesde()) && !ahora.isAfter(nota.getFechaRegistroHasta());
    }

    public void validarNotaVigente(NotaEntidad nota) {
        if (!esVigente(nota)) {
            throw new UcoTestApiException("La nota no esta vigente para registro");
        }
    }

    public void validarValor(BigDecimal valor) {
        if (valor.compareTo(VALOR_MINIMO) < 0 || valor.compareTo(VALOR_MAXIMO) > 0) {
            throw new UcoTestApiException("El valor de la nota debe estar entre " + VALOR_MINIMO + " y " + VALOR_MAXIMO);
        }
        if (valor.stripTrailingZeros().scale() > DECIMALES_PERMITIDOS) {
            throw new UcoTestApiException("El valor de la nota solo puede tener " + DECIMALES_PERMITIDOS + " decimal");
        }
    }

    public void validarNotaNoRegistrada(EstudianteGrupoEntidad estudianteGrupo, NotaEntidad nota) {
        NotaEstudianteEntidad filtro = new NotaEstudianteEntidad(null,
                new EstudianteGrupoEntidad(estudianteGrupo.getId(), null, null),
                new NotaEntidad(nota.getId(), null, null, null, null), null);
        if (!notaEstudianteRepositorio.consultarPorFiltro(filtro).isEmpty()) {
            throw new uco.test.api.transversal.excepcion.NotaYaRegistradaException();
        }
    }
}
