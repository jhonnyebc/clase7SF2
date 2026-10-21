package uco.test.api.infraestructura.output.repositorios.estructurados.JPA;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "notaEstudiante", uniqueConstraints = @UniqueConstraint(
        name = "uk_nota_estudiante_evaluacion", columnNames = {"estudiante_grupo_id", "nota_id"}))
public class NotaEstudianteEntidadJPA {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_grupo_id", nullable = false)
    private EstudianteGrupoEntidadJPA estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nota_id", nullable = false)
    private NotaEntidadJPA nota;

    @Column(name = "valor", nullable = false, precision = 2, scale = 1)
    private BigDecimal valor;

    public NotaEstudianteEntidadJPA() {
    }

    public NotaEstudianteEntidadJPA(UUID id, EstudianteGrupoEntidadJPA estudiante, NotaEntidadJPA nota, BigDecimal valor) {
        this.id = id;
        this.estudiante = estudiante;
        this.nota = nota;
        this.valor = valor;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EstudianteGrupoEntidadJPA getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(EstudianteGrupoEntidadJPA estudiante) {
        this.estudiante = estudiante;
    }

    public NotaEntidadJPA getNota() {
        return nota;
    }

    public void setNota(NotaEntidadJPA nota) {
        this.nota = nota;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
