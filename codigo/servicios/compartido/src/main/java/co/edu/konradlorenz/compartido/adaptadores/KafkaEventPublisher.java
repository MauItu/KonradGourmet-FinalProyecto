package co.edu.konradlorenz.compartido.adaptadores;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.compartido.infraestructura.OutboxEventos;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * «Publish-Subscribe · publicador» [Kafka]
 *
 * Publica cada evento en el tópico que dice su tipo() (más el prefijo, vacío en local), en JSON y con
 * una clave: Kafka solo garantiza el orden dentro de una partición, y la clave hace que los eventos de
 * la misma entidad caigan en la misma partición (FormatoEventos.clave()).
 *
 * Antes de publicar completa el evento con el recorrido (DEC-64): si el núcleo puso «sistema» y hay un
 * usuario autenticado, va su correo, y el correlacionId es el de la petición o el del evento que la
 * originó (ContextoRecorrido). Sin contexto (por ejemplo, un job) el evento sale como lo creó el núcleo.
 * La clase del evento viaja en una cabecera para que el consumidor lo reconstruya (ConvertidorEventos).
 *
 * Outbox transaccional (nota n_outbox de la 4a, DEC-104): en un servicio con base, el registro no va
 * directo a Kafka, se guarda en la tabla outbox en la misma transacción que el cambio (OutboxEventos) y
 * el relé lo publica después (ReleOutbox). Sin base (las pruebas de un controlador solo, por ejemplo), se
 * envía directo con el KafkaTemplate.
 */
public class KafkaEventPublisher implements EventPublisherPort {

    public static final String CABECERA_CLASE = "kg-evento";
    private static final String SISTEMA = "sistema";

    private final String prefijoTopico;
    private final Consumer<ProducerRecord<String, String>> salida;
    private final FormatoEventos formato;

    /** Publica directo en Kafka (sin outbox). */
    public KafkaEventPublisher(String prefijoTopico, KafkaTemplate<String, String> kafka, FormatoEventos formato) {
        this.prefijoTopico = prefijoTopico;
        this.salida = kafka::send;
        this.formato = formato;
    }

    /** Guarda cada evento en la outbox del servicio, en la transacción en curso; el relé lo publica. */
    public KafkaEventPublisher(String prefijoTopico, OutboxEventos outbox, FormatoEventos formato) {
        this.prefijoTopico = prefijoTopico;
        this.salida = registro -> outbox.guardar(registro, CABECERA_CLASE);
        this.formato = formato;
    }

    @Override
    public void publicar(EventoDominio evento) {
        ContextoRecorrido.Datos contexto = ContextoRecorrido.actual();
        String usuario = null;
        UUID correlacion = null;
        if (contexto != null) {
            if (SISTEMA.equals(evento.usuario()) && contexto.usuario() != null) {
                usuario = contexto.usuario();
            }
            correlacion = contexto.correlacionId();
        }
        ProducerRecord<String, String> registro = new ProducerRecord<>(prefijoTopico + evento.tipo(),
                formato.clave(evento), formato.aJson(evento, usuario, correlacion));
        registro.headers().add(CABECERA_CLASE, evento.getClass().getName().getBytes(StandardCharsets.UTF_8));
        salida.accept(registro);
    }

}
