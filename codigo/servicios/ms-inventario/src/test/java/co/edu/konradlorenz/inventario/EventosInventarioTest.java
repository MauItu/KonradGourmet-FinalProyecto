package co.edu.konradlorenz.inventario;

import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.inventario.eventos.StockBajoEvent;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudRegistradaEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Los eventos que publica (inventario.stock-bajo) y escucha (solicitud.registrada) ms-inventario llegan iguales. */
class EventosInventarioTest {

    private final FormatoEventos formato = new FormatoEventos();

    @Test
    void stockBajo() {
        StockBajoEvent original = new StockBajoEvent("sistema", 1L, 1L, "Res", new BigDecimal("11.5"), "kg", new BigDecimal("23.00"));
        StockBajoEvent leido = formato.deJson(formato.aJson(original), StockBajoEvent.class);
        assertEquals(original.id(), leido.id());
        assertEquals(original.correlacionId(), leido.correlacionId());
        assertEquals(original.ocurridoEn(), leido.ocurridoEn());
        assertEquals("sistema", leido.usuario());
        assertEquals(1L, leido.sucursalId());
        assertEquals(1L, leido.productoId());
        assertEquals("Res", leido.nombreProducto());
        assertEquals(new BigDecimal("11.5"), leido.stockActual());
        assertEquals("kg", leido.unidad());
        assertEquals(new BigDecimal("23.00"), leido.porcentaje());
        assertEquals("1", formato.clave(leido), "la clave de Kafka es el producto");
    }

    @Test
    void solicitudRegistrada() {
        SolicitudRegistradaEvent original = new SolicitudRegistradaEvent("carlos.mendoza@konradgourmet.com", "SOL-031", 1L, 2L,
                "carlos.mendoza@konradgourmet.com", List.of(1L, 2L));
        SolicitudRegistradaEvent leido = formato.deJson(formato.aJson(original), SolicitudRegistradaEvent.class);
        assertEquals(original.id(), leido.id());
        assertEquals(original.correlacionId(), leido.correlacionId());
        assertEquals("SOL-031", leido.numero());
        assertEquals(1L, leido.sucursalId());
        assertEquals(List.of(1L, 2L), leido.productoIds());
    }

}
