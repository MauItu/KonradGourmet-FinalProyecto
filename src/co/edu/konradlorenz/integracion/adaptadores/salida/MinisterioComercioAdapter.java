package co.edu.konradlorenz.integracion.adaptadores.salida;

import co.edu.konradlorenz.integracion.dominio.PrecioEstandar;
import co.edu.konradlorenz.integracion.puertos.salida.PrecioReferenciaPort;

/**
 * «Adapter»
 *
 * En el proyecto real: @Component.
 */
public class MinisterioComercioAdapter implements PrecioReferenciaPort {

    private final MinisterioSoapClient cliente;

    public MinisterioComercioAdapter(MinisterioSoapClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public PrecioEstandar precioEstandar(Long productoId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
