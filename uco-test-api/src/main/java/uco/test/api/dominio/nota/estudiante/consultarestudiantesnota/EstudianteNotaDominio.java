package uco.test.api.dominio.nota.estudiante.consultarestudiantesnota;

import uco.test.api.servicios.output.ports.entidades.EstudianteEntidad;

import java.math.BigDecimal;

public class EstudianteNotaDominio {
    private EstudianteEntidad estudiante;
    private BigDecimal valor;

    public EstudianteNotaDominio(EstudianteEntidad estudiante, BigDecimal valor) {
        this.estudiante = estudiante;
        this.valor = valor;
    }

    public EstudianteEntidad getEstudiante() {
        return estudiante;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public boolean isRegistrada() {
        return valor != null;
    }
}
