package co.edu.konradlorenz.compartido.infraestructura;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.RecordInterceptor;

import java.util.UUID;

/**
 * Antes de cada @KafkaListener pone como contexto el correlacionId del evento recibido (DEC-64): lo que
 * el listener publique sigue en el mismo recorrido. No hay usuario: el evento que se publique conserva
 * el que le puso el núcleo.
 */
public class InterceptorCorrelacion implements RecordInterceptor<Object, Object> {

    private final FormatoEventos formato;

    public InterceptorCorrelacion(FormatoEventos formato) {
        this.formato = formato;
    }

    @Override
    public ConsumerRecord<Object, Object> intercept(ConsumerRecord<Object, Object> registro, Consumer<Object, Object> consumidor) {
        UUID correlacion = registro.value() instanceof String json ? formato.correlacionDe(json) : null;
        ContextoRecorrido.abrir(null, correlacion != null ? correlacion : UUID.randomUUID());
        return registro;
    }

    @Override
    public void afterRecord(ConsumerRecord<Object, Object> registro, Consumer<Object, Object> consumidor) {
        ContextoRecorrido.cerrar();
    }

}
