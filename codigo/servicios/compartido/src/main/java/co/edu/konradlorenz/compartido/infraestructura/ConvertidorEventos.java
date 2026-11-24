package co.edu.konradlorenz.compartido.infraestructura;

import co.edu.konradlorenz.compartido.adaptadores.KafkaEventPublisher;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.support.converter.MessagingMessageConverter;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * Convierte el JSON de cada registro en el evento de dominio que lo originó, para que los listeners
 * reciban alRecibir(EventoDominio) con @KafkaListener, como dice su Javadoc. La clase viaja en la
 * cabecera que pone KafkaEventPublisher y solo se aceptan subclases de EventoDominio del proyecto.
 * La cola de errores de ms-notificaciones conserva esa cabecera, así que su reproceso (DEC-81) también
 * reconstruye el evento original con evento().
 */
public class ConvertidorEventos extends MessagingMessageConverter {

    private final FormatoEventos formato;

    public ConvertidorEventos(FormatoEventos formato) {
        this.formato = formato;
    }

    @Override
    protected Object extractAndConvertValue(ConsumerRecord<?, ?> registro, Type tipo) {
        if (!(registro.value() instanceof String)) {
            return super.extractAndConvertValue(registro, tipo);
        }
        return evento(registro);
    }

    /** El evento de dominio de un registro en JSON, con la clase que dice su cabecera. */
    public EventoDominio evento(ConsumerRecord<?, ?> registro) {
        if (!(registro.value() instanceof String json)) {
            throw new IllegalArgumentException("El registro de " + registro.topic() + " no es JSON");
        }
        Header cabecera = registro.headers().lastHeader(KafkaEventPublisher.CABECERA_CLASE);
        if (cabecera == null) {
            throw new IllegalArgumentException("El registro de " + registro.topic() + " no dice qué evento es");
        }
        return formato.deJson(json, claseEvento(new String(cabecera.value(), StandardCharsets.UTF_8)));
    }

    static Class<? extends EventoDominio> claseEvento(String nombre) {
        if (!nombre.startsWith("co.edu.konradlorenz.")) {
            throw new IllegalArgumentException("Evento desconocido: " + nombre);
        }
        try {
            Class<?> c = Class.forName(nombre);
            if (!EventoDominio.class.isAssignableFrom(c)) {
                throw new IllegalArgumentException("No es un evento de dominio: " + nombre);
            }
            return c.asSubclass(EventoDominio.class);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Este servicio no conoce el evento " + nombre, e);
        }
    }

}
