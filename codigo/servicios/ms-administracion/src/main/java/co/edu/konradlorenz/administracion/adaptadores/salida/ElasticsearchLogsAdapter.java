package co.edu.konradlorenz.administracion.adaptadores.salida;

import co.edu.konradlorenz.administracion.dominio.LogError;
import co.edu.konradlorenz.administracion.puertos.salida.LogsPort;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * «ELK · Elasticsearch · HTTPS»
 *
 * Consulta el índice logs-errores, donde cada servicio deja sus errores (AppenderElasticsearch de compartido,
 * DEC-84): los más recientes primero, de un servicio o de todos (HU-22). Si el índice todavía no existe es porque
 * ningún servicio ha fallado: la lista sale vacía. En local Elasticsearch va por HTTP, sin TLS (DEC-58).
 */
@Component
public class ElasticsearchLogsAdapter implements LogsPort {

    static final String INDICE = "logs-errores";
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final RestClient elasticsearch;
    private final int maximo;

    public ElasticsearchLogsAdapter(@Value("${konrad.logs.elasticsearch:http://elasticsearch:9200}") String url,
                                    @Value("${konrad.administracion.maximo-logs:200}") int maximo) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(3_000);
        tiempos.setReadTimeout(10_000);
        this.elasticsearch = RestClient.builder().baseUrl(url).requestFactory(tiempos).build();
        this.maximo = maximo;
    }

    @Override
    public List<LogError> logs(String servicio) {
        Map<String, Object> consulta = Map.of(
                "size", maximo,
                "sort", List.of(Map.of("fechaHora", Map.of("order", "desc"))),
                "query", servicio == null ? Map.of("match_all", Map.of()) : Map.of("term", Map.of("servicio", servicio)));
        JsonNode respuesta;
        try {
            respuesta = elasticsearch.post().uri("/" + INDICE + "/_search")
                    .contentType(MediaType.APPLICATION_JSON).body(consulta)
                    .retrieve().body(JsonNode.class);
        } catch (HttpClientErrorException.NotFound sinIndice) {
            return List.of();
        }
        List<LogError> errores = new ArrayList<>();
        for (JsonNode hit : respuesta.path("hits").path("hits")) {
            JsonNode d = hit.path("_source");
            errores.add(new LogError(d.path("servicio").asText(), d.path("mensaje").asText(),
                    OffsetDateTime.parse(d.path("fechaHora").asText()).atZoneSameInstant(BOGOTA).toLocalDateTime()));
        }
        return errores;
    }

}
