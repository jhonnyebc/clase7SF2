package uco.test.api.dominio.nota.estudiante.registrarnotaestudiante;

import java.math.BigDecimal;
import java.util.UUID;

public class RegistrarNotaEstudianteDominio {
    private UUID idEstudiante;
    private UUID idNota;
    private UUID idDocente;
    private BigDecimal valor;

    public RegistrarNotaEstudianteDominio(UUID idEstudiante, UUID idNota, UUID idDocente, BigDecimal valor) {
        this.idEstudiante = idEstudiante;
        this.idNota = idNota;
        this.idDocente = idDocente;
        this.valor = valor;
    }

    public UUID getIdEstudiante() {
        return idEstudiante;
    }

    public UUID getIdNota() {
        return idNota;
    }

    public UUID getIdDocente() {
        return idDocente;
    }

    public BigDecimal getValor() {
        return valor;
    }
}
