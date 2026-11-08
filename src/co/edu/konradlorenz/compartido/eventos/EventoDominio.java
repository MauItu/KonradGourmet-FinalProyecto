package co.edu.konradlorenz.compartido.eventos;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Nota del diagrama:
 * Eventos confiables
 * • Todo evento hereda id, usuario, correlacionId y fecha de EventoDominio.
 * • Outbox transaccional: KafkaEventPublisher no publica directo: escribe el evento en la tabla outbox
 * del servicio, en la misma transacción que el cambio, y un proceso relé lo publica después en Kafka.
 * • Cada consumidor guarda los id ya procesados y descarta los repetidos (idempotencia).
 * • correlacionId une en las trazas todos los eventos de un mismo recorrido.
 */
public abstract class EventoDominio {

    protected UUID id;
    protected String usuario;
    protected UUID correlacionId;
    protected LocalDateTime ocurridoEn;

    public UUID id() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public String usuario() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public UUID correlacionId() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public LocalDateTime ocurridoEn() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public abstract String tipo();

}
