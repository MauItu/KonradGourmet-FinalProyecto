package co.edu.konradlorenz.integracion.adaptadores.entrada;

import co.edu.konradlorenz.integracion.puertos.entrada.ConsultarPrecioUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * «REST interno · apoya V13/V14»
 *
 * REST interno: no se publica en el gateway (que solo enruta /api/integracion/** al SOAP). Lo llama
 * ms-cotizaciones (PrecioSugeridoRestAdapter, corte 4b) con el JWT de quien genera o valida la
 * cotización; en producción, además, con mTLS (en local no hay mTLS, DEC-58).
 */
@RestController
@RequestMapping("/interno/precios")
@PreAuthorize("hasAnyRole('AUXILIAR_COMPRAS', 'DIRECTOR_COMPRAS')")
public class PrecioRestController {

    private final ConsultarPrecioUseCase casoDeUso;

    public PrecioRestController(ConsultarPrecioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    // GET /interno/precios/{productoId}
    @GetMapping("/{productoId}")
    public BigDecimal precioSugerido(@PathVariable Long productoId) {
        return casoDeUso.precioSugerido(productoId);
    }

}
