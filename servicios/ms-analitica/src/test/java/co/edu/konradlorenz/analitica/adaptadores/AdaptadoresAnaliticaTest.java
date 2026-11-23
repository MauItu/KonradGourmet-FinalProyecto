package co.edu.konradlorenz.analitica.adaptadores;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import co.edu.konradlorenz.analitica.adaptadores.entrada.NegocioEventsListener;
import co.edu.konradlorenz.analitica.adaptadores.entrada.TendenciasJob;
import co.edu.konradlorenz.analitica.aplicacion.TableroService;
import co.edu.konradlorenz.analitica.configuracion.EventosProcesados;
import co.edu.konradlorenz.analitica.configuracion.NombresNegocio;
import co.edu.konradlorenz.analitica.dominio.CalculadorKpi;
import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.dominio.ProductoMayorDemanda;
import co.edu.konradlorenz.analitica.dominio.ProveedorMejoresPrecios;
import co.edu.konradlorenz.analitica.dominio.SucursalMayorDemanda;
import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;
import co.edu.konradlorenz.analitica.puertos.salida.KpiRepositoryPort;
import co.edu.konradlorenz.analitica.puertos.salida.SugerenciaRepositoryPort;
import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionValidadaEvent;
import co.edu.konradlorenz.pedidos.eventos.PedidoRegistradoEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * NegocioEventsListener con la copia local de los nombres (DEC-90) y sin repetir eventos, y TendenciasJob, que deja
 * el error en el log con un recorrido propio y no lo deja salir (HU-19, DEC-64).
 */
class AdaptadoresAnaliticaTest {

    private final List<HechoNegocio> hechos = new ArrayList<>();
    private final NombresNegocio nombres = mock(NombresNegocio.class);
    private final EventosProcesados procesados = mock(EventosProcesados.class);
    /** El job corre en una transacción (DEC-104); aquí no hay base. */
    private final PlatformTransactionManager transacciones = mock(PlatformTransactionManager.class);
    private final KpiRepositoryPort repositorio = new KpiRepositoryPort() {
        @Override
        public void guardar(HechoNegocio h) {
            hechos.add(h);
        }

        @Override
        public List<HechoNegocio> hechos(java.time.LocalDate desde, java.time.LocalDate hasta, Long sucursalId) {
            return List.copyOf(hechos);
        }
    };
    private final List<CalculadorKpi> estrategias = List.of(new ProductoMayorDemanda(), new SucursalMayorDemanda(), new ProveedorMejoresPrecios());
    private final TableroService tablero = new TableroService(estrategias, repositorio, List::of, mock(EventPublisherPort.class),
            mock(SugerenciaRepositoryPort.class));

    private final Logger log = (Logger) LoggerFactory.getLogger(TendenciasJob.class);
    private final List<String> registrados = new ArrayList<>();
    private final AppenderBase<ILoggingEvent> testigo = new AppenderBase<>() {
        @Override
        protected void append(ILoggingEvent e) {
            ContextoRecorrido.Datos d = ContextoRecorrido.actual();
            registrados.add(e.getLevel() + "|" + e.getFormattedMessage() + "|" + (d == null ? "-" : d.usuario() + "@" + d.correlacionId()));
        }
    };

    @AfterEach
    void limpiar() {
        log.detachAppender(testigo);
        ContextoRecorrido.cerrar();
    }

    @Test
    void elPedidoDejaSusHechosConLosNombresDeLaCopiaLocal() {
        when(procesados.marcar(any())).thenReturn(true);
        when(nombres.plato(1L)).thenReturn("Bandeja paisa");
        when(nombres.plato(2L)).thenReturn("Ajiaco");
        when(nombres.sucursal(1L)).thenReturn("Sucursal Centro");
        Map<Long, Integer> lineas = new LinkedHashMap<>();
        lineas.put(1L, 2);
        lineas.put(2L, 1);
        new NegocioEventsListener(tablero, nombres, procesados)
                .alRecibir(new PedidoRegistradoEvent("isabella.torres@konradgourmet.com", "PED-0251", 1L, lineas));
        assertEquals(List.of("DEMANDA_PRODUCTO:Bandeja paisa:2", "DEMANDA_PRODUCTO:Ajiaco:1", "DEMANDA_SUCURSAL:Sucursal Centro:3"),
                hechos.stream().map(h -> h.tipo() + ":" + h.clave() + ":" + h.valor()).toList());
        assertEquals("Bandeja paisa", tablero.kpis(null, null, null).get(0).ganador());
    }

    @Test
    void unEventoRepetidoNoDejaHechosOtraVez() {
        when(procesados.marcar(any())).thenReturn(true, false);
        NegocioEventsListener listener = new NegocioEventsListener(tablero, nombres, procesados);
        CotizacionValidadaEvent cot018 = new CotizacionValidadaEvent("laura.gomez@konradgourmet.com", "COT-018", 1L, "Proveedor ABC",
                "OPCIONADA", new BigDecimal("545000"), new BigDecimal("4.81"));
        listener.alRecibir(cot018);
        listener.alRecibir(cot018);
        assertEquals(1, hechos.size());
        assertEquals("PRECIO_PROVEEDOR", hechos.get(0).tipo());
        assertNull(hechos.get(0).sucursalId());
    }

    @Test
    void siElAnalisisFallaElJobLoDejaEnElLogConSuRecorridoYSigue() {
        testigo.setContext(log.getLoggerContext());
        testigo.start();
        log.addAppender(testigo);
        TableroUseCase caido = mock(TableroUseCase.class);
        List<ContextoRecorrido.Datos> durante = new ArrayList<>();
        doAnswer(i -> {
            durante.add(ContextoRecorrido.actual());
            throw new IllegalStateException("La API de tendencias de las redes sociales respondió 503");
        }).when(caido).detectarTendencias();

        new TendenciasJob(caido, transacciones).ejecutar();

        assertEquals(1, registrados.size(), registrados.toString());
        String[] r = registrados.get(0).split("\\|");
        assertEquals(Level.ERROR.toString(), r[0]);
        assertEquals("Falló el análisis de tendencias de promociones: La API de tendencias de las redes sociales respondió 503", r[1]);
        assertNotNull(durante.get(0), "corre en un recorrido propio (sin usuario: «sistema»)");
        assertNull(durante.get(0).usuario());
        assertEquals("null@" + durante.get(0).correlacionId(), r[2], "y el error sale con esa correlación");
        assertNull(ContextoRecorrido.actual(), "y deja el hilo como estaba");
    }

    @Test
    void elJobSinErroresNoRegistraNada() {
        testigo.setContext(log.getLoggerContext());
        testigo.start();
        log.addAppender(testigo);
        TableroUseCase bien = mock(TableroUseCase.class);
        new TendenciasJob(bien, transacciones).ejecutar();
        assertEquals(List.of(), registrados);
        doThrow(new RuntimeException("x")).when(bien).detectarTendencias();
        new TendenciasJob(bien, transacciones).ejecutar();
        assertEquals(1, registrados.size());
        assertNotNull(UUID.fromString(registrados.get(0).split("@")[1]));
    }

}
