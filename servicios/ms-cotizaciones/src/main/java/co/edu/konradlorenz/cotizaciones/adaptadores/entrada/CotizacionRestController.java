package co.edu.konradlorenz.cotizaciones.adaptadores.entrada;

import co.edu.konradlorenz.cotizaciones.configuracion.NombresProductos;
import co.edu.konradlorenz.cotizaciones.configuracion.NumeracionCotizaciones;
import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ItemCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarCotizacionUseCase;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarProveedorUseCase;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * «REST · /api/cotizaciones · GET …/{id} · POST …/{id}/reenvio»
 *
 * El auxiliar de compras genera y envía la cotización de una o varias solicitudes (V13–V14) y consulta las
 * suyas (V15); el director de compras ve todas y las pendientes (V16–V17), registra los valores del
 * proveedor (V18) y valida (V19). Las dos ven el detalle y el PDF.
 */
@RestController
@RequestMapping("/api/cotizaciones")
public class CotizacionRestController {

    private final GestionarCotizacionUseCase casoDeUso;
    private final GestionarProveedorUseCase proveedores;
    private final NumeracionCotizaciones numeracion;
    private final NombresProductos nombres;

    public CotizacionRestController(GestionarCotizacionUseCase casoDeUso, GestionarProveedorUseCase proveedores,
                                    NumeracionCotizaciones numeracion, NombresProductos nombres) {
        this.casoDeUso = casoDeUso;
        this.proveedores = proveedores;
        this.numeracion = numeracion;
        this.nombres = nombres;
    }

    /** V13: las solicitudes escogidas, el proveedor, la fecha límite y los productos sumados de esas solicitudes. */
    public record NuevaCotizacion(List<String> solicitudes, Long proveedorId, String fechaLimite, List<Linea> items) {
    }

    /** «producto» es el nombre que la SPA leyó de ms-menu: se guarda como copia local (DEC-74). */
    public record Linea(Long productoId, String producto, BigDecimal cantidad, String unidad) {
    }

    /** V18: el precio por unidad del proveedor de cada producto. */
    public record Valores(Map<Long, BigDecimal> precios) {
    }

    public record CotizacionJson(String numero, List<String> solicitudes, Long proveedorId, String proveedor, String correoProveedor,
                                 LocalDate fechaEnvio, LocalDate fechaLimite, String estado, boolean pdf, List<ItemJson> items,
                                 BigDecimal estandar, BigDecimal cotizado, BigDecimal diferencia) {
    }

    public record ItemJson(Long productoId, String producto, BigDecimal cantidad, String unidad, BigDecimal precioEstandar,
                           BigDecimal precioProveedor) {
    }

    // Atiende la ventana V13 de los mockups
    // POST /api/cotizaciones → 201 Created (ENVIADA, o PENDIENTE_PRECIO si el Ministerio no respondió)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('AUXILIAR_COMPRAS')")
    public CotizacionJson generar(@RequestBody NuevaCotizacion datos) {
        if (datos.proveedorId() == null) {
            throw new IllegalArgumentException("Selecciona el proveedor");
        }
        Proveedor proveedor = proveedores.detalle(datos.proveedorId());
        if (!proveedor.activo()) {
            throw new IllegalStateException("El proveedor " + proveedor.razonSocial() + " está inactivo");
        }
        LocalDate limite = fechaLimite(datos.fechaLimite());
        List<String> solicitudes = datos.solicitudes() == null ? List.of()
                : datos.solicitudes().stream().filter(s -> s != null && !s.isBlank()).map(String::trim).distinct().toList();
        List<ItemCotizacion> items = new ArrayList<>();
        Map<Long, String> nombresNuevos = new LinkedHashMap<>();
        if (datos.items() != null) {
            for (Linea l : datos.items()) {
                if (l.productoId() == null || l.cantidad() == null || l.cantidad().signum() <= 0) {
                    throw new IllegalArgumentException("Cada producto necesita una cantidad mayor que cero");
                }
                if (l.unidad() == null || l.unidad().isBlank()) {
                    throw new IllegalArgumentException("Cada producto necesita su unidad");
                }
                if (items.stream().anyMatch(i -> i.productoId().equals(l.productoId()))) {
                    throw new IllegalArgumentException("El producto " + l.productoId() + " está repetido: la SPA suma las cantidades");
                }
                items.add(new ItemCotizacion(l.productoId(), l.cantidad(), l.unidad().trim()));
                nombresNuevos.put(l.productoId(), l.producto());
            }
        }
        // las reglas de HU-09 (al menos una solicitud y un producto) las aplica el núcleo; si fallan no se gasta número
        new Cotizacion("—", solicitudes, proveedor, limite, items).validarSolicitudes();
        nombres.recordar(nombresNuevos);
        return json(casoDeUso.generar(new Cotizacion(numeracion.siguiente(), solicitudes, proveedor, limite, items)));
    }

    // Atiende la ventana V14 de los mockups (también V15 y V17: «Ver PDF» y «Descargar»)
    // GET /api/cotizaciones/{id}/pdf
    @GetMapping("/{numero}/pdf")
    @PreAuthorize("hasAnyRole('AUXILIAR_COMPRAS', 'DIRECTOR_COMPRAS')")
    public ResponseEntity<?> pdf(@PathVariable String numero, @RequestParam(defaultValue = "false") boolean descargar) {
        if (!existe(numero)) {
            return noExiste(numero);
        }
        byte[] pdf = casoDeUso.pdf(numero);
        ContentDisposition disposicion = (descargar ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(numero + ".pdf").build();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString()).body(pdf);
    }

