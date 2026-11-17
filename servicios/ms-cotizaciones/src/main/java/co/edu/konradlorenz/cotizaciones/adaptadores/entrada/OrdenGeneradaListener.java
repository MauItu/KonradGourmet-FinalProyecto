package co.edu.konradlorenz.cotizaciones.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarCotizacionUseCase;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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

    public OrdenGeneradaListener(GestionarCotizacionUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    @KafkaListener(topics = "${konrad.kafka.prefijo-topico:}orden.generada")
    public void alRecibir(EventoDominio evento) {
        if (evento instanceof OrdenGeneradaEvent orden) {
            casoDeUso.marcarConOrden(orden.cotizacionNumero());
        }
    }

}
