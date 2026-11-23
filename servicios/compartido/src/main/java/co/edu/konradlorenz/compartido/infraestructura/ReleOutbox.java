package co.edu.konradlorenz.compartido.infraestructura;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * El proceso relé de la outbox (nota n_outbox de la 4a, DEC-104): lee los eventos guardados en orden, los
 * publica en Kafka esperando la confirmación del broker y solo entonces los borra, todo en una transacción.
 * Si Kafka no responde, la transacción se deshace, los eventos quedan en la tabla y se vuelven a intentar;
 * un evento puede salir dos veces, y por eso los consumidores descartan los repetidos (EventosProcesados).
 *
 * Con varias réplicas, un candado de PostgreSQL (pg_try_advisory_xact_lock) deja publicar a una sola a la
 * vez, así los eventos salen en el orden en que se guardaron; FOR UPDATE SKIP LOCKED evita que dos lotes
 * tomen las mismas filas. Despierta apenas se confirma una transacción con eventos (OutboxEventos) y, si
 * no, revisa la tabla cada segundo.
 */
public class ReleOutbox implements SmartLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(ReleOutbox.class);
    /** El candado es por base: cada servicio tiene la suya. */
    private static final long CANDADO = 0x4B475F4F5554424FL; // «KG_OUTBO»
    private static final int LOTE = 200;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaccion;
    private final KafkaTemplate<String, String> kafka;
    private final String claseHeader;
    private final Semaphore aviso = new Semaphore(0);
    private volatile boolean activo;
    private Thread hilo;

    public ReleOutbox(JdbcTemplate jdbc, PlatformTransactionManager transacciones, KafkaTemplate<String, String> kafka, String claseHeader) {
        this.jdbc = jdbc;
        this.transaccion = new TransactionTemplate(transacciones);
        this.kafka = kafka;
        this.claseHeader = claseHeader;
    }

    /** Hay eventos nuevos confirmados: que no espere al siguiente segundo. */
    public void despertar() {
        aviso.release();
    }

    /** Publica lo que haya en la tabla; devuelve cuántos eventos salieron. */
    public int publicarPendientes() {
        int total = 0;
        int enviados;
        do {
            Integer n = transaccion.execute(t -> lote());
            enviados = n == null ? 0 : n;
            total += enviados;
        } while (enviados == LOTE);
        return total;
    }

    private int lote() {
        Boolean candado = jdbc.queryForObject("SELECT pg_try_advisory_xact_lock(?)", Boolean.class, CANDADO);
        if (!Boolean.TRUE.equals(candado)) {
            return 0; // otra réplica está publicando
        }
        List<Fila> filas = jdbc.query("SELECT secuencia, topico, clave, valor, clase FROM outbox ORDER BY secuencia LIMIT ? FOR UPDATE SKIP LOCKED",
                (r, i) -> new Fila(r.getLong(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5)), LOTE);
        for (Fila f : filas) {
            ProducerRecord<String, String> registro = new ProducerRecord<>(f.topico(), f.clave(), f.valor());
            if (f.clase() != null) {
                registro.headers().add(claseHeader, f.clase().getBytes(StandardCharsets.UTF_8));
            }
            try {
                kafka.send(registro).get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Se interrumpió la publicación de la outbox", e);
            } catch (Exception e) {
                throw new IllegalStateException("Kafka no confirmó el evento " + f.secuencia() + " de " + f.topico(), e);
            }
            jdbc.update("DELETE FROM outbox WHERE secuencia = ?", f.secuencia());
        }
        return filas.size();
    }

    private void ciclo() {
        long espera = 1_000;
        while (activo) {
            try {
                if (aviso.tryAcquire(espera, TimeUnit.MILLISECONDS)) {
                    aviso.drainPermits();
                }
                if (!activo) {
                    break;
                }
                publicarPendientes();
                espera = 1_000;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (RuntimeException e) {
                // Kafka o la base no responden: los eventos siguen en la tabla; se reintenta con más espera
                LOG.warn("La outbox no pudo publicar; se reintenta: {}", e.getMessage());
                espera = Math.min(espera * 2, 10_000);
            }
        }
    }

    @Override
    public synchronized void start() {
        if (activo) {
            return;
        }
        activo = true;
        hilo = new Thread(this::ciclo, "rele-outbox");
        hilo.setDaemon(true);
        hilo.start();
    }

    @Override
    public synchronized void stop() {
        activo = false;
        aviso.release();
        if (hilo != null) {
            try {
                hilo.join(5_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return activo;
    }

    /** Arranca después de Kafka y de la base y se detiene antes que ellos. */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 100;
    }

    private record Fila(long secuencia, String topico, String clave, String valor, String clase) {
    }

}
