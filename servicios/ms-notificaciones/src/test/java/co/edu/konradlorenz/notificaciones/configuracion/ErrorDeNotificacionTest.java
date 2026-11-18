package co.edu.konradlorenz.notificaciones.configuracion;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.mail.MailSendException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Requisito de mantenimiento 2 (DEC-84): el evento cuyo correo no salió después de los reintentos deja un ERROR en el
 * log, con la correlación de su recorrido (así llega a Elasticsearch y a la ventana de logs), y después pasa a la cola
 * de errores como antes (DEC-81).
 */
class ErrorDeNotificacionTest {

    private final Logger log = (Logger) LoggerFactory.getLogger(ConfiguracionNotificaciones.class);
    private final List<String> registrados = new ArrayList<>();
    private final AppenderBase<ILoggingEvent> testigo = new AppenderBase<>() {
        @Override
        protected void append(ILoggingEvent e) {
            ContextoRecorrido.Datos d = ContextoRecorrido.actual();
            registrados.add(e.getLevel() + "|" + e.getFormattedMessage() + "|" + (d == null ? "-" : d.correlacionId())
                    + "|" + (e.getThrowableProxy() == null ? "-" : e.getThrowableProxy().getClassName()));
        }
    };

    @AfterEach
    void quitar() {
        log.detachAppender(testigo);
        ContextoRecorrido.cerrar();
    }

    @Test
    void elEventoQueNoSeNotificoQuedaEnElLogConSuRecorrido() {
        testigo.setContext(log.getLoggerContext());
        testigo.start();
        log.addAppender(testigo);
        FormatoEventos formato = new FormatoEventos();
        UUID recorrido = UUID.randomUUID();
        String json = formato.aJson(new OrdenGeneradaEvent("sistema", "OC-0012", "COT-018", List.of("SOL-031"), "Proveedor ABC", "ventas@proveedorabc.com",
                "s3://documentos-pdf/ordenes/OC-0012.pdf", new BigDecimal("545000")), null, recorrido);
        ConsumerRecord<?, ?> registro = new ConsumerRecord<>("orden.generada", 0, 7L, "OC-0012", json);
        ConsumerRecordRecoverer cola = mock(ConsumerRecordRecoverer.class);
        MailSendException error = new MailSendException("Couldn't connect to host correo, port 1025");

        ConfiguracionNotificaciones.registrarYEncolar(cola, formato, "notificaciones.dlq", 3).accept(registro, error);

        assertEquals(1, registrados.size(), registrados.toString());
        String[] r = registrados.get(0).split("\\|");
        assertEquals(Level.ERROR.toString(), r[0]);
        assertEquals("No se pudo notificar un evento de orden.generada después de 3 intentos; pasa a la cola de errores notificaciones.dlq", r[1]);
        assertEquals(recorrido.toString(), r[2], "con la correlación del recorrido del evento");
        assertTrue(r[3].endsWith("MailSendException"), "con la excepción");
        verify(cola).accept(registro, error);
        assertNull(ContextoRecorrido.actual(), "y deja el hilo como estaba");
    }

}
