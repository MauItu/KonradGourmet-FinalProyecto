package co.edu.konradlorenz.compartido;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import co.edu.konradlorenz.compartido.infraestructura.AppenderElasticsearch;
import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DEC-84: cada error del servicio llega a Elasticsearch (índice logs-errores) con el servicio, el mensaje y la
 * fecha y hora de LogError, más la correlación y el usuario del recorrido. Un servidor HTTP local hace de
 * Elasticsearch y guarda lo que recibe.
 */
class AppenderElasticsearchTest {

    private final List<String> recibido = new CopyOnWriteArrayList<>();
    private HttpServer elasticsearch;
    private AppenderElasticsearch appender;

    @AfterEach
    void apagar() {
        if (appender != null) {
            appender.stop();
        }
        if (elasticsearch != null) {
            elasticsearch.stop(0);
        }
        ContextoRecorrido.cerrar();
    }

    @Test
    void soloLosErroresLleganConSuRecorrido() throws Exception {
        elasticsearch = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        elasticsearch.createContext("/", intercambio -> {
            String cuerpo = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            recibido.add(intercambio.getRequestMethod() + " " + intercambio.getRequestURI() + "\n" + cuerpo);
            byte[] r = "{\"errors\":false}".getBytes(StandardCharsets.UTF_8);
            intercambio.sendResponseHeaders(200, r.length);
            intercambio.getResponseBody().write(r);
            intercambio.close();
        });
        elasticsearch.start();

        LoggerContext contexto = new LoggerContext();
        appender = new AppenderElasticsearch("http://127.0.0.1:" + elasticsearch.getAddress().getPort() + "/", "ms-pedidos");
        appender.setContext(contexto);
        appender.start();
        Logger log = contexto.getLogger("co.edu.konradlorenz.pedidos");
        log.addAppender(appender);

        UUID correlacion = UUID.randomUUID();
        ContextoRecorrido.abrir("isabella.torres@konradgourmet.com", correlacion);
        log.info("Pedido registrado");
        log.warn("El pedido tardó");
        log.error("No se pudo guardar el pedido", new IllegalStateException("Fallo al guardar",
                new SQLException("Connection refused")));

        for (int i = 0; i < 50 && recibido.size() < 2; i++) {
            Thread.sleep(100);
        }
        assertEquals(2, recibido.size(), "crea el índice y envía un lote: " + recibido);
        assertTrue(recibido.get(0).startsWith("PUT /logs-errores\n"), "primero el índice con su mapeo");
        assertTrue(recibido.get(0).contains("\"servicio\":{\"type\":\"keyword\"}"));
        String[] lote = recibido.get(1).split("\n");
        assertEquals("POST /_bulk", lote[0]);
        assertEquals(3, lote.length, "solo el error: ni INFO ni WARN");
        JsonNode d = new ObjectMapper().readTree(lote[2]);
        assertEquals("ms-pedidos", d.get("servicio").asText());
        assertEquals("ERROR", d.get("nivel").asText());
        assertEquals("No se pudo guardar el pedido · java.sql.SQLException: Connection refused", d.get("mensaje").asText(),
                "el mensaje del log y la causa más profunda");
        assertTrue(d.get("fechaHora").asText().endsWith("-05:00"), "en hora de Bogotá: " + d.get("fechaHora"));
        assertEquals(correlacion.toString(), d.get("correlacionId").asText());
        assertEquals("isabella.torres@konradgourmet.com", d.get("usuario").asText());
        assertTrue(d.get("traza").asText().contains("IllegalStateException: Fallo al guardar"));
        assertEquals(Thread.currentThread().getName(), d.get("hilo").asText());
    }

    @Test
    void sinElasticsearchElServicioSigue() throws IOException {
        LoggerContext contexto = new LoggerContext();
        appender = new AppenderElasticsearch("http://127.0.0.1:9", "ms-pedidos");
        appender.setContext(contexto);
        appender.start();
        Logger log = contexto.getLogger("co.edu.konradlorenz.pedidos");
        log.addAppender(appender);
        long inicio = System.nanoTime();
        for (int i = 0; i < 2_000; i++) {
            log.error("Error " + i);
        }
        assertTrue((System.nanoTime() - inicio) / 1_000_000 < 2_000, "registrar un error no espera a Elasticsearch");
    }

}
