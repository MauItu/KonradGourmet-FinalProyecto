package co.edu.konradlorenz.notificaciones.configuracion;

import co.edu.konradlorenz.compartido.adaptadores.KafkaEventPublisher;
import co.edu.konradlorenz.compartido.infraestructura.ConvertidorEventos;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.CorreoCertificadoDecorator;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.CorreoSmtpAdapter;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.EstampaCronologicaDecorator;
import co.edu.konradlorenz.notificaciones.aplicacion.NotificacionService;
import co.edu.konradlorenz.notificaciones.dominio.CreadorAlarmaApp;
import co.edu.konradlorenz.notificaciones.dominio.CreadorCorreo;
import co.edu.konradlorenz.notificaciones.puertos.salida.AlarmaPushPort;
import co.edu.konradlorenz.notificaciones.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.notificaciones.puertos.salida.DestinatarioRepositoryPort;
import co.edu.konradlorenz.notificaciones.puertos.salida.EnvioRepositoryPort;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DEC-81: el reproceso de notificaciones.dlq con el núcleo, los decoradores y el adaptador SMTP reales (el
 * servidor de correo, la traza y el broker son simulados). Un orden.generada de OC-0012 cuyo correo falló
 * llega a la cola; el reproceso espera sus 5 minutos sin procesarlo, después lo entrega; y si el correo
 * sigue caído, lo devuelve a la cola con la ronda en la cabecera y se agota en la tercera.
 */
class ReprocesoColaErroresTest {

    private static final String COLA = "notificaciones.dlq";
    private static final Duration ESPERA = Duration.ofMinutes(5);
    private static final Instant LLEGO = Instant.parse("2026-11-17T22:00:00Z");
    private static final UUID RECORRIDO = UUID.fromString("7d3c1a52-6f0e-4d9b-9a51-2b8f0c6e4a11");

    /** Un reloj que la prueba adelanta. */
    private static final class Reloj extends Clock {
        private Instant ahora = LLEGO;

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    private final JavaMailSender smtp = mock(JavaMailSender.class);
    private final AlmacenArchivosPort almacen = mock(AlmacenArchivosPort.class);
    private final EnvioRepositoryPort envios = mock(EnvioRepositoryPort.class);
    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);
    private final FormatoEventos formato = new FormatoEventos();
    private final Reloj reloj = new Reloj();
    private ReprocesoColaErrores reproceso;