    // Atiende la ventana V15 de los mockups
    // Atiende la ventana V17 de los mockups
    // GET /api/cotizaciones (?estado=ENVIADA…; sin estado, todas)
    @GetMapping
    @PreAuthorize("hasAnyRole('AUXILIAR_COMPRAS', 'DIRECTOR_COMPRAS')")
    public List<CotizacionJson> consultar(@RequestParam(required = false) String estado) {
        EstadoCotizacion e = null;
        if (estado != null && !estado.isBlank()) {
            try {
                e = EstadoCotizacion.valueOf(estado.trim());
            } catch (IllegalArgumentException x) {
                throw new IllegalArgumentException("Estado desconocido: " + estado);
            }
        }
        return json(casoDeUso.consultar(e));
    }

    // Atiende la ventana V16 de los mockups
    // GET /api/cotizaciones/pendientes
    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('DIRECTOR_COMPRAS')")
    public List<CotizacionJson> pendientesDeAtencion() {
        // pendientes de validar: las que ya se enviaron y esperan los valores del proveedor
        return json(casoDeUso.consultar(EstadoCotizacion.ENVIADA));
    }

    // Atiende la ventana V18 de los mockups
    // PUT /api/cotizaciones/{id}/valores
    @PutMapping("/{numero}/valores")
    @PreAuthorize("hasRole('DIRECTOR_COMPRAS')")
    public ResponseEntity<?> registrarValores(@PathVariable String numero, @RequestBody Valores valores) {
        if (!existe(numero)) {
            return noExiste(numero);
        }
        if (valores.precios() == null || valores.precios().isEmpty()) {
            throw new IllegalArgumentException("Ingresa el precio del proveedor de al menos un producto");
        }
        return ResponseEntity.ok(json(casoDeUso.registrarValores(numero, valores.precios())));
    }

    // Atiende la ventana V19 de los mockups
    // POST /api/cotizaciones/{id}/validacion
    @PostMapping("/{numero}/validacion")
    @PreAuthorize("hasRole('DIRECTOR_COMPRAS')")
    public ResponseEntity<?> validar(@PathVariable String numero) {
        if (!existe(numero)) {
            return noExiste(numero);
        }
        return ResponseEntity.ok(json(casoDeUso.validar(numero)));
    }

    // Atiende la ventana V20 de los mockups
    // Atiende la ventana V21 de los mockups
    // GET /api/cotizaciones/{id}
    @GetMapping("/{numero}")
    @PreAuthorize("hasAnyRole('AUXILIAR_COMPRAS', 'DIRECTOR_COMPRAS')")
    public ResponseEntity<?> detalle(@PathVariable String numero) {
        if (!existe(numero)) {
            return noExiste(numero);
        }
        return ResponseEntity.ok(json(casoDeUso.detalle(numero)));
    }

    // POST /api/cotizaciones/{id}/reenvio
    @PostMapping("/{numero}/reenvio")
    @PreAuthorize("hasRole('AUXILIAR_COMPRAS')")
    public ResponseEntity<?> reenviar(@PathVariable String numero) {
        if (!existe(numero)) {
            return noExiste(numero);
        }
        casoDeUso.reenviar(numero);
        return ResponseEntity.ok(json(casoDeUso.detalle(numero)));
    }

    private boolean existe(String numero) {
        try {
            casoDeUso.detalle(numero);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static ResponseEntity<Map<String, String>> noExiste(String numero) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "No existe la cotización " + numero));
    }

    /** La fecha máxima para cotizar: obligatoria y desde hoy (V13). */
    private static LocalDate fechaLimite(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Indica la fecha máxima para cotizar");
        }
        LocalDate limite;
        try {
            limite = LocalDate.parse(texto.trim());
        } catch (DateTimeParseException x) {
            throw new IllegalArgumentException("La fecha máxima debe ir como AAAA-MM-DD");
        }
        if (limite.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha máxima para cotizar no puede ser anterior a hoy");
        }
        return limite;
    }

    private List<CotizacionJson> json(List<Cotizacion> lista) {
        Map<Long, String> n = nombres.todos();
        return lista.stream().map(c -> json(c, n)).toList();
    }

    private CotizacionJson json(Cotizacion c) {
        return json(c, nombres.todos());
    }

    /** Los totales y la diferencia salen del núcleo cuando hay con qué calcularlos; si no, van en null. */
    private static CotizacionJson json(Cotizacion c, Map<Long, String> n) {
        boolean conEstandar = !c.items().isEmpty() && c.items().stream().allMatch(i -> i.precioEstandar() != null);
        boolean conProveedor = !c.items().isEmpty() && c.items().stream().allMatch(i -> i.precioProveedor() != null);
        BigDecimal estandar = conEstandar ? c.valorEstandarTotal() : null;
        BigDecimal cotizado = conProveedor ? c.valorCotizadoTotal() : null;
        BigDecimal diferencia = conEstandar && conProveedor && estandar.signum() != 0 ? c.diferenciaPorcentual() : null;
        List<ItemJson> items = c.items().stream().map(i -> new ItemJson(i.productoId(), NombresProductos.de(n, i.productoId()), i.cantidad(),
                i.unidad(), i.precioEstandar(), i.precioProveedor())).toList();
        return new CotizacionJson(c.numero(), c.solicitudNumeros(), c.proveedor().id(), c.proveedor().razonSocial(), c.proveedor().correo(),
                c.fechaEnvio(), c.fechaLimite(), c.estado().name(), c.pdfUrl() != null, items, estandar, cotizado, diferencia);
    }

}
