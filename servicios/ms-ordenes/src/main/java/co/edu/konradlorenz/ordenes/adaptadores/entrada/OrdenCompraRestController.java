package co.edu.konradlorenz.ordenes.adaptadores.entrada;

import co.edu.konradlorenz.ordenes.configuracion.NombresProductos;
import co.edu.konradlorenz.ordenes.dominio.EstadoOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import co.edu.konradlorenz.ordenes.puertos.entrada.GenerarOrdenUseCase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * «REST · /api/ordenes-compra»
 *
 * Nota del diagrama: generar() rechaza una segunda orden sobre la misma cotización (existePorCotizacion), y
 * la base garantiza cotizacionNumero {único}, así un doble clic no crea dos órdenes. CotizacionConsultaPort
 * solo entrega cotizaciones OPCIONADA: si no, responde «Solo se pueden generar órdenes de compra sobre
 * cotizaciones opcionadas» (HU-14).
 *
 * Solo la directora de compras. generar() corre en una transacción: si el PDF o el almacén fallan, la orden
 * no queda guardada a medias (sin PDF y bloqueando la cotización). El número de la secuencia sí se gasta.
 */
@RestController
@RequestMapping("/api/ordenes-compra")
public class OrdenCompraRestController {

    private final GenerarOrdenUseCase casoDeUso;
    private final NombresProductos nombres;

    public OrdenCompraRestController(GenerarOrdenUseCase casoDeUso, NombresProductos nombres) {
        this.casoDeUso = casoDeUso;
        this.nombres = nombres;
    }

    /** V21: la cotización OPCIONADA que la directora escogió en V20. */
    public record NuevaOrden(String cotizacionNumero) {
    }

    public record OrdenJson(String numero, LocalDate fecha, String cotizacion, List<String> solicitudes, String proveedor,
                            String correoProveedor, String estado, boolean pdf, List<ItemJson> items, BigDecimal total) {
    }

    /** precio es por unidad; valor = cantidad × precio. */
    public record ItemJson(Long productoId, String producto, BigDecimal cantidad, String unidad, BigDecimal precio, BigDecimal valor) {
    }

    // Atiende la ventana V21 de los mockups
    // Atiende la ventana V22 de los mockups
    // POST /api/ordenes-compra → 201 Created
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DIRECTOR_COMPRAS')")
    @Transactional
    public OrdenJson generar(@RequestBody NuevaOrden datos) {
        if (datos == null || datos.cotizacionNumero() == null || datos.cotizacionNumero().isBlank()) {
            throw new IllegalArgumentException("Indica la cotización de la que sale la orden");
        }
        return json(casoDeUso.generar(datos.cotizacionNumero().trim()), nombres.todos());
    }

    // Atiende la ventana V23 de los mockups
    // GET /api/ordenes-compra (?estado=EN_PROCESO o FINALIZADA; sin estado, todas)
    @GetMapping
    @PreAuthorize("hasRole('DIRECTOR_COMPRAS')")
    public List<OrdenJson> consultar(@RequestParam(required = false) String estado) {
        EstadoOrden e = null;
        if (estado != null && !estado.isBlank()) {
            try {
                e = EstadoOrden.valueOf(estado.trim());
            } catch (IllegalArgumentException x) {
                throw new IllegalArgumentException("Estado desconocido: " + estado);
            }
        }
        Map<Long, String> n = nombres.todos();
        return casoDeUso.consultar(e).stream().sorted(Comparator.comparing(OrdenCompra::numero)).map(o -> json(o, n)).toList();
    }

    // Atiende la ventana V23 de los mockups
    // GET /api/ordenes-compra/{id}/pdf («Ver PDF», DEC-75)
    @GetMapping("/{numero}/pdf")
    @PreAuthorize("hasRole('DIRECTOR_COMPRAS')")
    public ResponseEntity<?> pdf(@PathVariable String numero, @RequestParam(defaultValue = "false") boolean descargar) {
        byte[] pdf;
        try {
            pdf = casoDeUso.pdf(numero);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
        ContentDisposition disposicion = (descargar ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(numero + ".pdf").build();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString()).body(pdf);
    }

    /** Dos peticiones a la vez sobre la misma cotización: la base rechaza la segunda (cotizacionNumero {único}). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> repetida(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", "Ya existe una orden de compra para esa cotización"));
    }

    /** ms-cotizaciones no respondió: no es un error de la directora, se puede intentar de nuevo. */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, String>> sinCotizaciones(RestClientException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("mensaje", "No se pudo leer la cotización en ms-cotizaciones. Intenta de nuevo en un momento."));
    }

    private static OrdenJson json(OrdenCompra o, Map<Long, String> n) {
        List<ItemJson> items = o.items().stream().map(i -> new ItemJson(i.productoId(), NombresProductos.de(n, i.productoId()), i.cantidad(),
                i.unidad(), i.precio(), i.precio().multiply(i.cantidad()))).toList();
        return new OrdenJson(o.numero(), o.fecha(), o.cotizacionNumero(), o.solicitudNumeros(), o.proveedor(), o.correoProveedor(),
                o.estado().name(), o.pdfUrl() != null, items, o.total());
    }

}
