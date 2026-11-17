package co.edu.konradlorenz.compartido.infraestructura;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

/**
 * El formato de los eventos en Kafka: JSON con los campos de la clase (los eventos no tienen getters
 * ni constructor vacío). Al leer, Jackson usa el constructor del evento y después escribe los campos
 * que el constructor no recibe, así que el id, el correlacionId y ocurridoEn llegan iguales a como
 * salieron (lo prueba FormatoEventosTest).
 */
public class FormatoEventos {

    /** Campos que identifican la entidad del evento, en orden de preferencia, para la clave de Kafka. */
    private static final List<String> CAMPOS_CLAVE = List.of("numero", "pedidoNumero", "solicitudNumero",
            "cotizacionNumero", "ordenNumero", "productoId", "platoId", "sucursalId");

    private final ObjectMapper json;

    public FormatoEventos() {
        this.json = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public String aJson(EventoDominio evento) {
        try {
            return json.writeValueAsString(evento);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo convertir a JSON el evento " + evento.tipo(), e);
        }
    }

    /**
     * El JSON del evento con el usuario y la correlación del recorrido (DEC-64), sin tocar el objeto: el
     * núcleo crea los eventos con «sistema» y una correlación propia porque no conoce la petición.
     */
    public String aJson(EventoDominio evento, String usuario, UUID correlacionId) {
        ObjectNode nodo = json.valueToTree(evento);
        if (usuario != null) {
            nodo.put("usuario", usuario);
        }
        if (correlacionId != null) {
            nodo.put("correlacionId", correlacionId.toString());
        }
        try {
            return json.writeValueAsString(nodo);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo convertir a JSON el evento " + evento.tipo(), e);
        }
    }

    /** El correlacionId de un evento en JSON, o null si no lo trae. */
    public UUID correlacionDe(String texto) {
        try {
            return ContextoRecorrido.uuid(json.readTree(texto).path("correlacionId").asText(null));
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public <T extends EventoDominio> T deJson(String texto, Class<T> tipo) {
        try {
            return json.readValue(texto, tipo);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Evento mal formado para " + tipo.getSimpleName() + ": " + e.getOriginalMessage(), e);
        }
    }

    /** La entidad del evento si tiene uno de los campos conocidos; si no, su correlacionId. */
    public String clave(EventoDominio evento) {
        for (String nombre : CAMPOS_CLAVE) {
            for (Class<?> c = evento.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                try {
                    Field f = c.getDeclaredField(nombre);
                    f.setAccessible(true);
                    Object valor = f.get(evento);
                    if (valor != null) {
                        return valor.toString();
                    }
                } catch (NoSuchFieldException e) {
                    // sigue con la superclase
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return String.valueOf(evento.correlacionId());
    }

}
