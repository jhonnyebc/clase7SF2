package uco.test.api.servicios.casosuso;

public interface CasoUsoConRetorno <I,O>{
    O ejecutar(I datos);
}
