package uco.test.api.servicios.input.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public class RegistrarNotaEstudianteRequest {
    private UUID idEstudiante;
    private UUID idNota;
    private UUID idDocente;
    private BigDecimal valor;

    public RegistrarNotaEstudianteRequest() {
    }

    public UUID getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(UUID idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public UUID getIdNota() {
        return idNota;
    }

    public void setIdNota(UUID idNota) {
        this.idNota = idNota;
    }

    public UUID getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(UUID idDocente) {
        this.idDocente = idDocente;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
