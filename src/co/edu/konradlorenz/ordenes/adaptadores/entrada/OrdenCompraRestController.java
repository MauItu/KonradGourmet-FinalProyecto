package co.edu.konradlorenz.ordenes.adaptadores.entrada;

import co.edu.konradlorenz.ordenes.dominio.EstadoOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import co.edu.konradlorenz.ordenes.puertos.entrada.GenerarOrdenUseCase;

import java.util.List;

/**
 * «REST · /api/ordenes-compra»
 *
 * En el proyecto real: @RestController.
 *
 * Nota del diagrama: generar() rechaza una segunda orden sobre la misma cotización
 * (existePorCotizacion), y la base garantiza cotizacionNumero {único}, así un doble clic no crea dos
 * órdenes. CotizacionConsultaPort solo entrega cotizaciones OPCIONADA: si no, responde «Solo se pueden
 * generar órdenes de compra sobre cotizaciones opcionadas» (HU-14).
 */
public class OrdenCompraRestController {

    private final GenerarOrdenUseCase casoDeUso;

    public OrdenCompraRestController(GenerarOrdenUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    // Atiende la ventana V21 de los mockups
    // Atiende la ventana V22 de los mockups
    // POST /api/ordenes-compra → 201 Created
    public OrdenCompra generar(String cotizacionNumero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V23 de los mockups
    // GET /api/ordenes-compra
    public List<OrdenCompra> consultar(EstadoOrden estado) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
