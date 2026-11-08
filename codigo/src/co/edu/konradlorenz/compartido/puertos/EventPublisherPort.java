package co.edu.konradlorenz.compartido.puertos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

/**
 * «Publish-Subscribe · publicador»
 * (Observer distribuido, vía Kafka)
 */
public interface EventPublisherPort {

    void publicar(EventoDominio evento);

}
