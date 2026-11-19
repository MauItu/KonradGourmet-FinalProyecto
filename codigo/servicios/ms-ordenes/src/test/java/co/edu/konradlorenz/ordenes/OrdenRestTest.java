package co.edu.konradlorenz.ordenes;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.infraestructura.ConfiguracionCompartida;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.compartido.infraestructura.ManejoErrores;
import co.edu.konradlorenz.compartido.infraestructura.SeguridadServicio;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.ordenes.adaptadores.entrada.OrdenCompraRestController;
import co.edu.konradlorenz.ordenes.aplicacion.OrdenCompraService;
import co.edu.konradlorenz.ordenes.configuracion.NombresProductos;
import co.edu.konradlorenz.ordenes.dominio.CotizacionOpcionada;
import co.edu.konradlorenz.ordenes.dominio.EstadoOrden;
import co.edu.konradlorenz.ordenes.dominio.ItemOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import co.edu.konradlorenz.ordenes.puertos.entrada.GenerarOrdenUseCase;
import co.edu.konradlorenz.ordenes.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.ordenes.puertos.salida.CotizacionConsultaPort;
import co.edu.konradlorenz.ordenes.puertos.salida.GeneradorPdfPort;
import co.edu.konradlorenz.ordenes.puertos.salida.OrdenRepositoryPort;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * V21–V23 con el controlador, la seguridad, el núcleo y el publicador de Kafka reales; la base, el almacén,
 * el PDF, ms-cotizaciones y el broker son simulados. Recorre OC-0012 de la demo: la directora la genera
 * sobre COT-018 (res 20 kg a $21.250 y arroz 15 kg a $8.000: $545.000), la ve en V23 y abre su PDF; y prueba
 * la segunda orden, la cotización no opcionada, el doble clic, ms-cotizaciones caído, la orden recibida
 * (FINALIZADA, DEC-80) y los roles.
 */
@WebMvcTest(OrdenCompraRestController.class)
@ImportAutoConfiguration({ConfiguracionCompartida.class, SeguridadServicio.class, ManejoErrores.class})
class OrdenRestTest {

    private static final String DIRECTORA = "laura.gomez@konradgourmet.com";
    private static final String NUEVA = "{\"cotizacionNumero\":\"COT-018\"}";

    @TestConfiguration
    static class Nucleo {
        @Bean
        GenerarOrdenUseCase generarOrden(OrdenRepositoryPort repositorio, CotizacionConsultaPort cotizaciones, GeneradorPdfPort pdf,
                                         AlmacenArchivosPort almacen, EventPublisherPort eventos) {
            return new OrdenCompraService(repositorio, cotizaciones, pdf, almacen, eventos);
        }
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private FormatoEventos formato;
    @MockitoBean
    private OrdenRepositoryPort repositorio;
    @MockitoBean
    private CotizacionConsultaPort cotizaciones;
    @MockitoBean
    private GeneradorPdfPort pdf;
    @MockitoBean
    private AlmacenArchivosPort almacen;
    @MockitoBean
    private NombresProductos nombres;
    @MockitoBean
    private KafkaTemplate<String, String> kafka;

    /** La «tabla» de órdenes: lo que guarda el repositorio simulado, con la secuencia desde OC-0012. */
    private final Map<String, OrdenCompra> tabla = new LinkedHashMap<>();
    private int siguiente = 12;

