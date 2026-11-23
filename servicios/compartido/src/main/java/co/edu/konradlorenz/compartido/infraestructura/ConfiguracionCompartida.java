package co.edu.konradlorenz.compartido.infraestructura;

import co.edu.konradlorenz.compartido.adaptadores.KafkaEventPublisher;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Lo que todo servicio recibe al depender de compartido: el formato de los eventos, el publicador de
 * Kafka como EventPublisherPort, la conversión de los registros a eventos para los @KafkaListener y el
 * contexto del recorrido (DEC-64). Si el servicio tiene base, también la outbox, su relé y los eventos
 * ya procesados (DEC-104). La seguridad está en SeguridadServicio.
 */
@AutoConfiguration(afterName = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration",
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"})
public class ConfiguracionCompartida {

    @Bean
    @ConditionalOnMissingBean
    public FormatoEventos formatoEventos() {
        return new FormatoEventos();
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisherPort.class)
    public EventPublisherPort eventPublisher(@Value("${konrad.kafka.prefijo-topico:}") String prefijo,
                                             KafkaTemplate<String, String> kafka, FormatoEventos formato,
                                             ObjectProvider<OutboxEventos> outbox) {
        OutboxEventos tabla = outbox.getIfAvailable();
        return tabla == null ? new KafkaEventPublisher(prefijo, kafka, formato) : new KafkaEventPublisher(prefijo, tabla, formato);
    }

    /** Con base: la outbox, su relé y los eventos ya procesados de los consumidores (DEC-104). */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(JdbcTemplate.class)
    @ConditionalOnBean({JdbcTemplate.class, PlatformTransactionManager.class})
    static class Outbox {

        @Bean
        public ReleOutbox releOutbox(JdbcTemplate jdbc, PlatformTransactionManager transacciones, KafkaTemplate<String, String> kafka) {
            return new ReleOutbox(jdbc, transacciones, kafka, KafkaEventPublisher.CABECERA_CLASE);
        }

        @Bean
        public OutboxEventos outboxEventos(JdbcTemplate jdbc, ReleOutbox rele) {
            return new OutboxEventos(jdbc, rele);
        }

        @Bean
        @ConditionalOnMissingBean
        public EventosProcesados eventosProcesados(JdbcTemplate jdbc) {
            return new EventosProcesados(jdbc);
        }
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
