package co.edu.konradlorenz.compartido.eventos;

/**
 * «Publish-Subscribe · suscriptor»
 * (Observer distribuido, vía Kafka)
 */
public interface EventListener<T extends EventoDominio> {

    void alRecibir(T evento);

}