    private static RequestPostProcessor como(String rol, long id, String correo) {
        return jwt().jwt(j -> j.subject(String.valueOf(id)).claim("correo", correo).claim("rol", rol).claim("sucursalId", 1))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @BeforeEach
    void preparar() {
        when(nombres.todos()).thenReturn(Map.of(1L, "Res", 2L, "Arroz"));
        when(repositorio.guardar(any())).thenAnswer(i -> {
            OrdenCompra o = i.getArgument(0);
            OrdenCompra n = o.numero() != null ? o : new OrdenCompra(String.format("OC-%04d", siguiente++), o);
            tabla.put(n.numero(), n);
            return n;
        });
        when(repositorio.existePorCotizacion(anyString()))
                .thenAnswer(i -> tabla.values().stream().anyMatch(o -> o.cotizacionNumero().equals(i.getArgument(0))));
        when(repositorio.porNumero(anyString())).thenAnswer(i -> tabla.get(i.<String>getArgument(0)));
        when(repositorio.porEstado(any())).thenAnswer(i -> tabla.values().stream().filter(o -> o.estado() == i.getArgument(0)).toList());
        when(cotizaciones.opcionada("COT-018")).thenReturn(new CotizacionOpcionada("COT-018", List.of("SOL-031"), "Proveedor ABC",
                "ventas@proveedorabc.com", List.of(new ItemOrden(1L, new BigDecimal("20"), "kg", new BigDecimal("21250")),
                new ItemOrden(2L, new BigDecimal("15"), "kg", new BigDecimal("8000")))));
        when(pdf.generar(any())).thenReturn("%PDF-1.4 orden".getBytes());
        when(almacen.guardar(anyString(), any())).thenAnswer(i -> "s3://documentos-pdf/ordenes/" + i.getArgument(0));
        when(almacen.obtener("s3://documentos-pdf/ordenes/OC-0012.pdf")).thenReturn("%PDF-1.4 orden".getBytes());
    }

    private <T extends EventoDominio> List<T> publicados(String topico, Class<T> tipo) {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<String, String>> enviados = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafka, org.mockito.Mockito.atLeast(0)).send(enviados.capture());
        return enviados.getAllValues().stream().filter(r -> r.topic().equals(topico)).map(r -> formato.deJson(r.value(), tipo)).toList();
    }

    @Test
    void laDirectoraGeneraOc0012SobreCot018YLaVeEnV23ConSuPdf() throws Exception {
        UUID recorrido = UUID.randomUUID();
        mvc.perform(post("/api/ordenes-compra").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).header("X-Correlation-Id", recorrido.toString())
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("OC-0012"))
                .andExpect(jsonPath("$.cotizacion").value("COT-018"))
                .andExpect(jsonPath("$.solicitudes[0]").value("SOL-031"))
                .andExpect(jsonPath("$.proveedor").value("Proveedor ABC"))
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"))
                .andExpect(jsonPath("$.pdf").value(true))
                .andExpect(jsonPath("$.items[0].producto").value("Res"))
                .andExpect(jsonPath("$.items[0].valor").value(425000))
                .andExpect(jsonPath("$.total").value(545000));

        verify(almacen).guardar(eq("OC-0012.pdf"), any());
        OrdenGeneradaEvent evento = publicados("orden.generada", OrdenGeneradaEvent.class).get(0);
        assertEquals("OC-0012", evento.numero());
        assertEquals("COT-018", evento.cotizacionNumero());
        assertEquals(List.of("SOL-031"), evento.solicitudNumeros());
        assertEquals("ventas@proveedorabc.com", evento.correoProveedor());
        assertEquals("s3://documentos-pdf/ordenes/OC-0012.pdf", evento.pdfUrl());
        assertEquals(0, new BigDecimal("545000").compareTo(evento.total()));
        assertEquals(DIRECTORA, evento.usuario(), "a nombre de la directora que inició sesión");
        assertEquals(recorrido, evento.correlacionId(), "en el recorrido de la petición");
        AuditoriaEvent auditoria = publicados("auditoria.evento", AuditoriaEvent.class).get(0);
        assertEquals("Orden OC-0012 generada sobre COT-018", auditoria.accion());
        assertEquals(DIRECTORA, auditoria.usuario());

