package co.edu.konradlorenz.pruebas;

import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ItemCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.demo.Ensamble;
import co.edu.konradlorenz.inventario.dominio.InsumoInventario;
import co.edu.konradlorenz.inventario.dominio.Inventario;
import co.edu.konradlorenz.menu.eventos.MenuActualizadoEvent;
import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;
import co.edu.konradlorenz.solicitudes.dominio.ItemSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Arma un sistema completo en memoria con los datos de partida de la demo. */
final class Datos {

    private Datos() {
    }

    static Ensamble sistema() {
        Ensamble s = new Ensamble(11);
        s.destinatariosRest.registrar(1L, "jefe.centro@konradgourmet.com");
        s.repositorioPedidos.agregarMesa(new Mesa(5, 1L, 4, true));
        s.ministerio.definirPrecio("1", 20000);
        s.ministerio.definirPrecio("2", 8000);
        s.repositorioInventario.guardar(new Inventario(1L, List.of(
                new InsumoInventario(1L, "Res", new BigDecimal("12.5"), new BigDecimal("50"), "kg"),
                new InsumoInventario(2L, "Arroz", new BigDecimal("40"), new BigDecimal("60"), "kg"),
                new InsumoInventario(3L, "Papa", new BigDecimal("30"), new BigDecimal("40"), "kg"),
                new InsumoInventario(4L, "Pollo", new BigDecimal("25"), new BigDecimal("40"), "kg"))));
        s.bus.publicar(new MenuActualizadoEvent("test", 1L, 1L, "Bandeja paisa", new BigDecimal("32000"), true, receta(1L, "0.5", 2L, "0.1")));
        s.bus.publicar(new MenuActualizadoEvent("test", 2L, 1L, "Ajiaco", new BigDecimal("18000"), true, receta(4L, "0.2", 3L, "0.3")));
        s.proveedores.registrar(new Proveedor(1L, "Proveedor ABC", "900123456-1", "ventas@proveedorabc.com", "601", "Cra 7", List.of()));
        s.proveedores.registrar(new Proveedor(2L, "Proveedor B", "900234567-2", "ventas@proveedorb.com", "601", "Cll 26", List.of()));
        return s;
    }

    static Map<Long, BigDecimal> receta(Long insumo1, String cantidad1, Long insumo2, String cantidad2) {
        Map<Long, BigDecimal> receta = new LinkedHashMap<>();
        receta.put(insumo1, new BigDecimal(cantidad1));
        receta.put(insumo2, new BigDecimal(cantidad2));
        return receta;
    }

    /** PED-0251: 2 bandejas paisas y 1 ajiaco, $82.000. */
    static Pedido pedido() {
        Pedido p = new Pedido("PED-0251", new Mesa(5, 1L, 4, true), 3L);
        p.agregarPlato(1L, 2, null, new BigDecimal("32000"));
        p.agregarPlato(2L, 1, null, new BigDecimal("18000"));
        return p;
    }

    /** SOL-031: res 20 kg y arroz 15 kg. */
    static SolicitudAlimentos solicitud() {
        SolicitudAlimentos s = new SolicitudAlimentos("SOL-031", 1L, 7L, "carlos.mendoza@konradgourmet.com");
        s.agregarItem(new ItemSolicitud(1L, new BigDecimal("20"), "Marca X", "kg"));
        s.agregarItem(new ItemSolicitud(2L, new BigDecimal("15"), "Marca X", "kg"));
        return s;
    }

    /** Una cotización nueva de SOL-031 para el proveedor 1 o 2, sin precios todavía. */
    static Cotizacion cotizacionNueva(Ensamble s, String numero, long proveedorId) {
        return new Cotizacion(numero, List.of("SOL-031"), s.proveedores.detalle(proveedorId), LocalDate.of(2026, 9, 30), List.of(
                new ItemCotizacion(1L, new BigDecimal("20"), "kg"),
                new ItemCotizacion(2L, new BigDecimal("15"), "kg")));
    }

    static Map<Long, BigDecimal> precios(String res, String arroz) {
        Map<Long, BigDecimal> precios = new LinkedHashMap<>();
        precios.put(1L, new BigDecimal(res));
        precios.put(2L, new BigDecimal(arroz));
        return precios;
    }

}
