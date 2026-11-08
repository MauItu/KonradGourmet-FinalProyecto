package co.edu.konradlorenz.compartido.adaptadores;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;

/**
 * En el proyecto real: @Component.
 */
public class KafkaEventPublisher implements EventPublisherPort {

    private final String prefijoTopico;

    public KafkaEventPublisher(String prefijoTopico) {
        this.prefijoTopico = prefijoTopico;
    }

    @Override
    public void publicar(EventoDominio evento) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
