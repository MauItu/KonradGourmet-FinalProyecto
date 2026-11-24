package co.edu.konradlorenz.inventario.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.infraestructura.EventosProcesados;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;
import co.edu.konradlorenz.pedidos.eventos.PedidoRegistradoEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * «Listener · pedido.registrado»
 *
 * Multiplica las líneas del pedido por la copia local de cada receta y suma el consumo de la sucursal;
 * el stock se descuenta en el cierre diario (ConsumoDiarioJob).
 */
@Component
public class PedidoRegistradoListener implements EventListener<EventoDominio> {

    private final GestionarInventarioUseCase casoDeUso;
    private final EventosProcesados procesados;

    public PedidoRegistradoListener(GestionarInventarioUseCase casoDeUso, EventosProcesados procesados) {
        this.casoDeUso = casoDeUso;
        this.procesados = procesados;
    }

    @Override
    @Transactional
    @KafkaListener(topics = "${konrad.kafka.prefijo-topico:}pedido.registrado")
    public void alRecibir(EventoDominio evento) {
        // un evento repetido se descarta; se marca en la misma transacción que el cambio (DEC-104)
        if (!procesados.marcar(evento, "PedidoRegistradoListener")) {
            return;
        }
        if (evento instanceof PedidoRegistradoEvent pedido) {
            casoDeUso.acumularConsumo(pedido.sucursalId(), pedido.lineas());
        }
    }

}
