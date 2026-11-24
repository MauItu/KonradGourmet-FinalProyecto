package co.edu.konradlorenz.solicitudes;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.infraestructura.ConfiguracionCompartida;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.compartido.infraestructura.ManejoErrores;
import co.edu.konradlorenz.compartido.infraestructura.SeguridadServicio;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.solicitudes.adaptadores.entrada.SolicitudRestController;
import co.edu.konradlorenz.solicitudes.aplicacion.SolicitudService;
import co.edu.konradlorenz.solicitudes.dominio.CriteriosConsulta;
import co.edu.konradlorenz.solicitudes.dominio.EstadoSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.ItemSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudRegistradaEvent;
import co.edu.konradlorenz.solicitudes.puertos.entrada.GestionarSolicitudUseCase;
import co.edu.konradlorenz.solicitudes.puertos.salida.SolicitudRepositoryPort;
import org.apache.kafka.clients.producer.ProducerRecord;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * V08–V12 con el controlador, la seguridad y el publicador de Kafka reales (solo la base y el broker son
 * simulados): la solicitud queda a nombre del jefe que inició sesión, solicitud.registrada sale con su
 * correo y la correlación de la petición, y cada ruta deja entrar solo a su rol.
 */
@WebMvcTest(SolicitudRestController.class)
@ImportAutoConfiguration({ConfiguracionCompartida.class, SeguridadServicio.class, ManejoErrores.class})
class SolicitudRegistradaTest {

    private static final String JEFE = "carlos.mendoza@konradgourmet.com";

    @TestConfiguration
    static class Nucleo {
        @Bean
        GestionarSolicitudUseCase gestionarSolicitud(SolicitudRepositoryPort repositorio, EventPublisherPort eventos) {
            return new SolicitudService(repositorio, eventos);
        }
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private FormatoEventos formato;
    @MockitoBean
    private SolicitudRepositoryPort repositorio;
    @MockitoBean
    private KafkaTemplate<String, String> kafka;

