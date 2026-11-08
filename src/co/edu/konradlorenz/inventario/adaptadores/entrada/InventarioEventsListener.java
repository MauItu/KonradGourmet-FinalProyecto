package co.edu.konradlorenz.inventario.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;

/**
 * «Listener · menu.actualizado · solicitud.registrada»
 */
public class InventarioEventsListener implements EventListener<EventoDominio> {

    private final GestionarInventarioUseCase casoDeUso;

    public InventarioEventsListener(GestionarInventarioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    public void alRecibir(EventoDominio evento) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
