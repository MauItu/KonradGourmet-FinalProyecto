package co.edu.konradlorenz.compartido.infraestructura;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Engancha AppenderElasticsearch al logger raíz de cada servicio que depende de compartido (DEC-84). Corre justo
 * después de que Spring Boot configura logback, así que también llegan a Elasticsearch los errores del arranque
 * (por ejemplo, si la base no responde). Solo se activa si konrad.logs.elasticsearch tiene la dirección de
 * Elasticsearch (en el compose, la variable KONRAD_LOGS_ELASTICSEARCH); las pruebas JUnit no la tienen.
 */
public class RegistroLogsElasticsearch implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    public static final String PROPIEDAD = "konrad.logs.elasticsearch";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent evento) {
        ConfigurableEnvironment entorno = evento.getEnvironment();
        String url = entorno.getProperty(PROPIEDAD);
        if (url == null || url.isBlank() || !(LoggerFactory.getILoggerFactory() instanceof LoggerContext contexto)) {
            return;
        }
        Logger raiz = contexto.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        if (raiz.getAppender(AppenderElasticsearch.NOMBRE) != null) {
            return;
        }
        AppenderElasticsearch appender = new AppenderElasticsearch(url.trim(), entorno.getProperty("spring.application.name", "desconocido"));
        appender.setContext(contexto);
        appender.start();
        raiz.addAppender(appender);
    }

    @Override
    public int getOrder() {
        return LoggingApplicationListener.DEFAULT_ORDER + 1;
    }

}
