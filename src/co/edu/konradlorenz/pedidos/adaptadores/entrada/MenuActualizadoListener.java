package co.edu.konradlorenz.pedidos.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.pedidos.puertos.entrada.RegistrarPedidoUseCase;

/**
 * «Listener · menu.actualizado»
 */
public class MenuActualizadoListener implements EventListener<EventoDominio> {

    private final RegistrarPedidoUseCase casoDeUso;

    public MenuActualizadoListener(RegistrarPedidoUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    public void alRecibir(EventoDominio evento) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
