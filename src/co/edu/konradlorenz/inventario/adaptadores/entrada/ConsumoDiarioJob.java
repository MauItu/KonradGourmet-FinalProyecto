package co.edu.konradlorenz.inventario.adaptadores.entrada;

import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;

/**
 * «Scheduler · diario 23:59»
 *
 * En el proyecto real: @Component, y ejecutar() lleva @Scheduled (diario 23:59).
 */
public class ConsumoDiarioJob {

    private final GestionarInventarioUseCase casoDeUso;

    public ConsumoDiarioJob(GestionarInventarioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public void ejecutar() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
