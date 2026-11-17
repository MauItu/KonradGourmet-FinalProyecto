package co.edu.konradlorenz.notificaciones.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.notificaciones.puertos.entrada.NotificarUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * «Listener · 6 tópicos»
 *
 * Nota del diagrama: Consume 6 tópicos y elige el canal:
 * • inventario.stock-bajo → alarma en la app (V05) y correo a los jefes de la sucursal, con producto,
 * cantidad restante y unidad
 * • solicitud.registrada → correo de confirmación a quien creó la solicitud
 * • solicitud.en-tramite → correo a quien creó la solicitud: pasó a EN TRÁMITE
 * • cotizacion.enviada → correo certificado al proveedor con el PDF (pdfUrl)
 * • orden.generada → correo certificado al proveedor con el PDF de la orden
 * • tendencia.detectada → correo de promociones a los jefes (máximo uno al día)
 * Destinatarios: los eventos de solicitud traen correoSolicitante; para el stock bajo y las promociones
 * usa su libreta de jefes por sucursal, que carga el administrador.
 *
 * Escucha cinco: tendencia.detectada llega con ms-analitica en el corte 6 (T-06). Si el envío falla, el
 * evento se reintenta y después pasa a la cola de errores (ConfiguracionNotificaciones).
 */
@Component
public class NotificacionListeners implements EventListener<EventoDominio> {

    private final NotificarUseCase casoDeUso;

    public NotificacionListeners(NotificarUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    @KafkaListener(topics = {"${konrad.kafka.prefijo-topico:}inventario.stock-bajo", "${konrad.kafka.prefijo-topico:}solicitud.registrada",
            "${konrad.kafka.prefijo-topico:}solicitud.en-tramite", "${konrad.kafka.prefijo-topico:}cotizacion.enviada",
            "${konrad.kafka.prefijo-topico:}orden.generada"})
    public void alRecibir(EventoDominio evento) {
        casoDeUso.notificarEvento(evento);
    }

}
