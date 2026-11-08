package co.edu.konradlorenz.solicitudes.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.solicitudes.puertos.entrada.GestionarSolicitudUseCase;

/**
 * «Listener · cotizacion.enviada · orden.generada»
 */
public class CotizacionEventsListener implements EventListener<EventoDominio> {

    private final GestionarSolicitudUseCase casoDeUso;

    public CotizacionEventsListener(GestionarSolicitudUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    public void alRecibir(EventoDominio evento) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
