package uco.test.api.infraestructura.output.repositorios.estructurados.JPA;

import jakarta.persistence.*;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "Nota")
public class NotaEntidadJPA {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private GrupoEntidadJPA grupo;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "fecha_registro_desde", nullable = false)
    private Instant fechaRegistroDesde;

    @Column(name = "fecha_registro_hasta", nullable = false)
    private Instant fechaRegistroHasta;

    @Column(name = "porcentaje", precision = 5, scale = 2)
    private BigDecimal porcentaje;

    public NotaEntidadJPA() {
    }

    public NotaEntidadJPA(UUID id, GrupoEntidadJPA grupo, String nombre, Instant fechaRegistroDesde, Instant fechaRegistroHasta) {
        this(id, grupo, nombre, fechaRegistroDesde, fechaRegistroHasta, null);
    }

    public NotaEntidadJPA(UUID id, GrupoEntidadJPA grupo, String nombre, Instant fechaRegistroDesde, Instant fechaRegistroHasta, BigDecimal porcentaje) {
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

    public GrupoEntidadJPA getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoEntidadJPA grupo) {
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
