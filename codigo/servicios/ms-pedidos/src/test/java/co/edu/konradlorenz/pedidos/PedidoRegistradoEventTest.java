package co.edu.konradlorenz.pedidos;

import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.pedidos.eventos.PedidoRegistradoEvent;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** pedido.registrado llega a ms-inventario igual a como salió de ms-pedidos: las líneas son platoId → cantidad. */
class PedidoRegistradoEventTest {

    private final FormatoEventos formato = new FormatoEventos();

    @Test
    void idaYVuelta() {
        Map<Long, Integer> lineas = new LinkedHashMap<>();
        lineas.put(1L, 2);
        lineas.put(2L, 1);
        PedidoRegistradoEvent original = new PedidoRegistradoEvent("isabella.torres@konradgourmet.com", "PED-0251", 1L, lineas);

        PedidoRegistradoEvent leido = formato.deJson(formato.aJson(original), PedidoRegistradoEvent.class);

        assertEquals(original.id(), leido.id());
        assertEquals(original.correlacionId(), leido.correlacionId());
        assertEquals(original.ocurridoEn(), leido.ocurridoEn());
        assertEquals(original.usuario(), leido.usuario());
        assertEquals("PED-0251", leido.numero());
        assertEquals(1L, leido.sucursalId());
        assertEquals(lineas, leido.lineas());
        assertEquals(Long.class, leido.lineas().keySet().iterator().next().getClass());
        assertEquals("PED-0251", formato.clave(leido), "la clave de Kafka es el pedido");
    }

}
