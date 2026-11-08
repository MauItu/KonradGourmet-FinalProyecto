package co.edu.konradlorenz.inventario.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;
import co.edu.konradlorenz.pedidos.eventos.PedidoRegistradoEvent;

/**
 * «Listener · pedido.registrado»
 *
 * En el proyecto real: @Component, y alRecibir() lleva @KafkaListener con sus tópicos.
 */
public class PedidoRegistradoListener implements EventListener<EventoDominio> {

    private final GestionarInventarioUseCase casoDeUso;

    public PedidoRegistradoListener(GestionarInventarioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    public void alRecibir(EventoDominio evento) {
        if (evento instanceof PedidoRegistradoEvent pedido) {
            casoDeUso.acumularConsumo(pedido.sucursalId(), pedido.lineas());
        }
    }

}
