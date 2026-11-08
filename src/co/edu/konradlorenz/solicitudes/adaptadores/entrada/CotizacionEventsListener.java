package co.edu.konradlorenz.solicitudes.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionEnviadaEvent;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import co.edu.konradlorenz.solicitudes.puertos.entrada.GestionarSolicitudUseCase;

/**
 * «Listener · cotizacion.enviada · orden.generada»
 *
 * En el proyecto real: @Component, y alRecibir() lleva @KafkaListener con sus tópicos.
 */
public class CotizacionEventsListener implements EventListener<EventoDominio> {

    private final GestionarSolicitudUseCase casoDeUso;

    public CotizacionEventsListener(GestionarSolicitudUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    public void alRecibir(EventoDominio evento) {
        if (evento instanceof CotizacionEnviadaEvent enviada) {
            for (String solicitud : enviada.solicitudNumeros()) {
                casoDeUso.registrarCotizacion(solicitud, enviada.numero(), enviada.precios());
            }
        } else if (evento instanceof OrdenGeneradaEvent orden) {
            for (String solicitud : orden.solicitudNumeros()) {
                casoDeUso.completar(solicitud);
            }
        }
    }

}
