package co.edu.konradlorenz.cotizaciones;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.infraestructura.ConfiguracionCompartida;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.compartido.infraestructura.ManejoErrores;
import co.edu.konradlorenz.compartido.infraestructura.SeguridadServicio;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.cotizaciones.adaptadores.entrada.CotizacionRestController;
import co.edu.konradlorenz.cotizaciones.adaptadores.entrada.ProveedorRestController;
import co.edu.konradlorenz.cotizaciones.aplicacion.CotizacionService;
import co.edu.konradlorenz.cotizaciones.aplicacion.GenerarCotizacionFacade;
import co.edu.konradlorenz.cotizaciones.aplicacion.ProveedorService;
import co.edu.konradlorenz.cotizaciones.configuracion.NombresProductos;
import co.edu.konradlorenz.cotizaciones.configuracion.NumeracionCotizaciones;
import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ItemCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.cotizaciones.dominio.ReglaValidacion;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionEnviadaEvent;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionValidadaEvent;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarCotizacionUseCase;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarProveedorUseCase;
import co.edu.konradlorenz.cotizaciones.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.CotizacionRepositoryPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.GeneradorPdfPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.PrecioSugeridoPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.ProveedorRepositoryPort;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * V13–V19 con los controladores, la seguridad, el núcleo y el publicador de Kafka reales; la base, el
 * almacén, el PDF, ms-integracion y el broker son simulados. Recorre COT-018 de la demo: el auxiliar la
 * genera con el precio del Ministerio y el PDF, y el director registra los valores y la valida
 * (+4,81 % → OPCIONADA); y prueba las otras dos reglas, el Ministerio caído y los roles.
 */
@WebMvcTest({CotizacionRestController.class, ProveedorRestController.class})
@ImportAutoConfiguration({ConfiguracionCompartida.class, SeguridadServicio.class, ManejoErrores.class})
class CotizacionRestTest {

    private static final String AUXILIAR = "isabella.castro@konradgourmet.com";
    private static final String DIRECTORA = "laura.gomez@konradgourmet.com";
    private static final Proveedor ABC = new Proveedor(1L, "Proveedor ABC", "900123456-1", "ventas@proveedorabc.com", "6011234567",
            "Cra 7 # 10-20", List.of("Carnes", "Granos"));

    @TestConfiguration
    static class Nucleo {
        @Bean
        GestionarCotizacionUseCase gestionarCotizacion(CotizacionRepositoryPort repositorio, PrecioSugeridoPort precios, GeneradorPdfPort pdf,
                                                       AlmacenArchivosPort almacen, EventPublisherPort eventos) {
            return new CotizacionService(new GenerarCotizacionFacade(repositorio, precios, pdf, almacen, eventos), repositorio,
                    ReglaValidacion.cadenaPorDefecto(), almacen, eventos);
        }

        @Bean
        GestionarProveedorUseCase gestionarProveedor(ProveedorRepositoryPort repositorio, EventPublisherPort eventos) {
            return new ProveedorService(repositorio, eventos);
        }
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private FormatoEventos formato;
    @MockitoBean
    private CotizacionRepositoryPort repositorio;
    @MockitoBean
    private ProveedorRepositoryPort proveedores;
    @MockitoBean
    private PrecioSugeridoPort precios;
    @MockitoBean
    private GeneradorPdfPort pdf;
    @MockitoBean
    private AlmacenArchivosPort almacen;
    @MockitoBean
    private NumeracionCotizaciones numeracion;
    @MockitoBean
    private NombresProductos nombres;
    @MockitoBean
    private KafkaTemplate<String, String> kafka;

    /** La «tabla» de cotizaciones: lo que guarda el repositorio simulado. */
    private final Map<String, Cotizacion> tabla = new HashMap<>();

