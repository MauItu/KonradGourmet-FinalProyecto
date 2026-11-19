package co.edu.konradlorenz.integracion.configuracion;

import co.edu.konradlorenz.integracion.dominio.PrecioEstandar;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * El mapa {Redis} de PrecioReferenciaCacheProxy: cada precio es una llave «precio-referencia:{producto}»
 * con su JSON, que comparten todas las réplicas de ms-integracion. La llave vence en un día, el mismo
 * plazo con que el Proxy da por vencido un precio (supuesto S3): Redis no guarda precios viejos.
 */
public class MapaPreciosRedis extends AbstractMap<Long, PrecioEstandar> {

    public static final String PREFIJO = "precio-referencia:";
    public static final Duration VIGENCIA = Duration.ofDays(1);

    private final StringRedisTemplate redis;
    private final ObjectMapper json = new ObjectMapper();

    public MapaPreciosRedis(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public PrecioEstandar get(Object llave) {
        if (!(llave instanceof Long producto)) {
            return null;
        }
        String guardado = redis.opsForValue().get(PREFIJO + producto);
        return guardado == null ? null : leer(guardado);
    }

    @Override
    public PrecioEstandar put(Long producto, PrecioEstandar precio) {
        ObjectNode nodo = json.createObjectNode()
                .put("productoId", precio.productoId())
                .put("valor", precio.valor().toPlainString())
                .put("consultadoEn", precio.consultadoEn().toString());
        redis.opsForValue().set(PREFIJO + producto, nodo.toString(), VIGENCIA);
        return null;
    }

    @Override
    public PrecioEstandar remove(Object llave) {
        PrecioEstandar antes = get(llave);
        redis.delete(PREFIJO + llave);
        return antes;
    }

    @Override
    public boolean containsKey(Object llave) {
        return get(llave) != null;
    }

    /** Los precios vigentes (para depurar; el Proxy solo usa get y put). */
    @Override
    public Set<Entry<Long, PrecioEstandar>> entrySet() {
        Set<Entry<Long, PrecioEstandar>> todos = new LinkedHashSet<>();
        Set<String> llaves = redis.keys(PREFIJO + "*");
        if (llaves != null) {
            for (String llave : llaves) {
                Long producto = Long.valueOf(llave.substring(PREFIJO.length()));
                PrecioEstandar p = get(producto);
                if (p != null) {
                    todos.add(new SimpleImmutableEntry<>(producto, p));
                }
            }
        }
        return todos;
    }

    private PrecioEstandar leer(String texto) {
        try {
            JsonNode n = json.readTree(texto);
            return new PrecioEstandar(n.get("productoId").asLong(), new BigDecimal(n.get("valor").asText()),
                    LocalDateTime.parse(n.get("consultadoEn").asText()));
        } catch (JsonProcessingException | RuntimeException e) {
            return null; // un valor dañado cuenta como no guardado: el Proxy vuelve a consultar
        }
    }

}
