package co.edu.konradlorenz.solicitudes;

import co.edu.konradlorenz.compartido.infraestructura.EventosProcesados;
import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionEnviadaEvent;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import co.edu.konradlorenz.solicitudes.adaptadores.entrada.CotizacionEventsListener;
import co.edu.konradlorenz.solicitudes.aplicacion.SolicitudService;
import co.edu.konradlorenz.solicitudes.dominio.EstadoSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.ItemSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudEnTramiteEvent;
import co.edu.konradlorenz.solicitudes.puertos.salida.SolicitudRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El listener con SolicitudService real y la tabla en un mapa: cotizacion.enviada pasa a EN TRÁMITE las
 * solicitudes que quedan cotizadas completas, con el precio sugerido del Ministerio, y publica
 * solicitud.en-tramite; un reenvío no cuenta dos veces; orden.generada (corte 5) las completa.
 */
class CotizacionEventsListenerTest {

    /** Ningún evento se había procesado (DEC-104). */
    private static EventosProcesados nuevos() {
        EventosProcesados p = org.mockito.Mockito.mock(EventosProcesados.class);
        org.mockito.Mockito.when(p.marcar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(true);
        return p;
    }

    private static final String JEFE = "carlos.mendoza@konradgourmet.com";

    private final Map<String, SolicitudAlimentos> tabla = new HashMap<>();
    private final List<EventoDominio> publicados = new ArrayList<>();
    private CotizacionEventsListener listener;

    @BeforeEach
    void preparar() {
        SolicitudRepositoryPort repositorio = new SolicitudRepositoryPort() {
            @Override
            public SolicitudAlimentos guardar(SolicitudAlimentos s) {
                tabla.put(s.numero(), s);
                return s;
            }

            @Override
            public List<SolicitudAlimentos> buscar(co.edu.konradlorenz.solicitudes.dominio.CriteriosConsulta c) {
                return List.copyOf(tabla.values());
            }

            @Override
            public SolicitudAlimentos porNumero(String numero) {
                return tabla.get(numero);
            }
        };
        EventPublisherPort eventos = publicados::add;
        listener = new CotizacionEventsListener(new SolicitudService(repositorio, eventos), nuevos());
        tabla.put("SOL-031", solicitud("SOL-031", new ItemSolicitud(1L, new BigDecimal("20"), "Marca X", "kg"),
                new ItemSolicitud(2L, new BigDecimal("15"), null, "kg")));
        tabla.put("SOL-032", solicitud("SOL-032", new ItemSolicitud(3L, new BigDecimal("10"), null, "kg")));
    }

    private static SolicitudAlimentos solicitud(String numero, ItemSolicitud... items) {
        return new SolicitudAlimentos(numero, 1L, 2L, JEFE, LocalDateTime.now(), EstadoSolicitud.PENDIENTE, BigDecimal.ZERO,
                new LinkedHashSet<>(), List.of(items));
    }

    private static CotizacionEnviadaEvent enviada(String numero, List<String> solicitudes, Map<Long, BigDecimal> precios) {
        return new CotizacionEnviadaEvent("isabella.castro@konradgourmet.com", numero, solicitudes, precios, "ventas@proveedorabc.com",
                "s3://documentos-pdf/cotizaciones/" + numero + ".pdf");
    }

    @Test
    void laCotizacionCompletaPasaLaSolicitudAEnTramite() {
        listener.alRecibir(enviada("COT-018", List.of("SOL-031"), Map.of(1L, new BigDecimal("20000"), 2L, new BigDecimal("8000"))));

        SolicitudAlimentos s = tabla.get("SOL-031");
        assertEquals(EstadoSolicitud.EN_TRAMITE, s.estado());
        assertEquals(0, new BigDecimal("520000").compareTo(s.valorSugeridoTotal()), "20 × 20.000 + 15 × 8.000");
        assertTrue(s.yaAplicada("COT-018"));
        SolicitudEnTramiteEvent enTramite = (SolicitudEnTramiteEvent) publicados.stream()
                .filter(e -> e instanceof SolicitudEnTramiteEvent).findFirst().orElseThrow();
        assertEquals("SOL-031", enTramite.numero());
        assertEquals(JEFE, enTramite.correoSolicitante(), "el aviso es para el jefe que la pidió");
        assertEquals(EstadoSolicitud.PENDIENTE, tabla.get("SOL-032").estado(), "SOL-032 no estaba en la cotización");
    }

    @Test
    void unaCotizacionDeVariasSolicitudesYUnReenvioQueNoCuentaDosVeces() {
        CotizacionEnviadaEvent evento = enviada("COT-020", List.of("SOL-031", "SOL-032"),
                Map.of(1L, new BigDecimal("20000"), 2L, new BigDecimal("8000"), 3L, new BigDecimal("3500")));
        listener.alRecibir(evento);
        listener.alRecibir(evento); // cotizacion.enviada otra vez (reenvío): no suma ni publica de nuevo

        assertEquals(EstadoSolicitud.EN_TRAMITE, tabla.get("SOL-031").estado());
        assertEquals(EstadoSolicitud.EN_TRAMITE, tabla.get("SOL-032").estado());
        assertEquals(0, new BigDecimal("35000").compareTo(tabla.get("SOL-032").valorSugeridoTotal()));
        assertEquals(2, publicados.stream().filter(e -> e instanceof SolicitudEnTramiteEvent).count());
        assertEquals(2, publicados.stream().filter(e -> e instanceof AuditoriaEvent a && a.accion().startsWith("Cotización COT-020 aplicada")).count());
    }

    @Test
    void unaCotizacionParcialNoLaPasaAEnTramite() {
        listener.alRecibir(enviada("COT-021", List.of("SOL-031"), Map.of(1L, new BigDecimal("20000"))));
        assertEquals(EstadoSolicitud.PENDIENTE, tabla.get("SOL-031").estado(), "le falta el arroz");
        assertTrue(publicados.stream().noneMatch(e -> e instanceof SolicitudEnTramiteEvent));
    }

    @Test
    void ordenGeneradaCompletaLasSolicitudes() {
        listener.alRecibir(new OrdenGeneradaEvent("laura.gomez@konradgourmet.com", "OC-0012", "COT-018", List.of("SOL-031"), "Proveedor ABC",
                "ventas@proveedorabc.com", "s3://documentos-pdf/ordenes/OC-0012.pdf", new BigDecimal("545000")));
        assertEquals(EstadoSolicitud.COMPLETADA, tabla.get("SOL-031").estado());
    }

}
