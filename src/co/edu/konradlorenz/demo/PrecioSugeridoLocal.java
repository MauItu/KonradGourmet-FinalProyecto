package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.cotizaciones.puertos.salida.PrecioSugeridoPort;
import co.edu.konradlorenz.integracion.puertos.entrada.ConsultarPrecioUseCase;

import java.math.BigDecimal;

/**
 * Hace las veces de PrecioSugeridoRestAdapter: en vez de una llamada REST con mTLS de ms-cotizaciones a
 * ms-integracion, llama directo al caso de uso de ms-integracion.
 */
public class PrecioSugeridoLocal implements PrecioSugeridoPort {

    private final ConsultarPrecioUseCase integracion;

    public PrecioSugeridoLocal(ConsultarPrecioUseCase integracion) {
        this.integracion = integracion;
    }

    @Override
    public BigDecimal precioSugerido(Long productoId) {
        return integracion.precioSugerido(productoId);
    }

}
