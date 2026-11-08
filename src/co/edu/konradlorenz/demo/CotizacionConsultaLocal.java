package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ItemCotizacion;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarCotizacionUseCase;
import co.edu.konradlorenz.ordenes.dominio.CotizacionOpcionada;
import co.edu.konradlorenz.ordenes.dominio.ItemOrden;
import co.edu.konradlorenz.ordenes.puertos.salida.CotizacionConsultaPort;

import java.util.ArrayList;
import java.util.List;

/**
 * Hace las veces de CotizacionRestClient: en vez de GET /api/cotizaciones/{id}, llama al caso de uso de
 * ms-cotizaciones y traduce la respuesta al tipo de ms-ordenes. Solo entrega cotizaciones OPCIONADA.
 */
public class CotizacionConsultaLocal implements CotizacionConsultaPort {

    private final GestionarCotizacionUseCase cotizaciones;

    public CotizacionConsultaLocal(GestionarCotizacionUseCase cotizaciones) {
        this.cotizaciones = cotizaciones;
    }

    @Override
    public CotizacionOpcionada opcionada(String numero) {
        Cotizacion c = cotizaciones.detalle(numero);
        if (c.estado() != EstadoCotizacion.OPCIONADA) {
            throw new IllegalStateException("Solo se pueden generar órdenes de compra sobre cotizaciones opcionadas");
        }
        List<ItemOrden> items = new ArrayList<>();
        for (ItemCotizacion i : c.items()) {
            items.add(new ItemOrden(i.productoId(), i.cantidad(), i.unidad(), i.precioProveedor()));
        }
        return new CotizacionOpcionada(c.numero(), c.solicitudNumeros(), c.proveedor().razonSocial(), c.proveedor().correo(), items);
    }

}
