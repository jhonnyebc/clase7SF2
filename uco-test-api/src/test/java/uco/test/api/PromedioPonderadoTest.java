package uco.test.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.*;
import uco.test.api.dominio.nota.estudiante.PromedioPonderadoDominio;
import uco.test.api.dominio.nota.estudiante.PromedioPonderadoDominio.Estado;
import uco.test.api.servicios.output.ports.entidades.NotaEntidad;

class PromedioPonderadoTest {
    NotaEntidad nota(String peso) {
        return new NotaEntidad(UUID.randomUUID(), null, "Evaluacion", null, null,
                peso == null ? null : new BigDecimal(peso));
    }
    void decimal(String esperado, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, new BigDecimal(esperado).compareTo(actual));
    }
    @Test void calculaDefinitivaConPesosDiferentes() {
        var a=nota("30"); var b=nota("30"); var c=nota("40");
        var r=PromedioPonderadoDominio.calcular(List.of(a,b,c), Map.of(a.getId(),new BigDecimal("4"),b.getId(),new BigDecimal("3.5"),c.getId(),new BigDecimal("4.5")));
        assertEquals(Estado.DEFINITIVA,r.estado()); decimal("4.05",r.definitiva()); decimal("100",r.porcentajeEvaluado());
        assertEquals(0,r.pendientes());
    }
    @Test void parcialNoRedistribuyePesosNiInventaCero() {
        var a=nota("70"); var b=nota("30");
        var r=PromedioPonderadoDominio.calcular(List.of(a,b), Map.of(a.getId(),new BigDecimal("4.2")));
        assertEquals(Estado.PARCIAL,r.estado()); decimal("2.94",r.acumulado()); decimal("70",r.porcentajeEvaluado());
        assertNull(r.definitiva()); assertEquals(1,r.pendientes()); assertEquals(1,r.registradas());
    }
    @Test void ceroEsUnaNotaRegistrada() {
        var a=nota("100"); var r=PromedioPonderadoDominio.calcular(List.of(a),Map.of(a.getId(),BigDecimal.ZERO));
        assertEquals(Estado.DEFINITIVA,r.estado()); decimal("0",r.definitiva()); assertEquals(1,r.registradas());
    }
    @Test void sinNotasNoDevuelveDefinitivaCero() {
        var r=PromedioPonderadoDominio.calcular(List.of(nota("100")),Map.of());
        assertEquals(Estado.SIN_NOTAS,r.estado()); assertNull(r.acumulado()); assertNull(r.definitiva()); decimal("0",r.porcentajeEvaluado());
    }
    @Test void noCalculaConConfiguracionInvalida() {
        for (var notas : List.of(List.of(nota("90")),List.of(nota(null)),List.of(nota("0"),nota("100")),List.<NotaEntidad>of())) {
            var r=PromedioPonderadoDominio.calcular(notas,Map.of());
            assertEquals(Estado.CONFIGURACION_INVALIDA,r.estado()); assertNull(r.acumulado()); assertNull(r.definitiva()); assertNull(r.porcentajeEvaluado());
        }
    }
    @Test void noRedondeaAntesDeSumar() {
        var a=nota("33.33"); var b=nota("33.33"); var c=nota("33.34");
        var r=PromedioPonderadoDominio.calcular(List.of(a,b,c),Map.of(a.getId(),new BigDecimal("0.1"),b.getId(),new BigDecimal("0.1"),c.getId(),new BigDecimal("0.1")));
        decimal("0.1",r.definitiva());
    }
    @Test void ignoraValoresDeEvaluacionesAjenas() {
        var a=nota("100");
        var r=PromedioPonderadoDominio.calcular(List.of(a),Map.of(UUID.randomUUID(),new BigDecimal("5")));
        assertEquals(Estado.SIN_NOTAS,r.estado()); assertEquals(0,r.registradas());
    }
}
