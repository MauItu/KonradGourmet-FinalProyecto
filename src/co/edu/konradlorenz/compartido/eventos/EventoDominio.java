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

    /** Crea el evento con un id nuevo, una correlación nueva y la hora actual. */
    protected EventoDominio(String usuario) {
        this.id = UUID.randomUUID();
        this.usuario = usuario;
        this.correlacionId = UUID.randomUUID();
        this.ocurridoEn = LocalDateTime.now();
    }

    public UUID id() {
        return id;
    }

    public String usuario() {
        return usuario;
    }

    public UUID correlacionId() {
        return correlacionId;
    }

    public LocalDateTime ocurridoEn() {
        return ocurridoEn;
    }

    public abstract String tipo();

}
