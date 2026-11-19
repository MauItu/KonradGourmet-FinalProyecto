package co.edu.konradlorenz.analitica.adaptadores.entrada;

import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;

import java.lang.System.Logger.Level;

/**
 * «Scheduler · semanal»
 *
 * En el proyecto real: @Component, y ejecutar() lleva @Scheduled (semanal) y @SchedulerLock
 * (ShedLock), para que lo ejecute una sola réplica.
 *
 * Si el análisis falla (por ejemplo, las redes no responden), el error queda en el log y el resto del sistema
 * sigue (HU-19): la excepción no sale del job y la próxima ejecución lo vuelve a intentar.
 */
public class TendenciasJob {

    private static final System.Logger LOG = System.getLogger(TendenciasJob.class.getName());

    private final TableroUseCase casoDeUso;

    public TendenciasJob(TableroUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public void ejecutar() {
        try {
            casoDeUso.detectarTendencias();
        } catch (RuntimeException e) {
            LOG.log(Level.ERROR, "Falló el análisis de tendencias de promociones: " + e.getMessage(), e);
        }
    }

}
