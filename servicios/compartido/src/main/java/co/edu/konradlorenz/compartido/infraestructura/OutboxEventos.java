package co.edu.konradlorenz.compartido.infraestructura;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;

/**
 * La tabla outbox del servicio (nota n_outbox de la 4a, DEC-104): KafkaEventPublisher no publica directo,
 * guarda aquí el registro ya armado (tópico, clave, JSON y la clase del evento) en la misma transacción que
 * el cambio que lo origina. Si la transacción se deshace, el evento no sale; si se confirma, ReleOutbox lo
 * publica, aunque Kafka no esté disponible en ese momento. Fuera del diagrama, como el resto de la
 * infraestructura de compartido.
 */
public class OutboxEventos {

    private final JdbcTemplate jdbc;
    private final ReleOutbox rele;

    public OutboxEventos(JdbcTemplate jdbc, ReleOutbox rele) {
        this.jdbc = jdbc;
        this.rele = rele;
    }

    /** Guarda el registro en la transacción en curso (o en una propia, si no hay) y avisa al relé al confirmar. */
    public void guardar(ProducerRecord<String, String> registro, String claseHeader) {
        Header clase = registro.headers().lastHeader(claseHeader);
        jdbc.update("INSERT INTO outbox (topico, clave, valor, clase) VALUES (?, ?, ?, ?)", registro.topic(), registro.key(),
                registro.value(), clase == null ? null : new String(clase.value(), StandardCharsets.UTF_8));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rele.despertar();
                }
            });
        } else {
            rele.despertar();
        }
    }

}