        mvc.perform(get("/api/ordenes-compra").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value("OC-0012"))
                .andExpect(jsonPath("$[0].items[1].producto").value("Arroz"));
        mvc.perform(get("/api/ordenes-compra?estado=FINALIZADA").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/ordenes-compra/OC-0012/pdf").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes("%PDF-1.4 orden".getBytes()));
        mvc.perform(get("/api/ordenes-compra/OC-9999/pdf").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la orden de compra OC-9999"));
    }

    @Test
    void unaSegundaOrdenSobreLaMismaCotizacionSeRechaza() throws Exception {
        mvc.perform(post("/api/ordenes-compra").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON).content(NUEVA))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/ordenes-compra").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON).content(NUEVA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una orden de compra para la cotización COT-018"));
        assertEquals(1, publicados("orden.generada", OrdenGeneradaEvent.class).size(), "una sola orden.generada");
    }

    @Test
    void soloSobreCotizacionesOpcionadas() throws Exception {
        when(cotizaciones.opcionada("COT-019")).thenThrow(new IllegalStateException("Solo se pueden generar órdenes de compra sobre cotizaciones opcionadas"));
        mvc.perform(post("/api/ordenes-compra").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cotizacionNumero\":\"COT-019\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Solo se pueden generar órdenes de compra sobre cotizaciones opcionadas"));
        verify(repositorio, never()).guardar(any());
        mvc.perform(post("/api/ordenes-compra").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void elDobleClicQueLaBaseRechazaYMsCotizacionesCaido() throws Exception {
        doReturn(false).when(repositorio).existePorCotizacion("COT-018");
        doThrow(new DataIntegrityViolationException("orden_compra_cotizacion_numero_key")).when(repositorio).guardar(any());
        mvc.perform(post("/api/ordenes-compra").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON).content(NUEVA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una orden de compra para esa cotización"));

        when(cotizaciones.opcionada("COT-022")).thenThrow(new ResourceAccessException("Connection refused"));
        mvc.perform(post("/api/ordenes-compra").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cotizacionNumero\":\"COT-022\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void laDirectoraMarcaOc0012ComoRecibidaUnaSolaVez() throws Exception {
        mvc.perform(post("/api/ordenes-compra").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON).content(NUEVA))
                .andExpect(status().isCreated());
        UUID recorrido = UUID.randomUUID();
        mvc.perform(patch("/api/ordenes-compra/OC-0012/finalizar").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA))
                        .header("X-Correlation-Id", recorrido.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("OC-0012"))
                .andExpect(jsonPath("$.estado").value("FINALIZADA"))
                .andExpect(jsonPath("$.items[0].producto").value("Res"))
                .andExpect(jsonPath("$.total").value(545000));
        assertEquals(EstadoOrden.FINALIZADA, tabla.get("OC-0012").estado(), "quedó guardada FINALIZADA");
        AuditoriaEvent auditoria = publicados("auditoria.evento", AuditoriaEvent.class).stream()
                .filter(a -> a.accion().equals("Orden OC-0012 finalizada")).findFirst().orElseThrow();
        assertEquals(DIRECTORA, auditoria.usuario(), "a nombre de la directora");
        assertEquals(recorrido, auditoria.correlacionId(), "en el recorrido de la petición");
        assertEquals(1, publicados("orden.generada", OrdenGeneradaEvent.class).size(), "no publica otra orden.generada");

        mvc.perform(get("/api/ordenes-compra?estado=FINALIZADA").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].numero").value("OC-0012"));
        mvc.perform(get("/api/ordenes-compra?estado=EN_PROCESO").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));

        mvc.perform(patch("/api/ordenes-compra/OC-0012/finalizar").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("La orden OC-0012 ya está finalizada"));
        mvc.perform(patch("/api/ordenes-compra/OC-9999/finalizar").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la orden de compra OC-9999"));
        assertEquals(1, publicados("auditoria.evento", AuditoriaEvent.class).stream()
                .filter(a -> a.accion().equals("Orden OC-0012 finalizada")).count(), "una sola auditoría de la finalización");
    }

    @Test
    void soloLaDirectoraDeCompras() throws Exception {
        mvc.perform(post("/api/ordenes-compra").with(como("AUXILIAR_COMPRAS", 3, "isabella.castro@konradgourmet.com"))
                .contentType(MediaType.APPLICATION_JSON).content(NUEVA)).andExpect(status().isForbidden());
        mvc.perform(get("/api/ordenes-compra").with(como("JEFE_COCINA", 2, "carlos.mendoza@konradgourmet.com"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/ordenes-compra/OC-0012/pdf").with(como("ADMINISTRADOR", 6, "paula.herrera@konradgourmet.com")))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/ordenes-compra/OC-0012/finalizar").with(como("AUXILIAR_COMPRAS", 3, "isabella.castro@konradgourmet.com")))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/ordenes-compra/OC-0012/finalizar").with(como("JEFE_COCINA", 2, "carlos.mendoza@konradgourmet.com")))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/ordenes-compra/OC-0012/finalizar")).andExpect(status().isUnauthorized());
        verify(repositorio, never()).porNumero("OC-0012");
        mvc.perform(get("/api/ordenes-compra")).andExpect(status().isUnauthorized());
        assertEquals(EstadoOrden.EN_PROCESO, EstadoOrden.valueOf("EN_PROCESO"));
    }

}
