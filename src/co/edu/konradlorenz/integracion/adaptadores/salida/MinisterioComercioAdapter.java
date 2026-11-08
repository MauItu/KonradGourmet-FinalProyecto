package co.edu.konradlorenz.integracion.adaptadores.salida;

import co.edu.konradlorenz.integracion.dominio.PrecioEstandar;
import co.edu.konradlorenz.integracion.puertos.salida.PrecioReferenciaPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        // el Ministerio identifica el producto con un código de texto y responde un double: aquí se traduce al puerto
        double valor = cliente.consultarPrecioEstandar(String.valueOf(productoId));
        return new PrecioEstandar(productoId, BigDecimal.valueOf(valor), LocalDateTime.now());
    }

}