    @BeforeEach
    void armar() {
        when(smtp.createMimeMessage()).thenAnswer(i -> new MimeMessage(Session.getInstance(new java.util.Properties())));
        when(almacen.obtener("s3://documentos-pdf/ordenes/OC-0012.pdf")).thenReturn("%PDF-1.4 orden OC-0012".getBytes(StandardCharsets.ISO_8859_1));
        when(kafka.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));
        CorreoSmtpAdapter adaptador = new CorreoSmtpAdapter(almacen, smtp, "Konrad Gourmet <notificaciones@konradgourmet.com>");
        NotificacionService servicio = new NotificacionService(
                new CreadorCorreo(new CorreoCertificadoDecorator(new EstampaCronologicaDecorator(adaptador))),
                new CreadorAlarmaApp(mock(AlarmaPushPort.class)), envios, mock(DestinatarioRepositoryPort.class), mock(EventPublisherPort.class));
        reproceso = new ReprocesoColaErrores(servicio, new ConvertidorEventos(formato), kafka, COLA, ESPERA, 3, reloj);
    }

    /** El registro como lo deja DeadLetterPublishingRecoverer: el JSON y las cabeceras del original, más las suyas. */
    private ConsumerRecord<String, String> enLaCola(Instant cuando, Headers cabeceras, String json) {
        return new ConsumerRecord<>(COLA, 0, 0L, cuando.toEpochMilli(), TimestampType.CREATE_TIME, -1, -1, "OC-0012", json, cabeceras, Optional.empty());
    }

    private ConsumerRecord<String, String> ordenQueFallo() {
        OrdenGeneradaEvent orden = new OrdenGeneradaEvent("laura.gomez@konradgourmet.com", "OC-0012", "COT-018", List.of("SOL-031"),
                "Proveedor ABC", "ventas@proveedorabc.com", "s3://documentos-pdf/ordenes/OC-0012.pdf", new BigDecimal("545000"));
        RecordHeaders cabeceras = new RecordHeaders();
        cabeceras.add(KafkaEventPublisher.CABECERA_CLASE, OrdenGeneradaEvent.class.getName().getBytes(StandardCharsets.UTF_8));
        cabeceras.add(KafkaHeaders.DLT_ORIGINAL_TOPIC, "orden.generada".getBytes(StandardCharsets.UTF_8));
        cabeceras.add(KafkaHeaders.DLT_EXCEPTION_MESSAGE, "Mail server connection failed".getBytes(StandardCharsets.UTF_8));
        return enLaCola(LLEGO, cabeceras, formato.aJson(orden, null, RECORRIDO));
    }

    @Test
    void esperaCincoMinutosSinBloquearYDespuesEntregaElCorreo() {
        ConsumerRecord<String, String> registro = ordenQueFallo();
        Acknowledgment acuse = mock(Acknowledgment.class);

        reloj.ahora = LLEGO.plusSeconds(10);
        reproceso.alRecibir(registro, acuse);
        verify(acuse).nack(Duration.ofSeconds(290));
        verify(acuse, never()).acknowledge();
        verify(smtp, never()).send(any(MimeMessage.class));

        reloj.ahora = LLEGO.plus(ESPERA);
        reproceso.alRecibir(registro, acuse);
        verify(smtp, times(1)).send(any(MimeMessage.class));
        verify(envios).registrar(eq("ventas@proveedorabc.com"), eq("orden.generada"), any());
        verify(acuse).acknowledge();
        verify(kafka, never()).send(any(ProducerRecord.class));
    }

    @Test
    void siElCorreoSigueCaidoVuelveALaColaYSeAgotaEnLaTerceraRonda() {
        doThrow(new MailSendException("Connection refused: correo:1025")).when(smtp).send(any(MimeMessage.class));
        ConsumerRecord<String, String> registro = ordenQueFallo();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<String, String>> devuelto = ArgumentCaptor.forClass(ProducerRecord.class);

        for (int ronda = 1; ronda <= 2; ronda++) {
            reloj.ahora = Instant.ofEpochMilli(registro.timestamp()).plus(ESPERA);
            Acknowledgment acuse = mock(Acknowledgment.class);
            reproceso.alRecibir(registro, acuse);
            verify(acuse).acknowledge();
            verify(kafka, times(ronda)).send(devuelto.capture());
            ProducerRecord<String, String> copia = devuelto.getValue();
            assertEquals(COLA, copia.topic(), "vuelve a la cola de errores");
            assertEquals(registro.value(), copia.value(), "con el mismo evento");
            assertEquals(String.valueOf(ronda), texto(copia.headers(), ReprocesoColaErrores.CABECERA_RONDA), "con la ronda fallida");
            assertEquals(1, cuantas(copia.headers(), ReprocesoColaErrores.CABECERA_RONDA), "una sola cabecera de ronda");
            assertEquals(OrdenGeneradaEvent.class.getName(), texto(copia.headers(), KafkaEventPublisher.CABECERA_CLASE), "y su clase");
            assertEquals("orden.generada", texto(copia.headers(), KafkaHeaders.DLT_ORIGINAL_TOPIC), "y su tópico de origen");
            assertEquals(RECORRIDO, formato.correlacionDe(copia.value()), "con la correlación original");
            registro = enLaCola(reloj.ahora.plusMillis(5), copia.headers(), copia.value());
        }

        reloj.ahora = Instant.ofEpochMilli(registro.timestamp()).plus(ESPERA);
        Acknowledgment ultimo = mock(Acknowledgment.class);
        reproceso.alRecibir(registro, ultimo);
        verify(ultimo).acknowledge();
        verify(kafka, times(2)).send(any(ProducerRecord.class));
        verify(envios, times(3)).registrarFallo("ventas@proveedorabc.com", "orden.generada", "Connection refused: correo:1025");
        verify(envios, never()).registrar(anyString(), anyString(), any());
    }

    private static String texto(Headers cabeceras, String nombre) {
        return new String(cabeceras.lastHeader(nombre).value(), StandardCharsets.UTF_8);
    }

    private static int cuantas(Headers cabeceras, String nombre) {
        int n = 0;
        for (var h : cabeceras.headers(nombre)) {
            n++;
        }
        return n;
    }

}
