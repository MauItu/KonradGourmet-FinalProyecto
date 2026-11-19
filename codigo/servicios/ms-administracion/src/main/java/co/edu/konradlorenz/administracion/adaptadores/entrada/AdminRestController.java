package co.edu.konradlorenz.administracion.adaptadores.entrada;

import co.edu.konradlorenz.administracion.puertos.entrada.AdministracionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * «REST · /api/admin · GET …/parametros/tema»
 *
 * Todo es del administrador (PD-06), salvo el tema, que lee cualquier usuario con sesión: la SPA lo aplica al
 * iniciar sesión, sin redespliegue (HU-20, DEC-86). La auditoría y los logs son de solo lectura (HU-21, HU-22).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminRestController.class);

    private final AdministracionUseCase casoDeUso;

    public AdminRestController(AdministracionUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public record Valor(String valor) {
    }

    public record Movimiento(String accion, String usuario, LocalDate fecha, LocalTime hora) {
    }

    public record Error(String servicio, String mensaje, LocalDateTime fechaHora) {
    }

    // PUT /api/admin/parametros/{clave}: crea el parámetro o cambia su valor
    @PutMapping("/parametros/{clave}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Map<String, String> parametrizar(@PathVariable String clave, @RequestBody(required = false) Valor v) {
        casoDeUso.parametrizar(clave, v == null ? null : v.valor());
        return Map.of("mensaje", "Parámetro actualizado.");
    }

    // GET /api/admin/parametros (DEC-85)
    @GetMapping("/parametros")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Map<String, String> parametros() {
        return casoDeUso.parametros();
    }

    // GET /api/admin/parametros/tema: cualquier usuario con sesión (DEC-86)
    @GetMapping("/parametros/tema")
    public Map<String, String> tema() {
        return casoDeUso.tema();
    }

    // GET /api/admin/auditoria?desde=2026-11-01&hasta=2026-11-17 (PD-07: el rango es obligatorio; lo valida el núcleo)
    @GetMapping("/auditoria")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Movimiento> auditoria(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return casoDeUso.auditoria(desde, hasta).stream()
                .map(r -> new Movimiento(r.accion(), r.usuario(), r.fecha(), r.hora().truncatedTo(ChronoUnit.SECONDS)))
                .toList();
    }

    // GET /api/admin/logs?servicio=ms-pedidos (el servicio es opcional)
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Error> logs(@RequestParam(required = false) String servicio) {
        return casoDeUso.logs(servicio).stream()
                .map(e -> new Error(e.servicio(), e.mensaje(), e.fechaHora().truncatedTo(ChronoUnit.SECONDS)))
                .toList();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> fechaInvalida(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", "La fecha no es válida."));
    }

    /** Elasticsearch no respondió: el error queda en el registro del servicio y la ventana muestra el mensaje. */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, String>> sinElasticsearch(RestClientException e) {
        LOG.error("No se pudo consultar Elasticsearch", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("mensaje", "No se pudieron consultar los logs de errores. Intente de nuevo en unos minutos."));
    }

}
