package co.edu.konradlorenz.analitica.adaptadores.entrada;

import co.edu.konradlorenz.analitica.dominio.ResultadoKpi;
import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * «REST · /api/bam (Director comercial)»
 *
 * Nota del diagrama: kpis() filtra por rango de fechas y sucursal (HU-18). Si no hay hechos
 * suficientes, el tablero muestra «No hay información suficiente…». Cada indicador sale con su valor, el ranking
 * para la gráfica y el periodo que se aplicó (DEC-88).
 */
@RestController
@RequestMapping("/api/bam")
@PreAuthorize("hasRole('DIRECTOR_COMERCIAL')")
public class TableroRestController {

    private final TableroUseCase casoDeUso;

    public TableroRestController(TableroUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public record Puesto(String clave, BigDecimal valor) {
    }

    public record Kpi(String kpi, String ganador, BigDecimal valor, List<Puesto> ranking, LocalDate desde, LocalDate hasta) {
    }

    // GET /api/bam/kpis?desde&hasta&sucursalId (los tres son opcionales; el rango va completo, PD-03)
    @GetMapping("/kpis")
    public List<Kpi> kpis(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                          @RequestParam(required = false) Long sucursalId) {
        return casoDeUso.kpis(desde, hasta, sucursalId).stream().map(TableroRestController::json).toList();
    }

    private static Kpi json(ResultadoKpi r) {
        return new Kpi(r.kpi(), r.ganador(), r.ganador() == null ? null : r.valor(),
                r.ranking().entrySet().stream().map(e -> new Puesto(e.getKey(), e.getValue())).toList(), r.desde(), r.hasta());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", "sucursalId".equals(e.getName())
                ? "La sucursal no es válida." : "La fecha no es válida."));
    }

}
