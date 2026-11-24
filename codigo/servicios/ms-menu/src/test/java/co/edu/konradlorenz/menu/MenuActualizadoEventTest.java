package co.edu.konradlorenz.menu;

import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.menu.eventos.MenuActualizadoEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** menu.actualizado llega a ms-pedidos y ms-inventario igual a como salió de ms-menu. */
class MenuActualizadoEventTest {

    private final FormatoEventos formato = new FormatoEventos();

    @Test
    void idaYVuelta() {
        Map<Long, BigDecimal> receta = new LinkedHashMap<>();
        receta.put(1L, new BigDecimal("0.5"));
        receta.put(2L, new BigDecimal("0.1"));
        MenuActualizadoEvent original = new MenuActualizadoEvent("carlos.mendoza@konradgourmet.com", 1L, 1L, "Bandeja paisa",
                "Fuertes", new BigDecimal("32000"), false, receta);

        MenuActualizadoEvent leido = formato.deJson(formato.aJson(original), MenuActualizadoEvent.class);

        assertEquals(original.id(), leido.id());
        assertEquals(original.correlacionId(), leido.correlacionId());
        assertEquals(original.ocurridoEn(), leido.ocurridoEn());
        assertEquals(original.usuario(), leido.usuario());
        assertEquals(1L, leido.platoId());
        assertEquals(1L, leido.sucursalId());
        assertEquals("Bandeja paisa", leido.nombre());
        assertEquals("Fuertes", leido.categoria());
        assertEquals(0, new BigDecimal("32000").compareTo(leido.precioVenta()));
        assertFalse(leido.activo());
        assertEquals(receta, leido.receta());
        assertEquals(Long.class, leido.receta().keySet().iterator().next().getClass());
        assertEquals("1", formato.clave(leido), "la clave de Kafka es el plato");
    }

}
