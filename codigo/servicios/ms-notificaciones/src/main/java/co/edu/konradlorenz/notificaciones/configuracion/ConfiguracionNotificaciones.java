package co.edu.konradlorenz.notificaciones.configuracion;

import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.CorreoCertificadoDecorator;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.CorreoSmtpAdapter;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.EstampaCronologicaDecorator;
import co.edu.konradlorenz.notificaciones.aplicacion.NotificacionService;
import co.edu.konradlorenz.notificaciones.dominio.CreadorAlarmaApp;
import co.edu.konradlorenz.notificaciones.dominio.CreadorCorreo;
import co.edu.konradlorenz.notificaciones.puertos.entrada.NotificarUseCase;
import co.edu.konradlorenz.notificaciones.puertos.salida.AlarmaPushPort;
import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;
import co.edu.konradlorenz.notificaciones.puertos.salida.DestinatarioRepositoryPort;
import co.edu.konradlorenz.notificaciones.puertos.salida.EnvioRepositoryPort;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.util.backoff.FixedBackOff;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.util.UUID;

/**
 * Arma el servicio de aplicación con sus constructores (el núcleo de src/ no lleva anotaciones): las dos
 * fábricas del Factory Method y, en el mismo orden que el Ensamble de la demo, los decoradores del correo:
 * certificado (huella SHA-256) → estampa cronológica → SMTP. Además, el cliente S3, la cola de errores de
 * Kafka y la fábrica de su reproceso (DEC-81), el canal de Redis de las alarmas (PD-01) y el token del WebSocket.
 */
@Configuration
public class ConfiguracionNotificaciones {

    /** Donde el navegador del jefe de cocina abre el WebSocket de las alarmas de V05. */
    public static final String RUTA_ALARMAS = "/api/notificaciones/alarmas";

    private static final Logger LOG = LoggerFactory.getLogger(ConfiguracionNotificaciones.class);

    @Bean
    public NotificarUseCase notificar(CorreoSmtpAdapter smtp, AlarmaPushPort alarmas, EnvioRepositoryPort envios,
                                      DestinatarioRepositoryPort destinatarios, EventPublisherPort eventos) {
        Correo correo = new CorreoCertificadoDecorator(new EstampaCronologicaDecorator(smtp));
        return new NotificacionService(new CreadorCorreo(correo), new CreadorAlarmaApp(alarmas), envios, destinatarios, eventos);
    }

    /**
     * Nota n_fallos de la 4d: si el correo falla, registrarFallo() deja la traza (lo hace el núcleo) y el
     * evento se reintenta; si sigue fallando, pasa a la cola de errores (DLQ) con las cabeceras del error,
     * que ReprocesoColaErrores vuelve a procesar (DEC-81). El auxiliar también puede reenviar la cotización (HU-12).
     */
    @Bean
    public CommonErrorHandler colaDeErrores(KafkaTemplate<String, String> kafka, FormatoEventos formato,
                                            @Value("${konrad.notificaciones.topico-errores}") String topico,
                                            @Value("${konrad.notificaciones.reintentos}") long reintentos,
                                            @Value("${konrad.notificaciones.espera-reintento-ms}") long espera) {
        DeadLetterPublishingRecoverer dlq = new DeadLetterPublishingRecoverer(kafka, (registro, error) -> new TopicPartition(topico, -1));
        return new DefaultErrorHandler(registrarYEncolar(dlq, formato, topico, reintentos + 1), new FixedBackOff(espera, reintentos));
    }

    /**
     * Requisito de mantenimiento 2 (DEC-84): el evento que no se pudo notificar deja un error en el log, que llega a
     * Elasticsearch con la correlación de su recorrido, antes de pasar a la cola de errores.
     */
    static ConsumerRecordRecoverer registrarYEncolar(ConsumerRecordRecoverer dlq, FormatoEventos formato, String topico, long intentos) {
        return (registro, error) -> {
            UUID correlacion = registro.value() instanceof String json ? formato.correlacionDe(json) : null;
            ContextoRecorrido.Datos anterior = ContextoRecorrido.actual();
            ContextoRecorrido.abrir(null, correlacion != null ? correlacion : UUID.randomUUID());
            try {
                LOG.error("No se pudo notificar un evento de {} después de {} intentos; pasa a la cola de errores {}",
                        registro.topic(), intentos, topico, error);
            } finally {
                if (anterior == null) {
                    ContextoRecorrido.cerrar();
                } else {
                    ContextoRecorrido.abrir(anterior.usuario(), anterior.correlacionId());
                }
            }
            dlq.accept(registro, error);
        };
    }

    /**
     * Los contenedores de ReprocesoColaErrores: lo mismo que la fábrica de Spring Boot (conversión de eventos y
     * recorrido de InterceptorCorrelacion), pero con acuse manual para esperar con nack(). Si devolver un
     * registro a la cola falla, se intenta dos veces más; después se registra en el log y se sigue con el siguiente.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> fabricaReproceso(ConcurrentKafkaListenerContainerFactoryConfigurer configurador,
                                                                                    ObjectProvider<ConsumerFactory<Object, Object>> consumidores) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> fabrica = new ConcurrentKafkaListenerContainerFactory<>();
        configurador.configure(fabrica, consumidores.getObject());
        fabrica.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        fabrica.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(10_000L, 2L)));
        return fabrica;
    }

    /** Cada réplica escucha el canal de alarmas y las entrega a las conexiones que tenga abiertas (PD-01). */
    @Bean
    public RedisMessageListenerContainer canalRedis(RedisConnectionFactory redis, CanalAlarmas canal,
                                                    @Value("${konrad.notificaciones.canal-alarmas}") String nombre) {
        RedisMessageListenerContainer contenedor = new RedisMessageListenerContainer();
        contenedor.setConnectionFactory(redis);
        contenedor.addMessageListener(canal, new ChannelTopic(nombre));
        return contenedor;
    }

    /**
     * El navegador no puede poner la cabecera Authorization al abrir un WebSocket: solo en la ruta de las
     * alarmas el JWT puede venir en ?access_token= (RFC 6750 §2.3). En las demás rutas, solo la cabecera.
     */
    @Bean
    public BearerTokenResolver tokenDelWebSocket() {
        DefaultBearerTokenResolver cabecera = new DefaultBearerTokenResolver();
        DefaultBearerTokenResolver parametro = new DefaultBearerTokenResolver();
        parametro.setAllowUriQueryParameter(true);
        return peticion -> (RUTA_ALARMAS.equals(peticion.getRequestURI()) ? parametro : cabecera).resolve(peticion);
    }

    /** SeaweedFS en local (DEC-59), igual que en ms-cotizaciones y ms-ordenes. */
    @Bean
    public S3Client s3(@Value("${konrad.s3.endpoint}") String endpoint, @Value("${konrad.s3.usuario}") String usuario,
                       @Value("${konrad.s3.clave}") String clave) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .serviceConfiguration(S3Configuration.builder().chunkedEncodingEnabled(false).build())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(usuario, clave)))
                .build();
    }

}
