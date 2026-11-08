package co.edu.konradlorenz.cotizaciones.aplicacion;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ItemCotizacion;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionEnviadaEvent;
import co.edu.konradlorenz.cotizaciones.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.CotizacionRepositoryPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.GeneradorPdfPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.PrecioSugeridoPort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * «Facade»
 *
 * Nota del diagrama: Si el servicio del Ministerio no responde, la fachada guarda la cotización como
 * PENDIENTE_PRECIO, deja el error en el log y avisa al usuario sin perderla. reenviar() (POST
 * …/{id}/reenvio) reintenta: completa el precio, o vuelve a publicar cotizacion.enviada si falló el
 * correo (HU-12).
 */
public class GenerarCotizacionFacade {

    private static final Logger LOG = Logger.getLogger(GenerarCotizacionFacade.class.getName());

    private final CotizacionRepositoryPort repositorio;
    private final PrecioSugeridoPort precios;
    private final GeneradorPdfPort pdf;
    private final AlmacenArchivosPort almacen;
    private final EventPublisherPort eventos;

    public GenerarCotizacionFacade(CotizacionRepositoryPort repositorio, PrecioSugeridoPort precios, GeneradorPdfPort pdf, AlmacenArchivosPort almacen, EventPublisherPort eventos) {
        this.repositorio = repositorio;
        this.precios = precios;
        this.pdf = pdf;
        this.almacen = almacen;
        this.eventos = eventos;
    }

    public Cotizacion generarYEnviar(Cotizacion c) {
        c.validarSolicitudes();
        // 1. precio estándar de cada producto (el Proxy responde desde la caché o consulta al Ministerio)
        List<ItemCotizacion> conPrecio = new ArrayList<>();
        try {
            for (ItemCotizacion i : c.items()) {
                conPrecio.add(new ItemCotizacion(i.productoId(), i.cantidad(), i.unidad(),
                        precios.precioSugerido(i.productoId()), i.precioProveedor()));
            }
        } catch (RuntimeException e) {
            // el Ministerio no responde: se guarda sin perderla, queda en el log y el usuario puede reenviar
            LOG.log(Level.WARNING, "No se pudo consultar el precio estándar de " + c.numero() + ": " + e.getMessage());
            c.cambiarEstado(EstadoCotizacion.PENDIENTE_PRECIO);
            return repositorio.guardar(c);
        }
        c.items().clear();
        c.items().addAll(conPrecio);
        c.cambiarEstado(EstadoCotizacion.ENVIADA);
        // 2. PDF al almacén de objetos (claim check): el evento lleva solo la URL
        String pdfUrl = almacen.guardar(c.numero() + ".pdf", pdf.generar(c));
        Cotizacion enviada = repositorio.guardar(new Cotizacion(c, pdfUrl));
        // 3. evento cotizacion.enviada con la lista de solicitudes y el precio sugerido de cada producto
        Map<Long, BigDecimal> sugeridos = new LinkedHashMap<>();
        enviada.items().forEach(i -> sugeridos.put(i.productoId(), i.precioEstandar()));
        eventos.publicar(new CotizacionEnviadaEvent("sistema", enviada.numero(), enviada.solicitudNumeros(),
                sugeridos, enviada.proveedor().correo(), pdfUrl));
        eventos.publicar(new AuditoriaEvent("sistema", "Cotización " + enviada.numero() + " enviada a " + enviada.proveedor().razonSocial()));
        return enviada;
    }

}
