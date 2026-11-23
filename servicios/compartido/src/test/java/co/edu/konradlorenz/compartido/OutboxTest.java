package co.edu.konradlorenz.compartido;

import co.edu.konradlorenz.compartido.adaptadores.KafkaEventPublisher;
import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.compartido.infraestructura.OutboxEventos;
import co.edu.konradlorenz.compartido.infraestructura.ReleOutbox;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * DEC-104: con base, el publicador no habla con Kafka: guarda el registro ya armado en la outbox, en la
 * transacción en curso, y el relé solo se despierta cuando esa transacción se confirma.
 */
class OutboxTest {

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final ReleOutbox rele = mock(ReleOutbox.class);
    private final FormatoEventos formato = new FormatoEventos();
    private final KafkaEventPublisher publicador = new KafkaEventPublisher("", new OutboxEventos(jdbc, rele), formato);

    @Test
    void elEventoQuedaEnLaOutboxConSuTopicoClaveYClaseYElReleEsperaAlCommit() {
        AuditoriaEvent evento = new AuditoriaEvent("paula.herrera@konradgourmet.com", "Parámetro actualizado");
        TransactionSynchronizationManager.initSynchronization();
        try {
            publicador.publicar(evento);
            ArgumentCaptor<String> valor = ArgumentCaptor.forClass(String.class);
            verify(jdbc).update(eq("INSERT INTO outbox (topico, clave, valor, clase) VALUES (?, ?, ?, ?)"), eq("auditoria.evento"),
                    eq(formato.clave(evento)), valor.capture(), eq(AuditoriaEvent.class.getName()));
            assertEquals(evento.id(), formato.deJson(valor.getValue(), AuditoriaEvent.class).id());
            verify(rele, never()).despertar();
            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
            verify(rele).despertar();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void sinTransaccionElReleSeDespiertaEnseguida() {
        publicador.publicar(new AuditoriaEvent("sistema", "Precio sugerido consultado: producto 2"));
        verify(rele).despertar();
    }

}
