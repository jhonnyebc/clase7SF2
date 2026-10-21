package uco.test.api.transversal.excepcion;

public class NotaYaRegistradaException extends UcoTestApiException {
    public NotaYaRegistradaException() {
        super("Esta evaluación ya tiene una nota registrada para el estudiante. Cierra el formulario y pulsa Actualizar para consultar la nota guardada.");
    }
}
