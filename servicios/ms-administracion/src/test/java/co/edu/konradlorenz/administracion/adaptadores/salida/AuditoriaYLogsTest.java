package co.edu.konradlorenz.administracion.adaptadores.salida;

import co.edu.konradlorenz.administracion.adaptadores.entrada.AuditoriaListener;
import co.edu.konradlorenz.administracion.aplicacion.AdministracionService;
import co.edu.konradlorenz.administracion.configuracion.RefrescoParametros;
import co.edu.konradlorenz.administracion.dominio.LogError;
import co.edu.konradlorenz.administracion.dominio.ParametrosSistema;
import co.edu.konradlorenz.administracion.puertos.salida.ParametrosRepositoryPort;
import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * La auditoría de todos los servicios con su recorrido (DEC-64), las acciones propias de ms-administracion a nombre
 * de quien inició sesión (n_bitacora), el refresco de la caché de parámetros (n_parametros) y la consulta de los
 * logs a Elasticsearch (DEC-84).
 */
class AuditoriaYLogsTest {

    private static final String PAULA = "paula.herrera@konradgourmet.com";

    private final AuditoriaJpaRepository repositorio = mock(AuditoriaJpaRepository.class);
    private final ParametrosRepositoryPort parametros = mock(ParametrosRepositoryPort.class);
    private final AdministracionService servicio = new AdministracionService(new AuditoriaJpaAdapter(repositorio), s -> List.of(), parametros);
    private HttpServer elasticsearch;

    @AfterEach
    void limpiar() {
        ContextoRecorrido.cerrar();
        if (elasticsearch != null) {
            elasticsearch.stop(0);
        }
    }

    private AuditoriaEntidad guardada() {
        ArgumentCaptor<AuditoriaEntidad> fila = ArgumentCaptor.forClass(AuditoriaEntidad.class);
        verify(repositorio).saveAndFlush(fila.capture());
        return fila.getValue();
    }

    @Test
    void laAuditoriaDeOtroServicioLlegaConSuUsuarioYSuRecorrido() {
        UUID recorrido = UUID.randomUUID();
        ContextoRecorrido.abrir(null, recorrido); // lo abre InterceptorCorrelacion con la correlación del evento
        AuditoriaEvent evento = new AuditoriaEvent("isabella.torres@konradgourmet.com", "Pedido PED-0251 registrado por el mesero 3");
        new AuditoriaListener(servicio).alRecibir(evento);
        AuditoriaEntidad e = guardada();
        assertEquals("Pedido PED-0251 registrado por el mesero 3", e.accion);
        assertEquals("isabella.torres@konradgourmet.com", e.usuario);
        assertEquals(recorrido, e.correlacionId);
        assertEquals(evento.ocurridoEn().toLocalDate(), e.fecha);
    }

    @Test
    void unEventoRepetidoNoSeGuardaDosVeces() {
        when(repositorio.existsByFechaAndHoraAndUsuarioAndAccionAndCorrelacionId(any(), any(), any(), any(), any())).thenReturn(true);
        new AuditoriaListener(servicio).alRecibir(new AuditoriaEvent("carlos.mendoza@konradgourmet.com", "Solicitud SOL-031 registrada"));
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    void elCambioDeUnParametroQuedaANombreDeLaAdministradora() {
        UUID recorrido = UUID.randomUUID();
        ContextoRecorrido.abrir(PAULA, recorrido); // lo abre FiltroContexto con el JWT
        servicio.parametrizar("general.moneda", "COP");
        verify(parametros).guardar("general.moneda", "COP");
        AuditoriaEntidad e = guardada();
        assertEquals("Parámetro general.moneda actualizado", e.accion);
        assertEquals(PAULA, e.usuario);
        assertEquals(recorrido, e.correlacionId);
    }

    @Test
    void sinSesionQuedaElUsuarioDelEvento() {
        new AuditoriaListener(servicio).alRecibir(new AuditoriaEvent("sistema", "Cierre diario de inventario"));
        AuditoriaEntidad e = guardada();
        assertEquals("sistema", e.usuario);
        assertEquals(null, e.correlacionId);
    }

    @Test
    void cadaReplicaRefrescaSuCacheDeLaBase() {
        when(parametros.cargar()).thenReturn(Map.of("tema.nombre", "Konrad Gourmet Express"));
        new RefrescoParametros(parametros).refrescar();
        assertEquals("Konrad Gourmet Express", ParametrosSistema.getInstancia().valor("tema.nombre"));
        assertEquals("Konrad Gourmet Express", servicio.tema().get("tema.nombre"));
    }

    @Test
    void losLogsSeConsultanEnElasticsearch() throws Exception {
        AtomicReference<String> consulta = new AtomicReference<>();
        AtomicReference<Integer> codigo = new AtomicReference<>(200);
        elasticsearch = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        elasticsearch.createContext("/logs-errores/_search", intercambio -> {
            consulta.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] r = (codigo.get() == 404 ? "{\"error\":{\"type\":\"index_not_found_exception\"}}"
                    : "{\"hits\":{\"hits\":[{\"_source\":{\"servicio\":\"ms-pedidos\",\"mensaje\":\"No se pudo guardar el pedido\","
                    + "\"fechaHora\":\"2026-11-17T15:30:15.123Z\"}}]}}").getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(codigo.get(), r.length);
            intercambio.getResponseBody().write(r);
            intercambio.close();
        });
        elasticsearch.start();
        ElasticsearchLogsAdapter adaptador = new ElasticsearchLogsAdapter("http://127.0.0.1:" + elasticsearch.getAddress().getPort(), 200);

        List<LogError> errores = adaptador.logs("ms-pedidos");
        assertEquals(1, errores.size());
        assertEquals("ms-pedidos", errores.get(0).servicio());
        assertEquals("No se pudo guardar el pedido", errores.get(0).mensaje());
        assertEquals(LocalDateTime.of(2026, 11, 17, 10, 30, 15, 123_000_000), errores.get(0).fechaHora(), "en hora de Bogotá");
        assertTrue(consulta.get().contains("\"term\":{\"servicio\":\"ms-pedidos\"}"), consulta.get());
        assertTrue(consulta.get().contains("\"order\":\"desc\""), "los más recientes primero");

        adaptador.logs(null);
        assertTrue(consulta.get().contains("\"match_all\""), "sin servicio, todos");

        codigo.set(404);
        assertEquals(List.of(), adaptador.logs(null), "sin índice todavía ningún servicio ha fallado");
    }

}
