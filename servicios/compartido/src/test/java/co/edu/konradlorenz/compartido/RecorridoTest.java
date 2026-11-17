package co.edu.konradlorenz.compartido;

import co.edu.konradlorenz.compartido.adaptadores.KafkaEventPublisher;
import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import co.edu.konradlorenz.compartido.infraestructura.ConvertidorEventos;
import co.edu.konradlorenz.compartido.infraestructura.FiltroContexto;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.compartido.infraestructura.InterceptorCorrelacion;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * DEC-64: los eventos llevan el usuario autenticado y el correlacionId del recorrido, sin que el núcleo
 * los reciba. El publicador los toma del contexto que abren el filtro HTTP y el interceptor de Kafka.
 */
class RecorridoTest {

    private static final String PAULA = "paula.herrera@konradgourmet.com";

    private final FormatoEventos formato = new FormatoEventos();
    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);
    private final KafkaEventPublisher publicador = new KafkaEventPublisher("", kafka, formato);

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
        ContextoRecorrido.cerrar();
    }

    @Test
    void enUnaPeticionElEventoQuedaANombreDeQuienLaHizoYConSuCorrelacion() throws Exception {
        autenticar(PAULA);
        UUID recorrido = UUID.randomUUID();
        MockHttpServletRequest peticion = new MockHttpServletRequest("POST", "/api/usuarios");
        peticion.addHeader(FiltroContexto.CABECERA, recorrido.toString());
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        new FiltroContexto().doFilter(peticion, respuesta, (req, res) -> {
            publicador.publicar(new AuditoriaEvent("sistema", "Usuario nuevo registrado"));
            publicador.publicar(new AuditoriaEvent("proveedor@abc.com", "Lo que el núcleo ya firmó no cambia"));
        });

        List<ProducerRecord<String, String>> enviados = enviados();
        AuditoriaEvent primero = formato.deJson(enviados.get(0).value(), AuditoriaEvent.class);
        AuditoriaEvent segundo = formato.deJson(enviados.get(1).value(), AuditoriaEvent.class);
        assertEquals(PAULA, primero.usuario());
        assertEquals("proveedor@abc.com", segundo.usuario());
        assertEquals(recorrido, primero.correlacionId());
        assertEquals(recorrido, segundo.correlacionId());
        assertEquals(recorrido.toString(), respuesta.getHeader(FiltroContexto.CABECERA));
        assertEquals(AuditoriaEvent.class.getName(), new String(enviados.get(0).headers().lastHeader(KafkaEventPublisher.CABECERA_CLASE).value()));
        assertNull(ContextoRecorrido.actual(), "el contexto no queda pegado al hilo después de la petición");
    }

    @Test
    void sinCabeceraLaPeticionAbreUnRecorridoNuevoComunATodosSusEventos() throws Exception {
        autenticar(PAULA);
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        new FiltroContexto().doFilter(new MockHttpServletRequest("POST", "/api/platos"), respuesta, (req, res) -> {
            publicador.publicar(new AuditoriaEvent("sistema", "uno"));
            publicador.publicar(new AuditoriaEvent("sistema", "dos"));
        });
        List<ProducerRecord<String, String>> enviados = enviados();
        UUID a = formato.deJson(enviados.get(0).value(), AuditoriaEvent.class).correlacionId();
        UUID b = formato.deJson(enviados.get(1).value(), AuditoriaEvent.class).correlacionId();
        assertEquals(a, b);
        assertEquals(a.toString(), respuesta.getHeader(FiltroContexto.CABECERA));
    }

    @Test
    void unListenerPublicaEnElRecorridoDelEventoQueRecibe() {
        AuditoriaEvent recibido = new AuditoriaEvent("isabella.torres@konradgourmet.com", "Pedido PED-0251 registrado");
        ConsumerRecord<Object, Object> registro = new ConsumerRecord<>("auditoria.evento", 0, 0L, "k", formato.aJson(recibido));
        InterceptorCorrelacion interceptor = new InterceptorCorrelacion(formato);

        interceptor.intercept(registro, null);
        publicador.publicar(new AuditoriaEvent("sistema", "Consumo acumulado"));
        interceptor.afterRecord(registro, null);

        AuditoriaEvent publicado = formato.deJson(enviados().get(0).value(), AuditoriaEvent.class);
        assertEquals(recibido.correlacionId(), publicado.correlacionId());
        assertEquals("sistema", publicado.usuario(), "un listener no tiene usuario autenticado");
        assertNull(ContextoRecorrido.actual());
    }

    @Test
    void unJobPublicaComoSistemaYSusEventosCompartenRecorrido() {
        autenticar(PAULA); // aunque el hilo tuviera un token, el job no abre contexto con usuario
        ContextoRecorrido.comoRecorridoNuevo(() -> {
            publicador.publicar(new AuditoriaEvent("sistema", "Consumo diario descontado"));
            publicador.publicar(new AuditoriaEvent("sistema", "Otra"));
        });
        List<ProducerRecord<String, String>> enviados = enviados();
        AuditoriaEvent a = formato.deJson(enviados.get(0).value(), AuditoriaEvent.class);
        AuditoriaEvent b = formato.deJson(enviados.get(1).value(), AuditoriaEvent.class);
        assertEquals("sistema", a.usuario());
        assertEquals(a.correlacionId(), b.correlacionId());
    }

    @Test
    void sinContextoElEventoSaleComoLoCreoElNucleo() {
        AuditoriaEvent e = new AuditoriaEvent("sistema", "x");
        publicador.publicar(e);
        AuditoriaEvent publicado = formato.deJson(enviados().get(0).value(), AuditoriaEvent.class);
        assertEquals("sistema", publicado.usuario());
        assertEquals(e.correlacionId(), publicado.correlacionId());
        assertNotEquals(null, publicado.id());
    }

    @Test
    void elConvertidorReconstruyeElEventoPorLaCabecera() {
        publicador.publicar(new AuditoriaEvent("laura.gomez@konradgourmet.com", "Generar orden OC-0012"));
        ProducerRecord<String, String> enviado = enviados().get(0);
        ConsumerRecord<String, String> registro = new ConsumerRecord<>(enviado.topic(), 0, 0L, enviado.key(), enviado.value());
        enviado.headers().forEach(h -> registro.headers().add(h));

        Object payload = new ConvertidorEventos(formato).toMessage(registro, null, null, EventoDominio.class).getPayload();

        AuditoriaEvent evento = (AuditoriaEvent) payload;
        assertEquals("Generar orden OC-0012", evento.accion());
        assertEquals("laura.gomez@konradgourmet.com", evento.usuario());
    }

    private void autenticar(String correo) {
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "RS256").subject("6").claim("correo", correo)
                .claim("rol", "ADMINISTRADOR").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of(), correo));
    }

    @SuppressWarnings("unchecked")
    private List<ProducerRecord<String, String>> enviados() {
        ArgumentCaptor<ProducerRecord<String, String>> c = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafka, atLeastOnce()).send(c.capture());
        return c.getAllValues();
    }


}