    private static RequestPostProcessor como(String rol, long id, String correo) {
        return jwt().jwt(j -> j.subject(String.valueOf(id)).claim("correo", correo).claim("rol", rol).claim("sucursalId", 1))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @BeforeEach
    void preparar() {
        when(proveedores.porId(1L)).thenReturn(ABC);
        when(numeracion.siguiente()).thenReturn("COT-018");
        when(nombres.todos()).thenReturn(Map.of(1L, "Res", 2L, "Arroz"));
        when(repositorio.guardar(any())).thenAnswer(i -> {
            Cotizacion c = i.getArgument(0);
            tabla.put(c.numero(), c);
            return c;
        });
        when(repositorio.porNumero(anyString())).thenAnswer(i -> tabla.get(i.<String>getArgument(0)));
        when(pdf.generar(any())).thenReturn("%PDF-1.4 cotización".getBytes());
        when(almacen.guardar(anyString(), any())).thenAnswer(i -> "s3://documentos-pdf/cotizaciones/" + i.getArgument(0));
    }

    /** Las cotizaciones de la demo: res 20 kg y arroz 15 kg de SOL-031, ya enviadas con los precios del Ministerio. */
    private void enviada(String numero) {
        tabla.put(numero, new Cotizacion(numero, List.of("SOL-031"), ABC, LocalDate.now(), LocalDate.now().plusDays(7),
                "s3://documentos-pdf/cotizaciones/" + numero + ".pdf", EstadoCotizacion.ENVIADA,
                List.of(new ItemCotizacion(1L, new BigDecimal("20"), "kg", new BigDecimal("20000"), null),
                        new ItemCotizacion(2L, new BigDecimal("15"), "kg", new BigDecimal("8000"), null))));
    }

    @SuppressWarnings("unchecked")
    private <T extends co.edu.konradlorenz.compartido.eventos.EventoDominio> T publicado(String topico, Class<T> tipo) {
        ArgumentCaptor<ProducerRecord<String, String>> enviados = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafka, org.mockito.Mockito.atLeastOnce()).send(enviados.capture());
        return enviados.getAllValues().stream().filter(r -> r.topic().equals(topico)).reduce((a, b) -> b)
                .map(r -> formato.deJson(r.value(), tipo)).orElseThrow(() -> new AssertionError("no se publicó " + topico));
    }

    private static final String NUEVA = "{\"solicitudes\":[\"SOL-031\"],\"proveedorId\":1,\"fechaLimite\":\"" + LocalDate.now().plusDays(7) + "\","
            + "\"items\":[{\"productoId\":1,\"producto\":\"Res\",\"cantidad\":20,\"unidad\":\"kg\"},"
            + "{\"productoId\":2,\"producto\":\"Arroz\",\"cantidad\":15,\"unidad\":\"kg\"}]}";

    @Test
    void elAuxiliarGeneraLaCotizacionConElPrecioDelMinisterioYElPdf() throws Exception {
        when(precios.precioSugerido(1L)).thenReturn(new BigDecimal("20000"));
        when(precios.precioSugerido(2L)).thenReturn(new BigDecimal("8000"));
        UUID recorrido = UUID.randomUUID();

        mvc.perform(post("/api/cotizaciones").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR)).header("X-Correlation-Id", recorrido.toString())
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("COT-018"))
                .andExpect(jsonPath("$.estado").value("ENVIADA"))
                .andExpect(jsonPath("$.pdf").value(true))
                .andExpect(jsonPath("$.proveedor").value("Proveedor ABC"))
                .andExpect(jsonPath("$.items[0].producto").value("Res"))
                .andExpect(jsonPath("$.estandar").value(520000))
                .andExpect(jsonPath("$.cotizado").doesNotExist());

