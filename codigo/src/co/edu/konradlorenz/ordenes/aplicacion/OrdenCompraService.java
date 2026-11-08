package co.edu.konradlorenz.ordenes.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.ordenes.dominio.EstadoOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import co.edu.konradlorenz.ordenes.puertos.entrada.GenerarOrdenUseCase;
import co.edu.konradlorenz.ordenes.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.ordenes.puertos.salida.CotizacionConsultaPort;
import co.edu.konradlorenz.ordenes.puertos.salida.GeneradorPdfPort;
import co.edu.konradlorenz.ordenes.puertos.salida.OrdenRepositoryPort;

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
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<OrdenCompra> consultar(EstadoOrden estado) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
