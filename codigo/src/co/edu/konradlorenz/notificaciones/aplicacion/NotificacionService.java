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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

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
            // con coma decimal, como lo muestra la aplicación (CD-06: «Pollo al 24,5 %»; DEC-98)
            String porcentaje = e.porcentaje().stripTrailingZeros().toPlainString().replace('.', ',');
            String stock = e.stockActual().stripTrailingZeros().toPlainString().replace('.', ',') + " " + e.unidad();
            // alarma en la app (V05) y correo a los jefes de la sucursal, que la nombra (HU-16, 4; DEC-102)
            String sucursal = destinatarios.nombreSucursal(e.sucursalId());
            for (String jefe : destinatarios.jefesDe(e.sucursalId())) {
                enviar(creadorAlarma, jefe, evento, e.nombreProducto() + " al " + porcentaje + " %: realice una solicitud");
                enviar(creadorCorreo, jefe, evento, "Stock bajo: " + e.nombreProducto() + "\nQuedan " + stock + " de "
                        + e.nombreProducto() + " (" + porcentaje + " % del stock de referencia) en la " + sucursal
                        + ". Realice una solicitud de alimentos.");
            }
        } else if (evento instanceof SolicitudRegistradaEvent e) {
            enviar(creadorCorreo, e.correoSolicitante(), evento, "Solicitud " + e.numero() + " registrada\nSu solicitud "
                    + e.numero() + " quedó PENDIENTE.");
        } else if (evento instanceof SolicitudEnTramiteEvent e) {
            enviar(creadorCorreo, e.correoSolicitante(), evento, "Solicitud " + e.numero() + " en trámite\nSu solicitud "
                    + e.numero() + " ya tiene cotización y pasó a EN TRÁMITE.");
        } else if (evento instanceof CotizacionEnviadaEvent e) {
            // el correo certificado al proveedor lleva el PDF adjunto: el adaptador lo descarga del almacén (claim check)
            enviar(creadorCorreo, e.correoProveedor(), evento, "Solicitud de cotización " + e.numero()
                    + "\nPor favor cotice los productos del PDF adjunto.", e.pdfUrl());
        } else if (evento instanceof OrdenGeneradaEvent e) {
            enviar(creadorCorreo, e.correoProveedor(), evento, "Orden de compra " + e.numero() + "\nOrden " + e.numero()
                    + " sobre la cotización " + e.cotizacionNumero() + ", total " + pesos(e.total())
                    + ". El PDF va adjunto.", e.pdfUrl());
        } else if (evento instanceof TendenciaDetectadaEvent e) {
            // HU-19: correo de promociones a los jefes de cocina de todas las sucursales (la libreta, DEC-13); las
            // sugerencias llegan como líneas de texto (PD-05) y ms-analitica publica a lo sumo un evento por día
            StringBuilder cuerpo = new StringBuilder("Promociones sugeridas\nEl análisis de la semana sugiere:");
            for (String linea : e.sugerencias()) {
                cuerpo.append("\n• ").append(linea);
            }
            cuerpo.append("\nRevíselas en Promociones sugeridas para aceptarlas o descartarlas.");
            for (String jefe : destinatarios.jefes()) {
                enviar(creadorCorreo, jefe, evento, cuerpo.toString());
            }
        } else {
            throw new IllegalArgumentException("ms-notificaciones no atiende el evento " + evento.tipo());
        }
    }

    @Override
    public void registrarDestinatario(Long sucursalId, String correo) {
        if (sucursalId == null) {
            throw new IllegalArgumentException("La sucursal es obligatoria.");
        }
        if (correo == null || !correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido.");
        }
        destinatarios.guardar(sucursalId, correo);
        // CU-15: el registro de destinatarios también se audita (el adaptador pone quién lo hizo, DEC-64)
        eventos.publicar(new AuditoriaEvent("sistema", "Destinatario registrado: " + correo + " (sucursal " + sucursalId + ")"));
    }

    @Override
    public List<String> destinatarios(Long sucursalId) {
        return destinatarios.jefesDe(sucursalId);
    }

    /** Envía por el canal del creador y deja la traza; si falla, registra el fallo y relanza para que el evento vaya a la DLQ. */
    private void enviar(CreadorNotificacion creador, String destino, EventoDominio evento, String contenido) {
        enviar(creador, destino, evento, contenido, null);
    }

    private void enviar(CreadorNotificacion creador, String destino, EventoDominio evento, String contenido, String adjuntoUrl) {
        try {
            creador.notificar(destino, contenido, adjuntoUrl);
            envios.registrar(destino, evento.tipo(), LocalDateTime.now());
            eventos.publicar(new AuditoriaEvent("sistema", "Notificación " + evento.tipo() + " enviada a " + destino));
        } catch (RuntimeException e) {
            envios.registrarFallo(destino, evento.tipo(), e.getMessage());
            throw e;
        }
    }

    /** El total en pesos como en la aplicación y en el PDF de la orden: «$840.000» (DEC-115). */
    private static String pesos(BigDecimal v) {
        return "$" + new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO"))).format(v);
    }

}
