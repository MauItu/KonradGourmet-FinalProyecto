package co.edu.konradlorenz.notificaciones.aplicacion;

import co.edu.konradlorenz.analitica.eventos.TendenciaDetectadaEvent;
import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionEnviadaEvent;
import co.edu.konradlorenz.inventario.eventos.StockBajoEvent;
import co.edu.konradlorenz.notificaciones.dominio.CreadorNotificacion;
import co.edu.konradlorenz.notificaciones.puertos.entrada.NotificarUseCase;
import co.edu.konradlorenz.notificaciones.puertos.salida.DestinatarioRepositoryPort;
import co.edu.konradlorenz.notificaciones.puertos.salida.EnvioRepositoryPort;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudEnTramiteEvent;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudRegistradaEvent;

import java.time.LocalDateTime;
import java.util.List;

public class NotificacionService implements NotificarUseCase {

    private final CreadorNotificacion creadorCorreo;
    private final CreadorNotificacion creadorAlarma;
    private final EnvioRepositoryPort envios;
    private final DestinatarioRepositoryPort destinatarios;
    private final EventPublisherPort eventos;

    public NotificacionService(CreadorNotificacion creadorCorreo, CreadorNotificacion creadorAlarma, EnvioRepositoryPort envios, DestinatarioRepositoryPort destinatarios, EventPublisherPort eventos) {
        this.creadorCorreo = creadorCorreo;
        this.creadorAlarma = creadorAlarma;
        this.envios = envios;
        this.destinatarios = destinatarios;
        this.eventos = eventos;
    }

    @Override
    public void notificarEvento(EventoDominio evento) {
        if (evento instanceof StockBajoEvent e) {
            String porcentaje = e.porcentaje().stripTrailingZeros().toPlainString();
            String stock = e.stockActual().stripTrailingZeros().toPlainString() + " " + e.unidad();
            // alarma en la app (V05) y correo a los jefes de la sucursal
            for (String jefe : destinatarios.jefesDe(e.sucursalId())) {
                enviar(creadorAlarma, jefe, evento, e.nombreProducto() + " al " + porcentaje + " %: realice una solicitud");
                enviar(creadorCorreo, jefe, evento, "Stock bajo: " + e.nombreProducto() + "\nQuedan " + stock + " de "
                        + e.nombreProducto() + " (" + porcentaje + " % del stock de referencia). Realice una solicitud de alimentos.");
            }
        } else if (evento instanceof SolicitudRegistradaEvent e) {
            enviar(creadorCorreo, e.correoSolicitante(), evento, "Solicitud " + e.numero() + " registrada\nSu solicitud "
                    + e.numero() + " quedó PENDIENTE.");
        } else if (evento instanceof SolicitudEnTramiteEvent e) {
            enviar(creadorCorreo, e.correoSolicitante(), evento, "Solicitud " + e.numero() + " en trámite\nSu solicitud "
                    + e.numero() + " ya tiene cotización y pasó a EN TRÁMITE.");
        } else if (evento instanceof CotizacionEnviadaEvent e) {
            // el correo certificado al proveedor lleva el enlace del PDF (claim check)
            enviar(creadorCorreo, e.correoProveedor(), evento, "Solicitud de cotización " + e.numero()
                    + "\nPor favor cotice los productos del PDF: " + e.pdfUrl());
        } else if (evento instanceof OrdenGeneradaEvent e) {
            enviar(creadorCorreo, e.correoProveedor(), evento, "Orden de compra " + e.numero() + "\nOrden " + e.numero()
                    + " sobre la cotización " + e.cotizacionNumero() + ", total " + e.total().toPlainString()
                    + ". PDF: " + e.pdfUrl());
        } else if (evento instanceof TendenciaDetectadaEvent) {
            // el evento trae SugerenciaPromocion, del dominio de ms-analitica, y DEC-29 no deja importarlo (T-06)
            throw new UnsupportedOperationException("Pendiente de implementar: tendencia.detectada (T-06)");
        } else {
            throw new IllegalArgumentException("ms-notificaciones no atiende el evento " + evento.tipo());
        }
    }

    @Override
    public void registrarDestinatario(Long sucursalId, String correo) {
        if (correo == null || !correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("El correo del destinatario no es válido");
        }
        destinatarios.guardar(sucursalId, correo);
    }

    @Override
    public List<String> destinatarios(Long sucursalId) {
        return destinatarios.jefesDe(sucursalId);
    }

    /** Envía por el canal del creador y deja la traza; si falla, registra el fallo y relanza para que el evento vaya a la DLQ. */
    private void enviar(CreadorNotificacion creador, String destino, EventoDominio evento, String contenido) {
        try {
            creador.notificar(destino, contenido, null);
            envios.registrar(destino, evento.tipo(), LocalDateTime.now());
            eventos.publicar(new AuditoriaEvent("sistema", "Notificación " + evento.tipo() + " enviada a " + destino));
        } catch (RuntimeException e) {
            envios.registrarFallo(destino, evento.tipo(), e.getMessage());
            throw e;
        }
    }

}