    private static RequestPostProcessor como(String rol, long id, String correo) {
        return jwt().jwt(j -> j.subject(String.valueOf(id)).claim("correo", correo).claim("rol", rol).claim("sucursalId", 1))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    private static SolicitudAlimentos guardada(String numero, long solicitante) {
        return new SolicitudAlimentos(numero, 1L, solicitante, solicitante == 2 ? JEFE : "otro.jefe@konradgourmet.com", LocalDateTime.now(),
                EstadoSolicitud.PENDIENTE, BigDecimal.ZERO, Set.of(),
                List.of(new ItemSolicitud(1L, new BigDecimal("20"), "Marca X", "kg")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void elJefeRegistraYElEventoLlevaSuCorreoYLaCorrelacion() throws Exception {
        // el repositorio le pone el número, como SolicitudJpaAdapter
        when(repositorio.guardar(any())).thenAnswer(i -> {
            SolicitudAlimentos s = i.getArgument(0);
            return new SolicitudAlimentos("SOL-031", s.sucursalId(), s.solicitanteId(), s.correoSolicitante(), s.fecha(), s.estado(),
                    s.valorSugeridoTotal(), s.cotizacionesAplicadas(), s.items());
        });
        UUID recorrido = UUID.randomUUID();

        mvc.perform(post("/api/solicitudes").with(como("JEFE_COCINA", 2, JEFE))
                        .header("X-Correlation-Id", recorrido.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productoId\":1,\"cantidad\":20,\"marca\":\" Marca X \",\"unidad\":\"kg\"},"
                                + "{\"productoId\":2,\"cantidad\":15,\"marca\":\"\",\"unidad\":\"kg\"}],\"sucursalId\":9,\"solicitanteId\":99}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("SOL-031"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.sucursalId").value(1))
                .andExpect(jsonPath("$.solicitanteId").value(2))
                .andExpect(jsonPath("$.correoSolicitante").value(JEFE))
                .andExpect(jsonPath("$.items[0].marca").value("Marca X"))
                .andExpect(jsonPath("$.items[1].marca").doesNotExist());

        ArgumentCaptor<ProducerRecord<String, String>> enviados = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafka, times(2)).send(enviados.capture());
        Map<String, ProducerRecord<String, String>> porTopico = enviados.getAllValues().stream()
                .collect(Collectors.toMap(ProducerRecord::topic, r -> r));
        ProducerRecord<String, String> registro = porTopico.get("solicitud.registrada");
        SolicitudRegistradaEvent evento = formato.deJson(registro.value(), SolicitudRegistradaEvent.class);
        assertEquals("SOL-031", registro.key(), "la clave de Kafka es la solicitud");
        assertEquals(JEFE, evento.usuario());
        assertEquals(recorrido, evento.correlacionId());
        assertEquals(1L, evento.sucursalId());
        assertEquals(2L, evento.solicitanteId());
        assertEquals(List.of(1L, 2L), evento.productoIds());
        AuditoriaEvent auditoria = formato.deJson(porTopico.get("auditoria.evento").value(), AuditoriaEvent.class);
        assertEquals("Solicitud SOL-031 registrada", auditoria.accion());
        assertEquals(recorrido, auditoria.correlacionId());
    }

    @Test
    void sinProductosNoSeRegistraNiSePublica() throws Exception {
        mvc.perform(post("/api/solicitudes").with(como("JEFE_COCINA", 2, JEFE))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"items\":[]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Debe agregar al menos un producto"));
        verify(kafka, never()).send(any(ProducerRecord.class));
    }

    @Test
    void cadaRutaDejaEntrarSoloASuRol() throws Exception {
        mvc.perform(post("/api/solicitudes").with(como("MESERO", 1, "isabella.torres@konradgourmet.com"))
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/solicitudes").with(como("AUXILIAR_COMPRAS", 3, "isabella.castro@konradgourmet.com"))
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/solicitudes").with(como("JEFE_COCINA", 2, JEFE))).andExpect(status().isForbidden());
        mvc.perform(get("/api/solicitudes?solicitante=yo").with(como("AUXILIAR_COMPRAS", 3, "isabella.castro@konradgourmet.com")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/solicitudes/SOL-031").with(como("DIRECTOR_COMPRAS", 4, "laura.gomez@konradgourmet.com")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/solicitudes")).andExpect(status().isUnauthorized());
    }

    @Test
    void elJefeVeSoloLasSuyasYElAuxiliarLasDeTodos() throws Exception {
        when(repositorio.buscar(any(CriteriosConsulta.class))).thenReturn(List.of(guardada("SOL-031", 2), guardada("SOL-032", 8)));
        when(repositorio.porNumero("SOL-032")).thenReturn(guardada("SOL-032", 8));

        mvc.perform(get("/api/solicitudes?solicitante=yo").with(como("JEFE_COCINA", 2, JEFE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].numero").value("SOL-031"));
        mvc.perform(get("/api/solicitudes/SOL-032").with(como("JEFE_COCINA", 2, JEFE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("La solicitud consultada no está disponible"));
        mvc.perform(get("/api/solicitudes?estado=PENDIENTE&producto=1&marca=Marca X").with(como("AUXILIAR_COMPRAS", 3, "isabella.castro@konradgourmet.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/solicitudes/SOL-032").with(como("AUXILIAR_COMPRAS", 3, "isabella.castro@konradgourmet.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productoId").value(1));

        ArgumentCaptor<CriteriosConsulta> criterios = ArgumentCaptor.forClass(CriteriosConsulta.class);
        verify(repositorio, times(2)).buscar(criterios.capture());
        CriteriosConsulta c = criterios.getAllValues().get(1);
        assertEquals(EstadoSolicitud.PENDIENTE, c.estado());
        assertEquals(1L, c.productoId());
        assertEquals("Marca X", c.marca());
    }

    @Test
    void filtrosMalEscritos() throws Exception {
        mvc.perform(get("/api/solicitudes?desde=2026-11-20&hasta=2026-11-01").with(como("AUXILIAR_COMPRAS", 3, "isabella.castro@konradgourmet.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La fecha inicial no puede ser posterior a la fecha final."));
        mvc.perform(get("/api/solicitudes?estado=ABIERTA").with(como("AUXILIAR_COMPRAS", 3, "isabella.castro@konradgourmet.com")))
                .andExpect(status().isBadRequest());
    }

}
