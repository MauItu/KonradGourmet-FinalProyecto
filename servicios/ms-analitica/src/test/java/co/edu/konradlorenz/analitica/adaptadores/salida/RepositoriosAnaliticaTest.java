package co.edu.konradlorenz.analitica.adaptadores.salida;

import co.edu.konradlorenz.analitica.configuracion.NombresNegocioPrueba;
import co.edu.konradlorenz.analitica.dominio.EstadoSugerencia;
import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;
import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Los adaptadores JPA de ms-analitica: la correlación de cada hecho (DEC-64) y la regla «ya fue aceptada o descartada» con dos jefes a la vez. */
class RepositoriosAnaliticaTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 18);

    @AfterEach
    void limpiar() {
        ContextoRecorrido.cerrar();
    }

    @Test
    void cadaHechoGuardaLaCorrelacionDelEventoQueLoDejo() {
        HechoJpaRepository repositorio = mock(HechoJpaRepository.class);
        UUID recorrido = UUID.randomUUID();
        ContextoRecorrido.abrir("isabella.torres@konradgourmet.com", recorrido);
        new KpiJpaAdapter(repositorio).guardar(new HechoNegocio("DEMANDA_PRODUCTO", 1L, "Ajiaco", BigDecimal.ONE, HOY));
        ArgumentCaptor<HechoEntidad> fila = ArgumentCaptor.forClass(HechoEntidad.class);
        verify(repositorio).save(fila.capture());
        assertEquals(recorrido, fila.getValue().correlacionId);
        assertEquals("Ajiaco", fila.getValue().clave);
    }

    @Test
    void sinSucursalSonTodasYSinFechasElRangoQuedaAbierto() {
        HechoJpaRepository repositorio = mock(HechoJpaRepository.class);
        KpiJpaAdapter adaptador = new KpiJpaAdapter(repositorio);
        adaptador.hechos(HOY.minusDays(29), HOY, null);
        verify(repositorio).findByFechaBetweenOrderByFechaAscIdAsc(HOY.minusDays(29), HOY);
        adaptador.hechos(null, null, 2L);
        verify(repositorio).findByFechaBetweenAndSucursalIdOrderByFechaAscIdAsc(eq(LocalDate.of(2000, 1, 1)), eq(LocalDate.of(9999, 12, 31)), eq(2L));
    }

    @Test
    void unaSugerenciaNuevaSeInsertaYUnaResueltaPorOtroJefeDa409() {
        SugerenciaJpaRepository repositorio = mock(SugerenciaJpaRepository.class);
        SugerenciaJpaAdapter adaptador = new SugerenciaJpaAdapter(repositorio);
        adaptador.guardar(new SugerenciaPromocion("Ajiaco", "Tendencia en redes", HOY, HOY.plusDays(6)));
        verify(repositorio).insertar("Ajiaco", "Tendencia en redes", HOY, HOY.plusDays(6), "SUGERIDA");

        SugerenciaPromocion aceptada = new SugerenciaPromocion(7L, "Ajiaco", "Tendencia en redes", HOY, HOY.plusDays(6), EstadoSugerencia.SUGERIDA);
        aceptada.aceptar();
        when(repositorio.resolver(7L, "ACEPTADA")).thenReturn(1);
        adaptador.guardar(aceptada);
        verify(repositorio).resolver(7L, "ACEPTADA");

        SugerenciaPromocion otra = new SugerenciaPromocion(8L, "Bandeja paisa", "Entre los 3 más pedidos de la semana", HOY, HOY.plusDays(6),
                EstadoSugerencia.SUGERIDA);
        otra.descartar();
        when(repositorio.resolver(8L, "DESCARTADA")).thenReturn(0);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> adaptador.guardar(otra));
        assertEquals("Esta sugerencia ya fue aceptada o descartada.", e.getMessage());
        verify(repositorio, never()).insertar(eq("Bandeja paisa"), any(), any(), any(), any());
    }

    @Test
    void elNombreDeUnPlatoQueTodaviaNoLlegaEsPlatoN() {
        assertEquals(List.of("Ajiaco", "Plato 9", "Sucursal Norte", "Sucursal 7"), NombresNegocioPrueba.nombres());
    }

}
