package co.edu.konradlorenz.integracion.adaptadores.entrada;

import co.edu.konradlorenz.integracion.puertos.entrada.ConsultarPrecioUseCase;

import java.math.BigDecimal;

/**
 * «REST interno · apoya V13/V14»
 *
 * En el proyecto real: @RestController.
 *
 * REST interno: no se publica en el gateway. Lo llama ms-cotizaciones con mTLS.
 */
public class PrecioRestController {

    private final ConsultarPrecioUseCase casoDeUso;

    public PrecioRestController(ConsultarPrecioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public BigDecimal precioSugerido(Long productoId) {
        return casoDeUso.precioSugerido(productoId);
    }

}
