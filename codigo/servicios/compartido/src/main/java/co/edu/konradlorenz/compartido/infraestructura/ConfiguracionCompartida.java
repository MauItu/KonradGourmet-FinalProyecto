package co.edu.konradlorenz.compartido.infraestructura;

import co.edu.konradlorenz.compartido.adaptadores.KafkaEventPublisher;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.kafka.support.converter.RecordMessageConverter;

/**
 * Lo que todo servicio recibe al depender de compartido: el formato de los eventos, el publicador de
 * Kafka como EventPublisherPort, la conversión de los registros a eventos para los @KafkaListener y el
 * contexto del recorrido (DEC-64). La seguridad está en SeguridadServicio.
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

    /** Spring Boot lo pone en la fábrica de los @KafkaListener. */
    @Bean
    @ConditionalOnMissingBean(RecordMessageConverter.class)
    public RecordMessageConverter convertidorEventos(FormatoEventos formato) {
        return new ConvertidorEventos(formato);
    }

    /** También lo pone Spring Boot en la fábrica: cada evento recibido abre su recorrido. */
    @Bean
    public RecordInterceptor<Object, Object> interceptorCorrelacion(FormatoEventos formato) {
        return new InterceptorCorrelacion(formato);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class Web {

        /** Justo después de Spring Security, que ya validó el JWT. */
        @Bean
        public FilterRegistrationBean<FiltroContexto> filtroContexto() {
            FilterRegistrationBean<FiltroContexto> r = new FilterRegistrationBean<>(new FiltroContexto());
            r.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER + 1);
            return r;
        }
    }

}
