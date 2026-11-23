package co.edu.konradlorenz.compartido.infraestructura;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Los id de los eventos que cada consumidor ya procesó (tabla evento_procesado, nota n_outbox de la 4a:
 * «cada consumidor guarda los id ya procesados y descarta los repetidos»; DEC-104). Kafka entrega al menos
 * una vez y la outbox puede publicar dos veces un evento: sin esto, un pedido repetido se descontaría dos
 * veces del inventario. Se marca en la misma transacción que el cambio del consumidor; la clave es el id
 * del evento y el consumidor, porque varios consumidores de un servicio leen el mismo tópico.
 */
public class EventosProcesados {

    private final JdbcTemplate jdbc;

    public EventosProcesados(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** true si el evento es nuevo para ese consumidor (y queda marcado); false si ya lo había procesado. */
    public boolean marcar(EventoDominio evento, String consumidor) {
        return jdbc.update("INSERT INTO evento_procesado (id, consumidor, topico) VALUES (?, ?, ?) ON CONFLICT (id, consumidor) DO NOTHING",
                evento.id(), consumidor, evento.tipo()) == 1;
    }

    /** Si el consumidor ya procesó el evento (para el que marca solo después de terminar, como ms-notificaciones). */
    public boolean procesado(EventoDominio evento, String consumidor) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM evento_procesado WHERE id = ? AND consumidor = ?", Integer.class,
                evento.id(), consumidor);
        return n != null && n > 0;
    }

}
