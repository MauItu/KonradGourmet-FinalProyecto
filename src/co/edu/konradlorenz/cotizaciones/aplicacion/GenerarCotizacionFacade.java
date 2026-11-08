package co.edu.konradlorenz.cotizaciones.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.CotizacionRepositoryPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.GeneradorPdfPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.PrecioSugeridoPort;

/**
 * «Facade»
 *
 * Nota del diagrama: Si el servicio del Ministerio no responde, la fachada guarda la cotización como
 * PENDIENTE_PRECIO, deja el error en el log y avisa al usuario sin perderla. reenviar() (POST
 * …/{id}/reenvio) reintenta: completa el precio, o vuelve a publicar cotizacion.enviada si falló el
 * correo (HU-12).
 */
public class GenerarCotizacionFacade {

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
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
