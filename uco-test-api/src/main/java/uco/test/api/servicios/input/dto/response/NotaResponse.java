package uco.test.api.servicios.input.dto.response;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

public class NotaResponse {
    private UUID id;
    private String nombre;
    private Instant fechaRegistroDesde;
    private Instant fechaRegistroHasta;
    private boolean vigente;
    private BigDecimal porcentaje;

    public NotaResponse(UUID id, String nombre, Instant fechaRegistroDesde, Instant fechaRegistroHasta, boolean vigente, BigDecimal porcentaje) {
        this.id = id;
        this.nombre = nombre;
        this.fechaRegistroDesde = fechaRegistroDesde;
        this.fechaRegistroHasta = fechaRegistroHasta;
        this.vigente = vigente;
        this.porcentaje = porcentaje;
    }

    public BigDecimal getPorcentaje() { return porcentaje; }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Instant getFechaRegistroDesde() {
        return fechaRegistroDesde;
    }

    public Instant getFechaRegistroHasta() {
        return fechaRegistroHasta;
    }

    public boolean isVigente() {
        return vigente;
    }
}
