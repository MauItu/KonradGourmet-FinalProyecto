package co.edu.konradlorenz.solicitudes.adaptadores.entrada;

import co.edu.konradlorenz.compartido.infraestructura.Sesion;
import co.edu.konradlorenz.solicitudes.dominio.CriteriosConsulta;
import co.edu.konradlorenz.solicitudes.dominio.EstadoSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.ItemSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;
import co.edu.konradlorenz.solicitudes.puertos.entrada.GestionarSolicitudUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * «REST · /api/solicitudes»
 *
 * El jefe de cocina registra sus solicitudes y consulta las suyas (V08–V10); la sucursal, el solicitante
 * y su correo salen del JWT, nunca del cuerpo. El auxiliar de compras consulta las de todas las
 * sucursales con los criterios de HU-07 y abre el detalle (V11–V12). El jefe también abre el detalle,
 * pero solo de sus solicitudes.
 */
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudRestController {

    private final GestionarSolicitudUseCase casoDeUso;

    public SolicitudRestController(GestionarSolicitudUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    /** V08: los productos de la solicitud; la marca es opcional. */
    public record NuevaSolicitud(List<Linea> items) {
    }

    public record Linea(Long productoId, BigDecimal cantidad, String marca, String unidad) {
    }

    public record SolicitudJson(String numero, Long sucursalId, Long solicitanteId, String correoSolicitante, LocalDateTime fecha,
                                String estado, BigDecimal valorSugeridoTotal, List<ItemJson> items) {
    }

    public record ItemJson(Long productoId, BigDecimal cantidad, String marca, String unidad, boolean cotizado, BigDecimal precioSugerido) {
    }

    // Atiende la ventana V08 de los mockups
    // Atiende la ventana V09 de los mockups
    // POST /api/solicitudes → 201 Created
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('JEFE_COCINA')")
    public SolicitudJson registrar(@RequestBody NuevaSolicitud datos, @AuthenticationPrincipal Jwt jwt) {
        SolicitudAlimentos s = new SolicitudAlimentos(null, Sesion.sucursalId(jwt), Sesion.usuarioId(jwt), jwt.getClaimAsString("correo"));
        if (datos.items() != null) {
            for (Linea l : datos.items()) {
                if (l.unidad() == null || l.unidad().isBlank()) {
                    throw new IllegalArgumentException("Cada producto necesita su unidad");
                }
                String marca = l.marca() == null || l.marca().isBlank() ? null : l.marca().trim();
                s.agregarItem(new ItemSolicitud(l.productoId(), l.cantidad(), marca, l.unidad().trim()));
            }
        }
        return json(casoDeUso.registrar(s));
    }

    // Atiende la ventana V10 de los mockups
    // GET /api/solicitudes?solicitante=yo (el jefe sale del token; admite los mismos criterios de V11)
    @GetMapping(params = "solicitante")
    @PreAuthorize("hasRole('JEFE_COCINA')")
    public List<SolicitudJson> misSolicitudes(@RequestParam String solicitante, @RequestParam(required = false) String desde,
                                              @RequestParam(required = false) String hasta, @RequestParam(required = false) Long producto,
                                              @RequestParam(required = false) String marca, @RequestParam(required = false) String estado,
                                              @AuthenticationPrincipal Jwt jwt) {
        if (!"yo".equals(solicitante)) {
            throw new IllegalArgumentException("Solo se consultan las solicitudes propias (solicitante=yo)");
        }
        Long yo = Sesion.usuarioId(jwt);
        return casoDeUso.consultar(criterios(desde, hasta, producto, marca, estado)).stream()
                .filter(s -> yo.equals(s.solicitanteId())).map(SolicitudRestController::json).toList();
    }

    // Atiende la ventana V11 de los mockups
    // GET /api/solicitudes?estado=PENDIENTE (y los demás criterios: desde, hasta, producto, marca)
    @GetMapping
    @PreAuthorize("hasRole('AUXILIAR_COMPRAS')")
    public List<SolicitudJson> consultar(@RequestParam(required = false) String desde, @RequestParam(required = false) String hasta,
                                         @RequestParam(required = false) Long producto, @RequestParam(required = false) String marca,
                                         @RequestParam(required = false) String estado) {
        return casoDeUso.consultar(criterios(desde, hasta, producto, marca, estado)).stream().map(SolicitudRestController::json).toList();
    }

    // Atiende la ventana V12 de los mockups
    // GET /api/solicitudes/{id}
    @GetMapping("/{numero}")
    @PreAuthorize("hasAnyRole('AUXILIAR_COMPRAS', 'JEFE_COCINA')")
    public ResponseEntity<?> detalle(@PathVariable String numero, @AuthenticationPrincipal Jwt jwt) {
        SolicitudAlimentos s;
        try {
            s = casoDeUso.detalle(numero);
        } catch (IllegalArgumentException e) {
            return noExiste(numero);
        }
        boolean esJefe = "JEFE_COCINA".equals(jwt.getClaimAsString("rol"));
        if (esJefe && !Sesion.usuarioId(jwt).equals(s.solicitanteId())) {
            return noExiste(numero); // la de otro jefe no se muestra, ni se dice que existe
        }
        return ResponseEntity.ok(json(s));
    }

    private static ResponseEntity<Map<String, String>> noExiste(String numero) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "No existe la solicitud " + numero));
    }

    /** Los filtros de HU-07; uno vacío no filtra. */
    private static CriteriosConsulta criterios(String desde, String hasta, Long producto, String marca, String estado) {
        LocalDate d = fecha(desde, "desde");
        LocalDate h = fecha(hasta, "hasta");
        if (d != null && h != null && d.isAfter(h)) {
            throw new IllegalArgumentException("La fecha «desde» es posterior a «hasta»");
        }
        EstadoSolicitud e = null;
        if (estado != null && !estado.isBlank()) {
            try {
                e = EstadoSolicitud.valueOf(estado.trim());
            } catch (IllegalArgumentException x) {
                throw new IllegalArgumentException("Estado desconocido: " + estado + " (PENDIENTE, EN_TRAMITE o COMPLETADA)");
            }
        }
        return new CriteriosConsulta(d, h, producto, marca == null || marca.isBlank() ? null : marca.trim(), e);
    }

    private static LocalDate fecha(String texto, String nombre) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException x) {
            throw new IllegalArgumentException("La fecha «" + nombre + "» debe ir como AAAA-MM-DD");
        }
    }

    private static SolicitudJson json(SolicitudAlimentos s) {
        return new SolicitudJson(s.numero(), s.sucursalId(), s.solicitanteId(), s.correoSolicitante(), s.fecha(), s.estado().name(),
                s.valorSugeridoTotal(), s.items().stream()
                .map(i -> new ItemJson(i.productoId(), i.cantidad(), i.marca(), i.unidad(), i.cotizado(), i.precioSugerido())).toList());
    }

}
