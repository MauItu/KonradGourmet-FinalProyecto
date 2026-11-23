package co.edu.konradlorenz.inventario;

import co.edu.konradlorenz.compartido.infraestructura.EventosProcesados;
import co.edu.konradlorenz.inventario.adaptadores.entrada.PedidoRegistradoListener;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;
import co.edu.konradlorenz.pedidos.eventos.PedidoRegistradoEvent;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * C7-5 y DEC-104: Kafka entrega al menos una vez; si el mismo pedido.registrado llega dos veces (un consumidor
 * que se cayó antes de confirmar, o la outbox que lo publicó de nuevo), el consumo se suma una sola vez y el
 * cierre no descuenta el doble.
 */
class PedidoRepetidoTest {

    @Test
    void elMismoPedidoDosVecesSeSumaUnaSola() {
        // la tabla evento_procesado: el INSERT … ON CONFLICT DO NOTHING devuelve 1 la primera vez y 0 después
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Set<Object> vistos = new HashSet<>();
        when(jdbc.update(anyString(), any(), eq("PedidoRegistradoListener"), any()))
                .thenAnswer(i -> vistos.add(i.getArgument(1)) ? 1 : 0);
        GestionarInventarioUseCase casoDeUso = mock(GestionarInventarioUseCase.class);
        PedidoRegistradoListener listener = new PedidoRegistradoListener(casoDeUso, new EventosProcesados(jdbc));
        PedidoRegistradoEvent pedido = new PedidoRegistradoEvent("ana.torres@konradgourmet.com", "PED-0251", 1L, Map.of(1L, 2));

        listener.alRecibir(pedido);
        listener.alRecibir(pedido);
        listener.alRecibir(new PedidoRegistradoEvent("ana.torres@konradgourmet.com", "PED-0252", 1L, Map.of(1L, 1)));

        verify(casoDeUso, times(1)).acumularConsumo(1L, Map.of(1L, 2));
        verify(casoDeUso, times(1)).acumularConsumo(1L, Map.of(1L, 1));
    }

}
