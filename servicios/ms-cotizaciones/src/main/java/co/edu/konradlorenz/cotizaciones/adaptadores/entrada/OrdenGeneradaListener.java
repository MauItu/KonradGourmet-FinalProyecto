package co.edu.konradlorenz.cotizaciones.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.infraestructura.EventosProcesados;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarCotizacionUseCase;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * «Listener · orden.generada»
 *
 * Nota del diagrama: CON_ORDEN: la cotización ya tiene orden de compra y sale de V20; no admite otra
 * orden (HU-14).
 *
 * ms-ordenes publica orden.generada desde el corte 5; el listener ya está suscrito y lo prueba
 * OrdenGeneradaListenerTest.
 */
@Component
public class OrdenGeneradaListener implements EventListener<EventoDominio> {

    private final GestionarCotizacionUseCase casoDeUso;
    private final EventosProcesados procesados;

    public OrdenGeneradaListener(GestionarCotizacionUseCase casoDeUso, EventosProcesados procesados) {
        this.casoDeUso = casoDeUso;
        this.procesados = procesados;
    }

    @Override
    @Transactional
    @KafkaListener(topics = "${konrad.kafka.prefijo-topico:}orden.generada")
    public void alRecibir(EventoDominio evento) {
        // un evento repetido se descarta; se marca en la misma transacción que el cambio (DEC-104)
        if (!procesados.marcar(evento, "OrdenGeneradaListener")) {
            return;
        }
        if (evento instanceof OrdenGeneradaEvent orden) {
            casoDeUso.marcarConOrden(orden.cotizacionNumero());
        }
    }

}
