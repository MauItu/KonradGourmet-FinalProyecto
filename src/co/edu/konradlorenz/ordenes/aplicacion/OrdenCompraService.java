package co.edu.konradlorenz.ordenes.aplicacion;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.ordenes.dominio.CotizacionOpcionada;
import co.edu.konradlorenz.ordenes.dominio.EstadoOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import co.edu.konradlorenz.ordenes.puertos.entrada.GenerarOrdenUseCase;
import co.edu.konradlorenz.ordenes.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.ordenes.puertos.salida.CotizacionConsultaPort;
import co.edu.konradlorenz.ordenes.puertos.salida.GeneradorPdfPort;
import co.edu.konradlorenz.ordenes.puertos.salida.OrdenRepositoryPort;

import java.util.ArrayList;
import java.util.List;

public class OrdenCompraService implements GenerarOrdenUseCase {

    private final OrdenRepositoryPort repositorio;
    private final CotizacionConsultaPort cotizaciones;
    private final GeneradorPdfPort pdf;
    private final AlmacenArchivosPort almacen;
    private final EventPublisherPort eventos;

    public OrdenCompraService(OrdenRepositoryPort repositorio, CotizacionConsultaPort cotizaciones, GeneradorPdfPort pdf, AlmacenArchivosPort almacen, EventPublisherPort eventos) {
        this.repositorio = repositorio;
        this.cotizaciones = cotizaciones;
        this.pdf = pdf;
        this.almacen = almacen;
        this.eventos = eventos;
    }

    @Override
    public OrdenCompra generar(String cotizacionNumero) {
        if (repositorio.existePorCotizacion(cotizacionNumero)) {
            throw new IllegalStateException("Ya existe una orden de compra para la cotización " + cotizacionNumero);
        }
        // el puerto solo entrega cotizaciones OPCIONADA; si no, lanza «Solo se pueden generar órdenes…»
        CotizacionOpcionada c = cotizaciones.opcionada(cotizacionNumero);
        OrdenCompra numerada = repositorio.guardar(OrdenCompra.desde(c));
        String pdfUrl = almacen.guardar(numerada.numero() + ".pdf", pdf.generar(numerada));
        OrdenCompra orden = repositorio.guardar(new OrdenCompra(numerada, pdfUrl));
        eventos.publicar(new OrdenGeneradaEvent("sistema", orden.numero(), orden.cotizacionNumero(), orden.solicitudNumeros(),
                orden.proveedor(), orden.correoProveedor(), pdfUrl, orden.total()));
        eventos.publicar(new AuditoriaEvent("sistema", "Orden " + orden.numero() + " generada sobre " + orden.cotizacionNumero()));
        return orden;
    }

    @Override
    public List<OrdenCompra> consultar(EstadoOrden estado) {
        if (estado != null) {
            return repositorio.porEstado(estado);
        }
        List<OrdenCompra> todas = new ArrayList<>();
        for (EstadoOrden e : EstadoOrden.values()) {
            todas.addAll(repositorio.porEstado(e));
        }
        return todas;
    }

}
