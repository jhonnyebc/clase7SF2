package uco.test.api.servicios.output.ports.entidades;

import java.math.BigDecimal;
import java.util.UUID;

public class NotaEstudianteEntidad {
    private UUID id;
    private EstudianteGrupoEntidad estudiante;
    private NotaEntidad nota;
    private BigDecimal valor;

    public NotaEstudianteEntidad() {
    }

    public NotaEstudianteEntidad(UUID id, EstudianteGrupoEntidad estudiante, NotaEntidad nota, BigDecimal valor) {
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

    public EstudianteGrupoEntidad getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(EstudianteGrupoEntidad estudiante) {
        this.estudiante = estudiante;
    }

    public NotaEntidad getNota() {
        return nota;
    }

    public void setNota(NotaEntidad nota) {
        this.nota = nota;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