        verify(nombres).recordar(Map.of(1L, "Res", 2L, "Arroz"));
        verify(almacen).guardar(org.mockito.ArgumentMatchers.eq("COT-018.pdf"), any());
        CotizacionEnviadaEvent evento = publicado("cotizacion.enviada", CotizacionEnviadaEvent.class);
        assertEquals("COT-018", evento.numero());
        assertEquals(List.of("SOL-031"), evento.solicitudNumeros());
        assertEquals(0, new BigDecimal("20000").compareTo(evento.precios().get(1L)), "lleva el precio sugerido de cada producto");
        assertEquals("ventas@proveedorabc.com", evento.correoProveedor());
        assertEquals("s3://documentos-pdf/cotizaciones/COT-018.pdf", evento.pdfUrl());
        assertEquals(AUXILIAR, evento.usuario(), "a nombre de la auxiliar que inició sesión");
        assertEquals(recorrido, evento.correlacionId(), "en el recorrido de la petición");
        AuditoriaEvent auditoria = publicado("auditoria.evento", AuditoriaEvent.class);
        assertEquals("Cotización COT-018 enviada a Proveedor ABC", auditoria.accion());
        assertEquals(AUXILIAR, auditoria.usuario());
    }

    @Test
    void siElMinisterioNoRespondeQuedaPendienteDePrecioSinPublicarLaEnviada() throws Exception {
        when(precios.precioSugerido(1L)).thenThrow(new IllegalStateException("ms-integracion no responde"));

        mvc.perform(post("/api/cotizaciones").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR)).contentType(MediaType.APPLICATION_JSON).content(NUEVA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_PRECIO"))
                .andExpect(jsonPath("$.pdf").value(false));
        verify(pdf, never()).generar(any());
        AuditoriaEvent auditoria = publicado("auditoria.evento", AuditoriaEvent.class);
        assertTrue(auditoria.accion().startsWith("Cotización COT-018 guardada PENDIENTE_PRECIO"), auditoria.accion());

        // reenviar completa el precio y la envía
        org.mockito.Mockito.doReturn(new BigDecimal("20000")).when(precios).precioSugerido(1L);
        when(precios.precioSugerido(2L)).thenReturn(new BigDecimal("8000"));
        mvc.perform(post("/api/cotizaciones/COT-018/reenvio").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENVIADA"));
        assertEquals("COT-018", publicado("cotizacion.enviada", CotizacionEnviadaEvent.class).numero());
    }

    @Test
    void sinSolicitudesNoSeGastaNumero() throws Exception {
        mvc.perform(post("/api/cotizaciones").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR)).contentType(MediaType.APPLICATION_JSON)
                        .content(NUEVA.replace("[\"SOL-031\"]", "[]")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Debe seleccionar al menos una solicitud"));
        verify(numeracion, never()).siguiente();
        verify(kafka, never()).send(any(ProducerRecord.class));
    }

    @Test
    void unProveedorInactivoNoRecibeCotizaciones() throws Exception {
        Proveedor inactivo = new Proveedor(1L, "Proveedor ABC", "900123456-1", "ventas@proveedorabc.com", null, null, List.of(), false);
        when(proveedores.porId(1L)).thenReturn(inactivo);
        mvc.perform(post("/api/cotizaciones").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR)).contentType(MediaType.APPLICATION_JSON).content(NUEVA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El proveedor Proveedor ABC está inactivo"));
    }

    @Test
    void laDirectoraRegistraLosValoresYValidaCot018Opcionada() throws Exception {
        enviada("COT-018");
        mvc.perform(put("/api/cotizaciones/COT-018/valores").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"precios\":{\"1\":21250,\"2\":8000}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cotizado").value(545000))
                .andExpect(jsonPath("$.diferencia").value(4.81))
                .andExpect(jsonPath("$.estado").value("ENVIADA"));
        mvc.perform(post("/api/cotizaciones/COT-018/validacion").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OPCIONADA"));
        CotizacionValidadaEvent validada = publicado("cotizacion.validada", CotizacionValidadaEvent.class);
        assertEquals(EstadoCotizacion.OPCIONADA, validada.estado());
        assertEquals(0, new BigDecimal("545000").compareTo(validada.valorCotizado()));
        assertEquals(DIRECTORA, validada.usuario());
    }

    @Test
    void lasOtrasDosReglasDeLaCadena() throws Exception {
        enviada("COT-019");
        enviada("COT-020");
        // COT-019: la res a $29.000 → 700.000 contra 520.000 = +34,62 % → RECHAZADA
        mvc.perform(put("/api/cotizaciones/COT-019/valores").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"precios\":{\"1\":29000,\"2\":8000}}")).andExpect(status().isOk());
        mvc.perform(post("/api/cotizaciones/COT-019/validacion").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(jsonPath("$.estado").value("RECHAZADA")).andExpect(jsonPath("$.diferencia").value(34.62));
        // COT-020: la res a $4.000 → 200.000 = −61,54 % → SOSPECHOSA
        mvc.perform(put("/api/cotizaciones/COT-020/valores").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"precios\":{\"1\":4000,\"2\":8000}}")).andExpect(status().isOk());
        mvc.perform(post("/api/cotizaciones/COT-020/validacion").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(jsonPath("$.estado").value("SOSPECHOSA")).andExpect(jsonPath("$.diferencia").value(-61.54));
        // ya validada, no se vuelve a validar
        mvc.perform(post("/api/cotizaciones/COT-020/validacion").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isConflict());
    }

    @Test
    void sinTodosLosPreciosNoSeValida() throws Exception {
        enviada("COT-018");
        mvc.perform(put("/api/cotizaciones/COT-018/valores").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"precios\":{\"1\":21250}}")).andExpect(status().isOk());
        mvc.perform(post("/api/cotizaciones/COT-018/validacion").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Falta el precio del proveedor para el producto 2"));
    }

    @Test
    void elPdfSaleDelAlmacen() throws Exception {
        enviada("COT-018");
        when(almacen.obtener("s3://documentos-pdf/cotizaciones/COT-018.pdf")).thenReturn("%PDF-1.4".getBytes());
        mvc.perform(get("/api/cotizaciones/COT-018/pdf?descargar=true").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"COT-018.pdf\""))
                .andExpect(content().bytes("%PDF-1.4".getBytes()));
        mvc.perform(get("/api/cotizaciones/COT-099/pdf").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la cotización COT-099"));
    }

    @Test
    void proveedoresHu15() throws Exception {
        when(proveedores.existeNit("900123456-1")).thenReturn(true);
        when(proveedores.guardar(any())).thenAnswer(i -> {
            Proveedor p = i.getArgument(0);
            return new Proveedor(5L, p.razonSocial(), p.nit(), p.correo(), p.telefono(), p.direccion(), p.categorias(), p.activo());
        });
        mvc.perform(post("/api/proveedores").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razonSocial\":\" Frutas del Valle \",\"nit\":\"901000111-5\",\"correo\":\"ventas@frutasdelvalle.com\",\"categorias\":[\"Frutas\",\"\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.razonSocial").value("Frutas del Valle"))
                .andExpect(jsonPath("$.categorias.length()").value(1))
                .andExpect(jsonPath("$.activo").value(true));
        assertEquals("Proveedor registrado: Frutas del Valle", publicado("auditoria.evento", AuditoriaEvent.class).accion());
        mvc.perform(post("/api/proveedores").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razonSocial\":\"Otro\",\"nit\":\"900123456-1\",\"correo\":\"otro@correo.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe un proveedor con el NIT 900123456-1"));
        mvc.perform(post("/api/proveedores").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razonSocial\":\"Otro\",\"nit\":\"901\",\"correo\":\"sin-arroba\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El correo del proveedor no es válido"));
        mvc.perform(patch("/api/proveedores/1/inactivar").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR)))
                .andExpect(status().isNoContent());
        ArgumentCaptor<Proveedor> guardado = ArgumentCaptor.forClass(Proveedor.class);
        verify(proveedores, org.mockito.Mockito.atLeastOnce()).guardar(guardado.capture());
        assertEquals(false, guardado.getValue().activo(), "inactivar no borra: guarda activo = false");
    }

    @Test
    void v20ListaSoloLasOpcionadas() throws Exception {
        enviada("COT-018");
        tabla.get("COT-018").cambiarEstado(EstadoCotizacion.OPCIONADA);
        when(repositorio.porEstado(EstadoCotizacion.OPCIONADA)).thenAnswer(i -> tabla.values().stream()
                .filter(c -> c.estado() == EstadoCotizacion.OPCIONADA).toList());
        mvc.perform(get("/api/cotizaciones?estado=OPCIONADA").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].numero").value("COT-018"))
                .andExpect(jsonPath("$[0].estado").value("OPCIONADA"));
        tabla.get("COT-018").cambiarEstado(EstadoCotizacion.CON_ORDEN);
        mvc.perform(get("/api/cotizaciones?estado=OPCIONADA").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void cadaRutaDejaEntrarSoloASuRol() throws Exception {
        mvc.perform(post("/api/cotizaciones").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON).content(NUEVA))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/cotizaciones/COT-018/valores").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"precios\":{\"1\":1}}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/cotizaciones/COT-018/validacion").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR))).andExpect(status().isForbidden());
        mvc.perform(get("/api/cotizaciones/pendientes").with(como("AUXILIAR_COMPRAS", 3, AUXILIAR))).andExpect(status().isForbidden());
        mvc.perform(get("/api/cotizaciones").with(como("JEFE_COCINA", 2, "carlos.mendoza@konradgourmet.com"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/proveedores").with(como("DIRECTOR_COMPRAS", 4, DIRECTORA)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/proveedores").with(como("MESERO", 1, "isabella.torres@konradgourmet.com"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/cotizaciones")).andExpect(status().isUnauthorized());
    }

}
