package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Publish-Subscribe sin Kafka: guarda cada evento publicado y se lo entrega, en el mismo hilo, a los
 * suscriptores de su tópico. La suscripción «*» recibe todos los eventos (sirve para trazar).
 */
public class BusEnMemoria implements EventPublisherPort {

    private final Map<String, List<EventListener<EventoDominio>>> suscriptores = new HashMap<>();
    private final List<EventoDominio> publicados = new ArrayList<>();

    public void suscribir(String topico, EventListener<EventoDominio> listener) {
        suscriptores.computeIfAbsent(topico, t -> new ArrayList<>()).add(listener);
    }

    @Override
    public void publicar(EventoDominio evento) {
        publicados.add(evento);
        for (EventListener<EventoDominio> l : List.copyOf(suscriptores.getOrDefault("*", List.of()))) {
            l.alRecibir(evento);
        }
        for (EventListener<EventoDominio> l : List.copyOf(suscriptores.getOrDefault(evento.tipo(), List.of()))) {
            l.alRecibir(evento);
        }
    }

    /** Todos los eventos publicados hasta ahora, en orden. */
    public List<EventoDominio> publicados() {
        return List.copyOf(publicados);
    }

    /** Cuántos eventos de ese tópico se publicaron. */
    public long cuantos(String topico) {
        return publicados.stream().filter(e -> e.tipo().equals(topico)).count();
    }

}
