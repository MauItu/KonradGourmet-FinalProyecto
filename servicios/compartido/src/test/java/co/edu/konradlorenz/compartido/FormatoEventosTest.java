package co.edu.konradlorenz.compartido;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** El evento que sale por Kafka llega idéntico: sin esto, la idempotencia y las fechas de la auditoría fallarían. */
class FormatoEventosTest {

    private final FormatoEventos formato = new FormatoEventos();

    @Test
    void elEventoLlegaIgualAComoSalio() throws Exception {
        AuditoriaEvent original = new AuditoriaEvent("laura.gomez@konradgourmet.com", "Generar orden OC-0012");
        String json = formato.aJson(original);
        Thread.sleep(5);
        AuditoriaEvent leido = formato.deJson(json, AuditoriaEvent.class);

        assertEquals(original.id(), leido.id());
        assertEquals(original.correlacionId(), leido.correlacionId());
        assertEquals(original.ocurridoEn(), leido.ocurridoEn());
        assertEquals(original.usuario(), leido.usuario());
        assertEquals(original.accion(), leido.accion());
        assertEquals("auditoria.evento", leido.tipo());
    }

    @Test
    void sinCampoDeEntidadLaClaveEsLaCorrelacion() {
        AuditoriaEvent e = new AuditoriaEvent("sistema", "x");
        assertEquals(e.correlacionId().toString(), formato.clave(e));
    }

}
