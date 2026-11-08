package co.edu.konradlorenz.analitica.adaptadores.entrada;

import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;

/**
 * «Scheduler · semanal»
 *
 * En el proyecto real: @Component, y ejecutar() lleva @Scheduled (semanal) y @SchedulerLock
 * (ShedLock), para que lo ejecute una sola réplica.
 */
public class TendenciasJob {

    private final TableroUseCase casoDeUso;

    public TendenciasJob(TableroUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public void ejecutar() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
