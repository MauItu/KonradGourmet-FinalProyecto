package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.puertos.salida.AlarmaPushPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * «WebSocket → V05»
 *
 * Nota del diagrama: Con varias réplicas, empujar() publica la alarma en un canal Redis y cada réplica la
 * entrega a las conexiones que tenga abiertas; V05 lee las alertas activas al abrirse (PD-01).
 *
 * La entrega por WebSocket la hace CanalAlarmas, que escucha el canal en cada réplica.
 */
@Component
public class AlarmaPushAdapter implements AlarmaPushPort {

    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final String canal;

    public AlarmaPushAdapter(StringRedisTemplate redis, ObjectMapper json, @Value("${konrad.notificaciones.canal-alarmas}") String canal) {
        this.redis = redis;
        this.json = json;
        this.canal = canal;
    }

    @Override
    public void empujar(String destino, String contenido) {
        Map<String, String> alarma = new LinkedHashMap<>();
        alarma.put("destino", destino);
        alarma.put("contenido", contenido);
        alarma.put("fechaHora", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
        try {
            redis.convertAndSend(canal, json.writeValueAsString(alarma));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo armar la alarma", e);
        }
    }

}
