package co.edu.konradlorenz.analitica.adaptadores.entrada;

import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;
import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;

import java.time.LocalDate;
import java.util.List;

/**
 * «REST · /api/bam/promociones (Jefe de cocina)»
 *
 * En el proyecto real: @RestController.
 *
 * Nota del diagrama: Promociones (HU-19): detectarTendencias() guarda cada sugerencia con producto,
 * motivo y vigencia, y publica tendencia.detectada solo si hoy no se notificó (notificadaEl). El jefe
 * de cocina consulta el histórico y acepta o descarta. Si el análisis falla, TendenciasJob deja el
 * error en el log y el resto sigue.
 */
public class PromocionRestController {

    private final TableroUseCase casoDeUso;

    public PromocionRestController(TableroUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    // GET /api/bam/promociones?desde&hasta (sin rango, todo el histórico)
    public List<SugerenciaPromocion> sugerencias(LocalDate desde, LocalDate hasta) {
        return casoDeUso.sugerencias(desde, hasta);
    }

    // PATCH /api/bam/promociones/{id}/aceptar
    public void aceptar(Long id) {
        casoDeUso.aceptarSugerencia(id);
    }

    // PATCH /api/bam/promociones/{id}/descartar
    public void descartar(Long id) {
        casoDeUso.descartarSugerencia(id);
    }

}
