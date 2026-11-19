package co.edu.konradlorenz.inventario.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;
import co.edu.konradlorenz.pedidos.eventos.PedidoRegistradoEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * «Listener · pedido.registrado»
 *
 * Multiplica las líneas del pedido por la copia local de cada receta y suma el consumo de la sucursal;
 * el stock se descuenta en el cierre diario (ConsumoDiarioJob).
 */
@Component
public class PedidoRegistradoListener implements EventListener<EventoDominio> {

    private final GestionarInventarioUseCase casoDeUso;

    public PedidoRegistradoListener(GestionarInventarioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    @KafkaListener(topics = "${konrad.kafka.prefijo-topico:}pedido.registrado")
    public void alRecibir(EventoDominio evento) {
        if (evento instanceof PedidoRegistradoEvent pedido) {
            casoDeUso.acumularConsumo(pedido.sucursalId(), pedido.lineas());
        }
    }

}
