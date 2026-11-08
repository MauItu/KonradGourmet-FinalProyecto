package co.edu.konradlorenz.notificaciones.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.notificaciones.puertos.entrada.NotificarUseCase;

/**
 * «Listener · 6 tópicos»
 *
 * Nota del diagrama:
 * Consume 6 tópicos y elige el canal:
 * • inventario.stock-bajo → alarma en la app (V05) y correo a los jefes de la sucursal, con producto,
 * cantidad restante y unidad
 * • solicitud.registrada → correo de confirmación a quien creó la solicitud
 * • solicitud.en-tramite → correo a quien creó la solicitud: pasó a EN TRÁMITE
 * • cotizacion.enviada → correo certificado al proveedor con el PDF (pdfUrl)
 * • orden.generada → correo certificado al proveedor con el PDF de la orden
 * • tendencia.detectada → correo de promociones a los jefes (máximo uno al día)
 * Destinatarios: los eventos de solicitud traen correoSolicitante; para el stock bajo y las
 * promociones usa su libreta de jefes por sucursal, que carga el administrador.
 */
public class NotificacionListeners implements EventListener<EventoDominio> {

    private final NotificarUseCase casoDeUso;

    public NotificacionListeners(NotificarUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    public void alRecibir(EventoDominio evento) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
