package co.edu.konradlorenz.cotizaciones.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarCotizacionUseCase;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;

/**
 * «Listener · orden.generada»
 *
 * En el proyecto real: @Component, y alRecibir() lleva @KafkaListener con sus tópicos.
 *
 * Nota del diagrama: CON_ORDEN: la cotización ya tiene orden de compra y sale de V20; no admite otra
 * orden (HU-14).
 */
public class OrdenGeneradaListener implements EventListener<EventoDominio> {

    private final GestionarCotizacionUseCase casoDeUso;

    public OrdenGeneradaListener(GestionarCotizacionUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    public void alRecibir(EventoDominio evento) {
        if (evento instanceof OrdenGeneradaEvent orden) {
            casoDeUso.marcarConOrden(orden.cotizacionNumero());
        }
    }

}
