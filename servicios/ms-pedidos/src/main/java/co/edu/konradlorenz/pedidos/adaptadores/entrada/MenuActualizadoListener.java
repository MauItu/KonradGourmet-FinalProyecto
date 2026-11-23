package co.edu.konradlorenz.pedidos.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.infraestructura.EventosProcesados;
import co.edu.konradlorenz.menu.eventos.MenuActualizadoEvent;
import co.edu.konradlorenz.pedidos.dominio.PlatoCatalogo;
import co.edu.konradlorenz.pedidos.puertos.entrada.RegistrarPedidoUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * «Listener · menu.actualizado»
 *
 * Cada cambio de un plato en ms-menu reemplaza su entrada en el catálogo del mesero: con activo = false
 * sale de V03. Las réplicas de ms-pedidos comparten el grupo de consumidores, así que el evento lo
 * procesa una sola y queda en la base para todas.
 */
@Component
public class MenuActualizadoListener implements EventListener<EventoDominio> {

    private final RegistrarPedidoUseCase casoDeUso;
    private final EventosProcesados procesados;

    public MenuActualizadoListener(RegistrarPedidoUseCase casoDeUso, EventosProcesados procesados) {
        this.casoDeUso = casoDeUso;
        this.procesados = procesados;
    }

    @Override
    @Transactional
    @KafkaListener(topics = "${konrad.kafka.prefijo-topico:}menu.actualizado")
    public void alRecibir(EventoDominio evento) {
        // un evento repetido se descarta; se marca en la misma transacción que el cambio (DEC-104)
        if (!procesados.marcar(evento, "MenuActualizadoListener")) {
            return;
        }
        if (evento instanceof MenuActualizadoEvent menu) {
            casoDeUso.actualizarCatalogo(new PlatoCatalogo(menu.platoId(), menu.sucursalId(), menu.nombre(), menu.categoria(), menu.precioVenta(), menu.activo()));
        }
    }

}
