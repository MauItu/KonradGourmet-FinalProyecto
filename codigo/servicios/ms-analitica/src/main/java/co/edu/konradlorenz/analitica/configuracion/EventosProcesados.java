package co.edu.konradlorenz.analitica.configuracion;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Los id de los eventos que ya dejaron sus hechos (tabla evento_procesado): Kafka entrega al menos una vez, y un
 * pedido contado dos veces cambiaría el tablero (nota de EventoDominio: los consumidores descartan los repetidos).
 * Se marca en la misma transacción que los hechos. Fuera del diagrama.
 */
@Component
public class EventosProcesados {

    private final JdbcTemplate jdbc;

    public EventosProcesados(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** true si el evento es nuevo (y queda marcado); false si ya se había procesado. */
    public boolean marcar(EventoDominio evento) {
        return jdbc.update("INSERT INTO evento_procesado (id, topico) VALUES (?, ?) ON CONFLICT (id) DO NOTHING",
                evento.id(), evento.tipo()) == 1;
    }

}
