package uco.test.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.*;
import uco.test.api.servicios.input.dto.response.NotaResponse;
import uco.test.api.servicios.input.dto.response.PonderacionResponse;

class PonderacionTest {
    private NotaResponse nota(String porcentaje) {
        return new NotaResponse(UUID.randomUUID(), "Evaluacion", null, null, true,
                porcentaje == null ? null : new BigDecimal(porcentaje));
    }
    @Test void aceptaTotalCienConPrecisionDecimal() {
        var resultado = PonderacionResponse.desde(List.of(nota("33.33"), nota("33.33"), nota("33.34")));
        assertTrue(resultado.valida());
        assertEquals(0, resultado.total().compareTo(new BigDecimal("100")));
    }
    @Test void noAceptaVacioNiPorcentajesAusentes() {
        assertFalse(PonderacionResponse.desde(List.of()).valida());
        var resultado = PonderacionResponse.desde(Arrays.asList(nota(null), nota("100")));
        assertFalse(resultado.valida());
        assertEquals(1, resultado.sinPorcentaje());
    }
    @Test void noAceptaTotalesIncompletosOExcesivos() {
        assertFalse(PonderacionResponse.desde(List.of(nota("30"), nota("60"))).valida());
        assertFalse(PonderacionResponse.desde(List.of(nota("70"), nota("40"))).valida());
    }
    @Test void noAceptaValoresInvalidosAunqueSumenCien() {
        assertFalse(PonderacionResponse.desde(List.of(nota("-10"), nota("110"))).valida());
        assertFalse(PonderacionResponse.desde(List.of(nota("0"), nota("100"))).valida());
    }
}
