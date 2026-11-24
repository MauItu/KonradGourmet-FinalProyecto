package co.edu.konradlorenz.integracion;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.integracion.adaptadores.salida.MinisterioSoapClient;
import co.edu.konradlorenz.integracion.dominio.PrecioEstandar;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ms-integracion de punta a punta dentro del proceso: Apache CXF publica el endpoint SOAP, la seguridad
 * valida el token, el Proxy guarda el precio y el adaptador traduce lo que responde el cliente del
 * Ministerio (simulado aquí; en la plataforma, el contenedor ministerio). La caché es un mapa en memoria
 * en lugar de Redis, y Kafka y la llave del JWT también son simulados; sin la base, la outbox no se arma.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "eureka.client.enabled=false", "konrad.integracion.url-publica=http://localhost:8080/api/integracion",
        // sin base: la outbox no se arma y la auditoría sale directo al KafkaTemplate simulado
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"})
class PrecioSoapTest {

    @TestConfiguration
    static class CacheEnMemoria {
        @Bean
        @Primary
        Map<Long, PrecioEstandar> cacheDePrueba() {
            return new ConcurrentHashMap<>();
        }
    }

    @LocalServerPort
    private int puerto;
    @Autowired
    private Map<Long, PrecioEstandar> cache;
    @Autowired
    private FormatoEventos formato;
    @MockitoBean
    private MinisterioSoapClient ministerio;
    @MockitoBean
    private KafkaTemplate<String, String> kafka;
    @MockitoBean
    private JwtDecoder jwt;

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void preparar() {
        cache.clear();
        when(ministerio.consultarPrecioEstandar("1")).thenReturn(20000.0);
        when(ministerio.consultarPrecioEstandar("99")).thenThrow(new IllegalArgumentException("El Ministerio no tiene precio para el producto 99"));
        when(jwt.decode(anyString())).thenAnswer(i -> {
            String rol = i.getArgument(0);
            return Jwt.withTokenValue(rol).header("alg", "RS256").subject("3").claim("rol", rol)
                    .claim("correo", rol.toLowerCase() + "@konradgourmet.com").issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(600)).build();
        });
    }

    private HttpResponse<String> soap(String token, String productoId, UUID correlacion) throws Exception {
        String sobre = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ab=\"http://abastecimiento.konradgourmet.com/\">"
                + "<soap:Body><ab:consultarPrecioSugerido><productoId>" + productoId + "</productoId></ab:consultarPrecioSugerido></soap:Body></soap:Envelope>";
        HttpRequest.Builder p = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/integracion/abastecimiento"))
                .header("Content-Type", "text/xml; charset=utf-8").header("SOAPAction", "\"\"")
                .header("X-Correlation-Id", correlacion.toString())
                .POST(HttpRequest.BodyPublishers.ofString(sobre));
        if (token != null) {
            p.header("Authorization", "Bearer " + token);
        }
        return http.send(p.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String ruta, String token) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Authorization", "Bearer " + token).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void elSoapRespondeElPrecioYLaSegundaVezLoDaLaCache() throws Exception {
        UUID recorrido = UUID.randomUUID();
        HttpResponse<String> primera = soap("AUXILIAR_COMPRAS", "1", recorrido);
        assertEquals(200, primera.statusCode(), primera.body());
        assertTrue(primera.body().contains("<precioSugerido>20000.0</precioSugerido>"), primera.body());
        verify(ministerio, times(1)).consultarPrecioEstandar("1");
        assertEquals(0, new BigDecimal("20000").compareTo(cache.get(1L).valor()), "el precio quedó en la caché");

        HttpResponse<String> segunda = soap("AUXILIAR_COMPRAS", "1", UUID.randomUUID());
        assertTrue(segunda.body().contains("<precioSugerido>20000.0</precioSugerido>"), segunda.body());
        verify(ministerio, times(1)).consultarPrecioEstandar("1"); // sigue en 1: respondió la caché

        // la auditoría de la consulta va a nombre de quien llamó, con la correlación de su petición (DEC-64)
        ArgumentCaptor<ProducerRecord<String, String>> enviados = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafka, times(2)).send(enviados.capture());
        AuditoriaEvent auditoria = formato.deJson(enviados.getAllValues().get(0).value(), AuditoriaEvent.class);
        assertEquals("auditoria.evento", enviados.getAllValues().get(0).topic());
        assertEquals("auxiliar_compras@konradgourmet.com", auditoria.usuario());
        assertEquals(recorrido, auditoria.correlacionId());
        assertEquals("Precio sugerido consultado: producto 1", auditoria.accion());
    }

    @Test
    void unPrecioVencidoSeVuelveAPedir() throws Exception {
        cache.put(1L, new PrecioEstandar(1L, new BigDecimal("18000"), LocalDateTime.now().minusDays(2)));
        HttpResponse<String> r = soap("AUXILIAR_COMPRAS", "1", UUID.randomUUID());
        assertTrue(r.body().contains("<precioSugerido>20000.0</precioSugerido>"), r.body());
        verify(ministerio, times(1)).consultarPrecioEstandar("1");
    }

    @Test
    void sinPrecioDelMinisterioEsUnSoapFault() throws Exception {
        HttpResponse<String> r = soap("AUXILIAR_COMPRAS", "99", UUID.randomUUID());
        assertEquals(500, r.statusCode());
        assertTrue(r.body().contains("Fault") && r.body().contains("No hay precio estándar disponible para el producto 99."), r.body());
    }

    @Test
    void elSoapPideToken() throws Exception {
        assertEquals(401, soap(null, "1", UUID.randomUUID()).statusCode());
    }

    @Test
    void elWsdlAnunciaLaDireccionDelGateway() throws Exception {
        HttpResponse<String> r = get("/api/integracion/abastecimiento?wsdl", "MESERO");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("consultarPrecioSugerido"), r.body());
        assertTrue(r.body().contains("location=\"http://localhost:8080/api/integracion/abastecimiento\""), r.body());
    }

    @Test
    void elRestInternoEsSoloDeCompras() throws Exception {
        HttpResponse<String> r = get("/interno/precios/1", "DIRECTOR_COMPRAS");
        assertEquals(200, r.statusCode());
        assertEquals("20000.0", r.body());
        assertEquals(403, get("/interno/precios/1", "MESERO").statusCode());
        clearInvocations(kafka);
        HttpResponse<String> sinPrecio = get("/interno/precios/99", "AUXILIAR_COMPRAS");
        assertEquals(400, sinPrecio.statusCode());
        assertTrue(sinPrecio.body().contains("no tiene precio"), sinPrecio.body());
        verify(kafka, times(0)).send(any(ProducerRecord.class));
    }

}
