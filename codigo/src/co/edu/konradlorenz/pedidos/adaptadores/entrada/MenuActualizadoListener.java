package co.edu.konradlorenz.pedidos.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.menu.eventos.MenuActualizadoEvent;
import co.edu.konradlorenz.pedidos.dominio.PlatoCatalogo;
import co.edu.konradlorenz.pedidos.puertos.entrada.RegistrarPedidoUseCase;

/**
 * «Listener · menu.actualizado»
 *
 * En el proyecto real: @Component, y alRecibir() lleva @KafkaListener con sus tópicos.
 */
public class MenuActualizadoListener implements EventListener<EventoDominio> {

    private final RegistrarPedidoUseCase casoDeUso;

    public MenuActualizadoListener(RegistrarPedidoUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    public void alRecibir(EventoDominio evento) {
        if (evento instanceof MenuActualizadoEvent menu) {
            casoDeUso.actualizarCatalogo(new PlatoCatalogo(menu.platoId(), menu.sucursalId(), menu.nombre(), menu.precioVenta(), menu.activo()));
        }
    }

}
