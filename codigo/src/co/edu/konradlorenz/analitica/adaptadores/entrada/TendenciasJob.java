package co.edu.konradlorenz.analitica.adaptadores.entrada;

import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;

/**
 * «Scheduler · semanal»
 *
 * En el proyecto real: @Component, y ejecutar() lleva @Scheduled (semanal).
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
