package co.edu.konradlorenz.notificaciones.configuracion;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.infraestructura.ConvertidorEventos;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.notificaciones.puertos.entrada.NotificarUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Vuelve a procesar la cola de errores notificaciones.dlq (DEC-81). Nota n_fallos de la 4d: tras dos
 * reintentos el evento pasa a la cola de errores, que se vuelve a procesar cada 5 minutos, hasta tres veces.
 * Fuera del diagrama, como CanalAlarmas: es infraestructura de NotificacionListeners.
 *
 * Lee la cola con su propio grupo de consumo y acuse manual (fábrica «fabricaReproceso»). Cada registro
 * espera a llevar la espera en la cola: si le falta, nack(lo que falta) pausa la partición sin bloquear el
 * hilo y Kafka lo vuelve a entregar después. Entonces llama a notificarEvento() con el evento original: su
 * clase viaja en la cabecera kg-evento, que la cola conserva, y su recorrido lo abre InterceptorCorrelacion
 * con la correlación del evento, así que el envío queda en la traza con la correlación original.
 *
 * Si vuelve a fallar, el núcleo ya dejó el fallo en la traza (registrarFallo) y el registro se publica otra
 * vez en la cola con la ronda en la cabecera kg-reproceso (y el tópico de origen, kafka_dlt-original-topic,
 * que la cola ya trae); después de la tercera ronda se deja ahí. La entrega es «al menos una vez»: si el
 * evento tenía varios destinatarios y falló con uno, se reenvía a todos.
 */
@Component
public class ReprocesoColaErrores {

    /** Cuántas rondas de reproceso ya falló el registro (sin la cabecera, ninguna). */
    public static final String CABECERA_RONDA = "kg-reproceso";
    private static final Logger LOG = LoggerFactory.getLogger(ReprocesoColaErrores.class);
    /** Kafka entrega el registro un poco antes o después de lo pedido: con menos de esto, ya se procesa. */
    private static final Duration HOLGURA = Duration.ofSeconds(1);

    private final NotificarUseCase casoDeUso;
    private final ConvertidorEventos convertidor;
    private final KafkaTemplate<String, String> kafka;
    private final String topico;
    private final Duration espera;
    private final int rondas;
    private final Clock reloj;

    @Autowired
    public ReprocesoColaErrores(NotificarUseCase casoDeUso, FormatoEventos formato, KafkaTemplate<String, String> kafka,
                                @Value("${konrad.notificaciones.topico-errores}") String topico,
                                @Value("${konrad.notificaciones.espera-cola-errores}") Duration espera,
                                @Value("${konrad.notificaciones.rondas-cola-errores}") int rondas) {
        this(casoDeUso, new ConvertidorEventos(formato), kafka, topico, espera, rondas, Clock.systemUTC());
    }

    ReprocesoColaErrores(NotificarUseCase casoDeUso, ConvertidorEventos convertidor, KafkaTemplate<String, String> kafka, String topico,
                         Duration espera, int rondas, Clock reloj) {
        this.casoDeUso = casoDeUso;
        this.convertidor = convertidor;
        this.kafka = kafka;
        this.topico = topico;
        this.espera = espera;
        this.rondas = rondas;
        this.reloj = reloj;
    }

    @KafkaListener(id = "reproceso-cola-errores", topics = "${konrad.notificaciones.topico-errores}",
            groupId = "ms-notificaciones-reproceso", containerFactory = "fabricaReproceso")
    public void alRecibir(ConsumerRecord<String, String> registro, Acknowledgment acuse) {
        Duration falta = Duration.between(reloj.instant(), Instant.ofEpochMilli(registro.timestamp()).plus(espera));
        if (falta.compareTo(HOLGURA) > 0) {
            acuse.nack(falta);
            return;
        }
        int ronda = rondasFallidas(registro) + 1;
        String origen = cabecera(registro, KafkaHeaders.DLT_ORIGINAL_TOPIC);
        try {
            EventoDominio evento = convertidor.evento(registro);
            casoDeUso.notificarEvento(evento);
            LOG.info("Cola de errores: el evento {} de {} se entregó en la ronda {}", evento.id(), origen, ronda);
        } catch (RuntimeException e) {
            if (ronda < rondas) {
                volverALaCola(registro, ronda);
                LOG.warn("Cola de errores: ronda {} de {} fallida para un evento de {} ({}); vuelve a la cola", ronda, rondas, origen, e.getMessage());
            } else {
                LOG.error("Cola de errores: un evento de {} sigue fallando después de {} rondas; queda en la cola ({})", origen, rondas, e.getMessage());
            }
        }
        acuse.acknowledge();
    }

    /** Publica la copia antes de confirmar el original: si el broker no la recibe, el original se vuelve a leer. */
    private void volverALaCola(ConsumerRecord<String, String> registro, int ronda) {
        RecordHeaders cabeceras = new RecordHeaders();
        for (Header h : registro.headers()) {
            if (!CABECERA_RONDA.equals(h.key())) {
                cabeceras.add(h);
            }
        }
        cabeceras.add(CABECERA_RONDA, String.valueOf(ronda).getBytes(StandardCharsets.UTF_8));
        try {
            kafka.send(new ProducerRecord<>(topico, null, registro.key(), registro.value(), cabeceras)).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió el envío a la cola de errores", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("No se pudo devolver el evento a la cola de errores", e);
        }
    }

    static int rondasFallidas(ConsumerRecord<?, ?> registro) {
        String valor = cabecera(registro, CABECERA_RONDA);
        try {
            return valor == null ? 0 : Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String cabecera(ConsumerRecord<?, ?> registro, String nombre) {
        Header h = registro.headers().lastHeader(nombre);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }

}
