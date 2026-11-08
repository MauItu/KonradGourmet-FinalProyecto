package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import co.edu.konradlorenz.cotizaciones.puertos.salida.PrecioSugeridoPort;

import java.math.BigDecimal;

/**
 * «REST cliente → ms-integracion»
 *
 * En el proyecto real: @Component.
 */
public class PrecioSugeridoRestAdapter implements PrecioSugeridoPort {

    private final String urlIntegracion;

    public PrecioSugeridoRestAdapter(String urlIntegracion) {
        this.urlIntegracion = urlIntegracion;
    }

    @Override
    public BigDecimal precioSugerido(Long productoId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
