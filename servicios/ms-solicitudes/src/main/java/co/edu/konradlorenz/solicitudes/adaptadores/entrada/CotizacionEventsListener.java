package co.edu.konradlorenz.solicitudes.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionEnviadaEvent;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import co.edu.konradlorenz.solicitudes.puertos.entrada.GestionarSolicitudUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * «Listener · cotizacion.enviada · orden.generada»
 *
 * cotizacion.enviada trae las solicitudes de la cotización y el precio sugerido de cada producto: cada
 * solicitud marca sus productos cotizados y pasa a EN TRÁMITE cuando ya no le falta ninguno (publica
 * solicitud.en-tramite en el mismo recorrido de la cotización). Es idempotente por número de cotización:
 * un reenvío no suma dos veces. orden.generada (corte 5) completa cada solicitud.
 */
@Component
public class CotizacionEventsListener implements EventListener<EventoDominio> {

    private final GestionarSolicitudUseCase casoDeUso;

    public CotizacionEventsListener(GestionarSolicitudUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    @KafkaListener(topics = {"${konrad.kafka.prefijo-topico:}cotizacion.enviada", "${konrad.kafka.prefijo-topico:}orden.generada"})
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
