package co.edu.konradlorenz.cotizaciones.adaptadores.entrada;

import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarCotizacionUseCase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * «REST · /api/cotizaciones · GET …/{id} · POST …/{id}/reenvio»
 *
 * En el proyecto real: @RestController.
 */
public class CotizacionRestController {

    private final GestionarCotizacionUseCase casoDeUso;

    public CotizacionRestController(GestionarCotizacionUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    // Atiende la ventana V13 de los mockups
    // POST /api/cotizaciones
    public Cotizacion generar(Cotizacion c) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V14 de los mockups
    // GET /api/cotizaciones/{id}/pdf
    public byte[] pdf(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V15 de los mockups
    // Atiende la ventana V17 de los mockups
    // GET /api/cotizaciones
    public List<Cotizacion> consultar(EstadoCotizacion estado) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V16 de los mockups
    // GET /api/cotizaciones/pendientes
    public List<Cotizacion> pendientesDeAtencion() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V18 de los mockups
    // PUT /api/cotizaciones/{id}/valores
    public Cotizacion registrarValores(String numero, Map<Long, BigDecimal> precios) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V19 de los mockups
    // POST /api/cotizaciones/{id}/validacion
    public Cotizacion validar(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V20 de los mockups
    // GET /api/cotizaciones?estado=OPCIONADA
    public List<Cotizacion> opcionadas() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V20 de los mockups
    // Atiende la ventana V21 de los mockups
    // GET /api/cotizaciones/{id}
    public Cotizacion detalle(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // POST /api/cotizaciones/{id}/reenvio
    public void reenviar(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
