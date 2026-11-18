package co.edu.konradlorenz.compartido.infraestructura;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Envía a Elasticsearch cada error (nivel ERROR) del servicio (DEC-84, requisito de mantenimiento 2): lo que
 * ElasticsearchLogsAdapter de ms-administracion consulta para la ventana de logs (HU-22).
 *
 * Cada error se guarda como un documento del índice logs-errores con el servicio, el mensaje, la fecha y hora
 * (los campos de LogError), y además el nivel, el logger, el hilo, la traza de la excepción y, si la hay, la
 * correlación y el usuario del recorrido (DEC-64). El envío es asíncrono, por lotes y desde un hilo propio:
 * el servicio no espera a Elasticsearch. Si Elasticsearch no responde, el lote se intenta tres veces y se
 * descarta; el error sigue en el registro del contenedor. La cola guarda hasta 1.000 errores.
 */
public class AppenderElasticsearch extends AppenderBase<ILoggingEvent> {

    public static final String NOMBRE = "ELASTICSEARCH";
    public static final String INDICE = "logs-errores";
    static final int MAXIMO_MENSAJE = 2_000;
    static final int MAXIMO_TRAZA = 8_000;
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    /** Los campos por los que se busca son keyword; la fecha, date. */
    static final String MAPEO = """
            {"mappings":{"properties":{
              "servicio":{"type":"keyword"},"nivel":{"type":"keyword"},"mensaje":{"type":"text"},
              "fechaHora":{"type":"date"},"logger":{"type":"keyword"},"hilo":{"type":"keyword"},
              "traza":{"type":"text","index":false},"correlacionId":{"type":"keyword"},"usuario":{"type":"keyword"}}}}""";

    private final String url;
    private final String servicio;
    private final ObjectMapper json = new ObjectMapper();
    private final BlockingQueue<String> cola = new ArrayBlockingQueue<>(1_000);
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private volatile Thread hilo;
    private volatile boolean indiceListo;

    public AppenderElasticsearch(String url, String servicio) {
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.servicio = servicio;
        setName(NOMBRE);
    }

    @Override
    public void start() {
        hilo = new Thread(this::enviarSiempre, "logs-elasticsearch");
        hilo.setDaemon(true);
        hilo.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        Thread h = hilo;
        hilo = null;
        if (h != null) {
            h.interrupt();
        }
    }

    @Override
    protected void append(ILoggingEvent evento) {
        // Solo errores, y nunca lo que registre el propio envío (evita un ciclo si Elasticsearch falla).
        if (!evento.getLevel().isGreaterOrEqual(Level.ERROR) || Thread.currentThread() == hilo
                || evento.getLoggerName().startsWith("jdk.internal.httpclient")) {
            return;
        }
        if (!cola.offer(documento(evento))) {
            addWarn("La cola de logs para Elasticsearch está llena: se descarta un error de " + servicio);
        }
    }

    /** El documento JSON de un error. */
    String documento(ILoggingEvent evento) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("servicio", servicio);
        d.put("nivel", evento.getLevel().toString());
        d.put("mensaje", recortar(mensaje(evento), MAXIMO_MENSAJE));
        d.put("fechaHora", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(evento.getInstant().atZone(BOGOTA)));
        d.put("logger", evento.getLoggerName());
        d.put("hilo", evento.getThreadName());
        IThrowableProxy error = evento.getThrowableProxy();
        if (error != null) {
            d.put("traza", recortar(ThrowableProxyUtil.asString(error), MAXIMO_TRAZA));
        }
        ContextoRecorrido.Datos recorrido = ContextoRecorrido.actual();
        if (recorrido != null) {
            d.put("correlacionId", String.valueOf(recorrido.correlacionId()));
            if (recorrido.usuario() != null) {
                d.put("usuario", recorrido.usuario());
            }
        }
        try {
            return json.writeValueAsString(d);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo convertir el error a JSON", e);
        }
    }

    /** El mensaje del log y, si hay excepción, la causa más profunda: lo que el administrador necesita ver primero. */
    static String mensaje(ILoggingEvent evento) {
        String m = evento.getFormattedMessage();
        IThrowableProxy e = evento.getThrowableProxy();
        if (e == null) {
            return m;
        }
        while (e.getCause() != null) {
            e = e.getCause();
        }
        String causa = e.getClassName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
        return m == null || m.isBlank() ? causa : m + " · " + causa;
    }

    private static String recortar(String texto, int maximo) {
        return texto == null || texto.length() <= maximo ? texto : texto.substring(0, maximo) + "…";
    }

    private void enviarSiempre() {
        while (hilo == Thread.currentThread()) {
            try {
                String primero = cola.poll(1, TimeUnit.SECONDS);
                if (primero == null) {
                    continue;
                }
                List<String> lote = new ArrayList<>();
                lote.add(primero);
                cola.drainTo(lote, 99);
                enviar(lote);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void enviar(List<String> lote) throws InterruptedException {
        StringBuilder ndjson = new StringBuilder();
        for (String d : lote) {
            ndjson.append("{\"index\":{\"_index\":\"").append(INDICE).append("\"}}\n").append(d).append('\n');
        }
        for (int intento = 1; intento <= 3; intento++) {
            try {
                crearIndice();
                HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(url + "/_bulk"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/x-ndjson")
                        .POST(HttpRequest.BodyPublishers.ofString(ndjson.toString())).build(), HttpResponse.BodyHandlers.ofString());
                if (r.statusCode() < 300 && !r.body().contains("\"errors\":true")) {
                    return;
                }
                addWarn("Elasticsearch rechazó un lote de " + lote.size() + " errores: " + r.statusCode());
            } catch (java.io.IOException e) {
                addWarn("No se pudo enviar a Elasticsearch un lote de " + lote.size() + " errores (intento " + intento + ")", e);
            }
            Thread.sleep(5_000L * intento);
        }
        addError("Se descartó un lote de " + lote.size() + " errores de " + servicio + ": Elasticsearch no respondió");
    }

    /** El índice con su mapeo, una vez; si otro servicio ya lo creó, Elasticsearch responde 400 y está bien. */
    private void crearIndice() throws java.io.IOException, InterruptedException {
        if (indiceListo) {
            return;
        }
        HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(url + "/" + INDICE))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(MAPEO)).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() < 300 || r.body().contains("resource_already_exists_exception")) {
            indiceListo = true;
        } else {
            throw new java.io.IOException("Elasticsearch no creó el índice " + INDICE + ": " + r.statusCode());
        }
    }

}
