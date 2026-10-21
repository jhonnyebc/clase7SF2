package uco.test.api.infraestructura.input.controladores;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uco.test.api.transversal.excepcion.UcoTestApiException;

import java.util.Map;

@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler(uco.test.api.transversal.excepcion.NotaYaRegistradaException.class)
    public ResponseEntity<Map<String, String>> manejarNotaDuplicada(
            uco.test.api.transversal.excepcion.NotaYaRegistradaException excepcion) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", excepcion.getMessage()));
    }

    @ExceptionHandler(UcoTestApiException.class)
    public ResponseEntity<Map<String, String>> manejarUcoTestApiException(UcoTestApiException excepcion) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", excepcion.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarIllegalArgumentException(IllegalArgumentException excepcion) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", excepcion.getMessage()));
    }
}
