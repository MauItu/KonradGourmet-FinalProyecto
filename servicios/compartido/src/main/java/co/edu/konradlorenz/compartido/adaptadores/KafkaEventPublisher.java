package co.edu.konradlorenz.compartido.adaptadores;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * «Publish-Subscribe · publicador» [Kafka]
 *
 * Publica cada evento en el tópico que dice su tipo() (más el prefijo, vacío en local), en JSON y con
 * una clave: Kafka solo garantiza el orden dentro de una partición, y la clave hace que los eventos de
 * la misma entidad caigan en la misma partición (FormatoEventos.clave()).
 *
 * El envío es asíncrono: el servicio no espera al broker y un fallo queda en el log. La tabla outbox
 * del diagrama (nota n_outbox de la 4a) es la versión que garantiza la entrega.
 */
public class KafkaEventPublisher implements EventPublisherPort {

    private final String prefijoTopico;
    private final KafkaTemplate<String, String> kafka;
    private final FormatoEventos formato;

    public KafkaEventPublisher(String prefijoTopico, KafkaTemplate<String, String> kafka, FormatoEventos formato) {
        this.prefijoTopico = prefijoTopico;
        this.kafka = kafka;
        this.formato = formato;
    }

    @Override
    public void publicar(EventoDominio evento) {
        kafka.send(prefijoTopico + evento.tipo(), formato.clave(evento), formato.aJson(evento));
    }

}
