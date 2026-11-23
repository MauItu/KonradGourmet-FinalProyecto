package co.edu.konradlorenz.notificaciones.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.infraestructura.EventosProcesados;
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
 * tendencia.detectada trae las sugerencias como líneas de texto (PD-05): el correo va a los jefes de todas las
 * sucursales de la libreta, y ms-analitica publica a lo sumo un evento por día. Si el envío falla, el
 * evento se reintenta y después pasa a la cola de errores (ConfiguracionNotificaciones), que
 * ReprocesoColaErrores vuelve a procesar (DEC-81).
 */
@Component
public class NotificacionListeners implements EventListener<EventoDominio> {

    /** El nombre con que marca los eventos ya notificados; ReprocesoColaErrores usa el mismo. */
    public static final String CONSUMIDOR = "NotificacionListeners";

    private final NotificarUseCase casoDeUso;
    private final EventosProcesados procesados;

    public NotificacionListeners(NotificarUseCase casoDeUso, EventosProcesados procesados) {
        this.casoDeUso = casoDeUso;
        this.procesados = procesados;
    }

    @Override
    @KafkaListener(topics = {"${konrad.kafka.prefijo-topico:}inventario.stock-bajo", "${konrad.kafka.prefijo-topico:}solicitud.registrada",
            "${konrad.kafka.prefijo-topico:}solicitud.en-tramite", "${konrad.kafka.prefijo-topico:}cotizacion.enviada",
            "${konrad.kafka.prefijo-topico:}orden.generada", "${konrad.kafka.prefijo-topico:}tendencia.detectada"})
    public void alRecibir(EventoDominio evento) {
        // un evento ya notificado no se vuelve a enviar (DEC-104). Se marca después de enviar, sin una
        // transacción alrededor: si el envío falla, la traza del fallo queda y el evento sigue sin marcar,
        // para los reintentos y la cola de errores
        if (procesados.procesado(evento, CONSUMIDOR)) {
            return;
        }
        casoDeUso.notificarEvento(evento);
        procesados.marcar(evento, CONSUMIDOR);
    }

}
