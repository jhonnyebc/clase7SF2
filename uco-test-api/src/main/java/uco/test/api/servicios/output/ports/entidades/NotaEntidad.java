package uco.test.api.servicios.output.ports.entidades;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

public class NotaEntidad {
    private UUID id;
    private GrupoEntidad grupo;
    private String nombre;
    private Instant fechaRegistroDesde;
    private Instant fechaRegistroHasta;
    private BigDecimal porcentaje;

    public NotaEntidad() {
    }

    public NotaEntidad(UUID id, GrupoEntidad grupo, String nombre, Instant fechaRegistroDesde, Instant fechaRegistroHasta) {
        this(id, grupo, nombre, fechaRegistroDesde, fechaRegistroHasta, null);
    }

    public NotaEntidad(UUID id, GrupoEntidad grupo, String nombre, Instant fechaRegistroDesde, Instant fechaRegistroHasta, BigDecimal porcentaje) {
        this.id = id;
        this.grupo = grupo;
        this.nombre = nombre;
        this.fechaRegistroDesde = fechaRegistroDesde;
        this.fechaRegistroHasta = fechaRegistroHasta;
        this.porcentaje = porcentaje;
    }

    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public GrupoEntidad getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoEntidad grupo) {
        this.grupo = grupo;
    }

    public Instant getFechaRegistroDesde() {
        return fechaRegistroDesde;
    }

    public void setFechaRegistroDesde(Instant fechaRegistroDesde) {
        this.fechaRegistroDesde = fechaRegistroDesde;
    }

    public Instant getFechaRegistroHasta() {
        return fechaRegistroHasta;
    }

    public void setFechaRegistroHasta(Instant fechaRegistroHasta) {
        this.fechaRegistroHasta = fechaRegistroHasta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
