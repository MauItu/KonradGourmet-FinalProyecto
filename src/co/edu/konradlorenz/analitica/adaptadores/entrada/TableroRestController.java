package co.edu.konradlorenz.analitica.adaptadores.entrada;

import co.edu.konradlorenz.analitica.dominio.ResultadoKpi;
import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;

import java.time.LocalDate;
import java.util.List;

/**
 * «REST · /api/bam (Director comercial)»
 *
 * En el proyecto real: @RestController.
 *
 * Nota del diagrama: kpis() filtra por rango de fechas y sucursal (HU-18). Si no hay hechos
 * suficientes, el tablero muestra «No hay información suficiente…».
 */
public class TableroRestController {

    private final TableroUseCase casoDeUso;

    public TableroRestController(TableroUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    // GET /api/bam/kpis?desde&hasta&sucursalId
    public List<ResultadoKpi> kpis(LocalDate desde, LocalDate hasta, Long sucursalId) {
        return casoDeUso.kpis(desde, hasta, sucursalId);
    }

}
