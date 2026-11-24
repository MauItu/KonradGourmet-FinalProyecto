package co.edu.konradlorenz.analitica.adaptadores.entrada;

import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;
import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionValidadaEvent;
import co.edu.konradlorenz.inventario.eventos.StockBajoEvent;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import co.edu.konradlorenz.pedidos.eventos.PedidoRegistradoEvent;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudRegistradaEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;

/**
 * «Listener · 5 tópicos»
 *
 * En el proyecto real: @Component, y alRecibir() lleva @KafkaListener con sus tópicos.
 *
 * Arma el almacén propio de ms-analitica con los eventos de negocio, sin leer las bases de otros servicios. Cada
 * evento deja uno o más hechos con la fecha en que ocurrió:
 * • pedido.registrado → DEMANDA_PRODUCTO por plato (unidades) y DEMANDA_SUCURSAL (unidades del pedido);
 * • cotizacion.validada → PRECIO_PROVEEDOR (diferencia contra el precio estándar), solo si quedó OPCIONADA (PD-03);
 * • inventario.stock-bajo → STOCK_BAJO (porcentaje que quedó del insumo, por sucursal);
 * • solicitud.registrada → SOLICITUD_SUCURSAL (una solicitud);
 * • orden.generada → ORDEN_PROVEEDOR (total de la orden).
 * Los tres KPI de HU-18 leen los tres primeros; los demás quedan en el almacén para otros indicadores.
 * El evento solo trae el id del plato y de la sucursal: los nombres los da una copia local (DEC-90); sin ella, la
 * versión en memoria dice «Plato N» y «Sucursal N».
 */
public class NegocioEventsListener implements EventListener<EventoDominio> {

    private final TableroUseCase casoDeUso;
    private final Function<Long, String> nombrePlato;
    private final Function<Long, String> nombreSucursal;

    public NegocioEventsListener(TableroUseCase casoDeUso) {
        this(casoDeUso, id -> "Plato " + id, id -> "Sucursal " + id);
    }

    public NegocioEventsListener(TableroUseCase casoDeUso, Function<Long, String> nombrePlato, Function<Long, String> nombreSucursal) {
        this.casoDeUso = casoDeUso;
        this.nombrePlato = nombrePlato;
        this.nombreSucursal = nombreSucursal;
    }

    @Override
    public void alRecibir(EventoDominio evento) {
        LocalDate fecha = evento.ocurridoEn().toLocalDate();
        if (evento instanceof PedidoRegistradoEvent e) {
            BigDecimal unidades = BigDecimal.ZERO;
            for (Map.Entry<Long, Integer> linea : e.lineas().entrySet()) {
                BigDecimal cantidad = BigDecimal.valueOf(linea.getValue());
                casoDeUso.registrarHecho(new HechoNegocio("DEMANDA_PRODUCTO", e.sucursalId(), nombrePlato.apply(linea.getKey()), cantidad, fecha));
                unidades = unidades.add(cantidad);
            }
            casoDeUso.registrarHecho(new HechoNegocio("DEMANDA_SUCURSAL", e.sucursalId(), nombreSucursal.apply(e.sucursalId()), unidades, fecha));
        } else if (evento instanceof CotizacionValidadaEvent e) {
            // las compras son centrales: el hecho no tiene sucursal; RECHAZADA y SOSPECHOSA no cuentan (PD-03)
            if ("OPCIONADA".equals(e.estado()) && e.diferenciaPorcentual() != null) {
                String proveedor = e.proveedor() != null ? e.proveedor() : "Proveedor " + e.proveedorId();
                casoDeUso.registrarHecho(new HechoNegocio("PRECIO_PROVEEDOR", null, proveedor, e.diferenciaPorcentual(), fecha));
            }
        } else if (evento instanceof StockBajoEvent e) {
            casoDeUso.registrarHecho(new HechoNegocio("STOCK_BAJO", e.sucursalId(), e.nombreProducto(), e.porcentaje(), fecha));
        } else if (evento instanceof SolicitudRegistradaEvent e) {
            casoDeUso.registrarHecho(new HechoNegocio("SOLICITUD_SUCURSAL", e.sucursalId(), nombreSucursal.apply(e.sucursalId()), BigDecimal.ONE, fecha));
        } else if (evento instanceof OrdenGeneradaEvent e) {
            casoDeUso.registrarHecho(new HechoNegocio("ORDEN_PROVEEDOR", null, e.proveedor(), e.total(), fecha));
        }
    }

}
