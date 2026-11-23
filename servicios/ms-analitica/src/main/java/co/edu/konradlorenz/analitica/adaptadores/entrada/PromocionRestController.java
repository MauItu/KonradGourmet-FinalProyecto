package co.edu.konradlorenz.analitica.adaptadores.entrada;

import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;
import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * «REST · /api/bam/promociones (Jefe de cocina)»
 *
 * Nota del diagrama: Promociones (HU-19): detectarTendencias() guarda cada sugerencia con producto,
 * motivo y vigencia, y publica tendencia.detectada solo si hoy no se notificó (notificadaEl). El jefe
 * de cocina consulta el histórico y acepta o descarta. Si el análisis falla, TendenciasJob deja el
 * error en el log y el resto sigue.
 *
 * Una sugerencia que no existe responde 404; una que ya fue aceptada o descartada, 409 (ManejoErrores).
 */
@RestController
@RequestMapping("/api/bam/promociones")
@PreAuthorize("hasRole('JEFE_COCINA')")
public class PromocionRestController {

    private final TableroUseCase casoDeUso;

    public PromocionRestController(TableroUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public record Sugerencia(Long id, String producto, String motivo, LocalDate vigenciaDesde, LocalDate vigenciaHasta, String estado) {
    }

    // GET /api/bam/promociones?desde&hasta (sin rango, todo el histórico)
    @GetMapping
    public List<Sugerencia> sugerencias(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return casoDeUso.sugerencias(desde, hasta).stream().map(PromocionRestController::json).toList();
    }

    // PATCH /api/bam/promociones/{id}/aceptar
    @PatchMapping("/{id}/aceptar")
    @Transactional // el cambio y sus eventos en la outbox, juntos (DEC-104)
    public ResponseEntity<Map<String, String>> aceptar(@PathVariable Long id) {
        return resolver(() -> casoDeUso.aceptarSugerencia(id), "Sugerencia aceptada.");
    }

    // PATCH /api/bam/promociones/{id}/descartar
    @PatchMapping("/{id}/descartar")
    @Transactional // el cambio y sus eventos en la outbox, juntos (DEC-104)
    public ResponseEntity<Map<String, String>> descartar(@PathVariable Long id) {
        return resolver(() -> casoDeUso.descartarSugerencia(id), "Sugerencia descartada.");
    }

    private static ResponseEntity<Map<String, String>> resolver(Runnable accion, String mensaje) {
        try {
            accion.run();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("mensaje", mensaje));
    }

    private static Sugerencia json(SugerenciaPromocion s) {
        return new Sugerencia(s.id(), s.producto(), s.motivo(), s.vigenciaDesde(), s.vigenciaHasta(), s.estado().name());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", "id".equals(e.getName())
                ? "La sugerencia no es válida." : "La fecha no es válida."));
    }

}
