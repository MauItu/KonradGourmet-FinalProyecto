package co.edu.konradlorenz.compartido.infraestructura;

import co.edu.konradlorenz.compartido.adaptadores.KafkaEventPublisher;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Lo que todo servicio recibe al depender de compartido: el formato de los eventos y el publicador
 * de Kafka como EventPublisherPort. La seguridad está en SeguridadServicio.
 */
@AutoConfiguration
public class ConfiguracionCompartida {

    @Bean
    @ConditionalOnMissingBean
    public FormatoEventos formatoEventos() {
        return new FormatoEventos();
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisherPort.class)
    public EventPublisherPort eventPublisher(@Value("${konrad.kafka.prefijo-topico:}") String prefijo,
                                             KafkaTemplate<String, String> kafka, FormatoEventos formato) {
        return new KafkaEventPublisher(prefijo, kafka, formato);
    }

}
