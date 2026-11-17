package co.edu.konradlorenz.compartido.infraestructura;

import java.util.UUID;

/**
 * Quién hace la operación y a qué recorrido pertenece (DEC-64), guardado por hilo mientras dura una
 * petición HTTP (FiltroContexto), un evento de Kafka (InterceptorCorrelacion) o una ejecución de un job.
 * KafkaEventPublisher lo lee al publicar: el núcleo no recibe ni el usuario ni la correlación.
 */
public final class ContextoRecorrido {

    /** usuario es null cuando nadie inició sesión (un listener o un job): el evento conserva el suyo. */
    public record Datos(String usuario, UUID correlacionId) {
    }

    private static final ThreadLocal<Datos> ACTUAL = new ThreadLocal<>();

    private ContextoRecorrido() {
    }

    public static void abrir(String usuario, UUID correlacionId) {
        ACTUAL.set(new Datos(usuario, correlacionId));
    }

    /** El contexto del hilo, o null si no hay ninguno. */
    public static Datos actual() {
        return ACTUAL.get();
    }

    public static void cerrar() {
        ACTUAL.remove();
    }

    /**
     * Ejecuta la tarea como un recorrido nuevo sin usuario (los jobs publican «sistema»). Al terminar
     * deja el contexto que había, por si el job se lanzó desde una petición.
     */
    public static void comoRecorridoNuevo(Runnable tarea) {
        Datos anterior = ACTUAL.get();
        abrir(null, UUID.randomUUID());
        try {
            tarea.run();
        } finally {
            if (anterior == null) {
                cerrar();
            } else {
                ACTUAL.set(anterior);
            }
        }
    }

    /** Lee un UUID de una cabecera; si falta o no es válido, null. */
    public static UUID uuid(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(texto.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
