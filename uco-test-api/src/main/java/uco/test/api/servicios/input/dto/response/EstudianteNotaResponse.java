package uco.test.api.servicios.input.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class EstudianteNotaResponse {
    private UUID idEstudiante;
    private String identificacion;
    private String nombres;
    private String apellidos;
    private boolean activo;
    private BigDecimal valor;
    private boolean registrada;

    public EstudianteNotaResponse(UUID idEstudiante, String identificacion, String nombres, String apellidos,
                                  boolean activo, BigDecimal valor, boolean registrada) {
        this.idEstudiante = idEstudiante;
        this.identificacion = identificacion;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.activo = activo;
        this.valor = valor;
        this.registrada = registrada;
    }

    public UUID getIdEstudiante() {
        return idEstudiante;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public boolean isActivo() {
        return activo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public boolean isRegistrada() {
        return registrada;
    }
}
