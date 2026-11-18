package co.edu.konradlorenz.analitica.adaptadores.entrada;

import co.edu.konradlorenz.analitica.configuracion.EventosProcesados;
import co.edu.konradlorenz.analitica.configuracion.NombresNegocio;
import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;
import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionValidadaEvent;
import co.edu.konradlorenz.inventario.eventos.StockBajoEvent;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import co.edu.konradlorenz.pedidos.eventos.PedidoRegistradoEvent;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudRegistradaEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * «Listener · 5 tópicos»
 *
 * Arma el almacén propio de ms-analitica con los eventos de negocio, sin leer las bases de otros servicios. Cada
 * evento deja uno o más hechos con la fecha en que ocurrió, como el de src/ (PruebasAnalitica):
 * • pedido.registrado → DEMANDA_PRODUCTO por plato (unidades) y DEMANDA_SUCURSAL (unidades del pedido);
 * • cotizacion.validada → PRECIO_PROVEEDOR (diferencia contra el precio estándar), solo si quedó OPCIONADA (PD-03);
 * • inventario.stock-bajo → STOCK_BAJO; solicitud.registrada → SOLICITUD_SUCURSAL; orden.generada → ORDEN_PROVEEDOR.
 * Los nombres del plato y de la sucursal salen de NombresNegocio (DEC-90). Un evento repetido no deja hechos otra vez
 * (EventosProcesados, en la misma transacción); el recorrido del evento (InterceptorCorrelacion) llega hasta
 * KpiJpaAdapter, que guarda su correlacionId.
 */
@Component
public class NegocioEventsListener implements EventListener<EventoDominio> {

    private final TableroUseCase casoDeUso;
    private final NombresNegocio nombres;
    private final EventosProcesados procesados;

    public NegocioEventsListener(TableroUseCase casoDeUso, NombresNegocio nombres, EventosProcesados procesados) {
        this.casoDeUso = casoDeUso;
        this.nombres = nombres;
        this.procesados = procesados;
    }

    @Override
    @Transactional
    @KafkaListener(topics = {"${konrad.kafka.prefijo-topico:}pedido.registrado", "${konrad.kafka.prefijo-topico:}cotizacion.validada",
            "${konrad.kafka.prefijo-topico:}inventario.stock-bajo", "${konrad.kafka.prefijo-topico:}solicitud.registrada",
            "${konrad.kafka.prefijo-topico:}orden.generada"})
    public void alRecibir(EventoDominio evento) {
        if (!procesados.marcar(evento)) {
            return;
        }
        LocalDate fecha = evento.ocurridoEn().toLocalDate();
        if (evento instanceof PedidoRegistradoEvent e) {
            BigDecimal unidades = BigDecimal.ZERO;
            for (Map.Entry<Long, Integer> linea : e.lineas().entrySet()) {
                BigDecimal cantidad = BigDecimal.valueOf(linea.getValue());
                casoDeUso.registrarHecho(new HechoNegocio("DEMANDA_PRODUCTO", e.sucursalId(), nombres.plato(linea.getKey()), cantidad, fecha));
                unidades = unidades.add(cantidad);
            }
            casoDeUso.registrarHecho(new HechoNegocio("DEMANDA_SUCURSAL", e.sucursalId(), nombres.sucursal(e.sucursalId()), unidades, fecha));
        } else if (evento instanceof CotizacionValidadaEvent e) {
            // las compras son centrales: el hecho no tiene sucursal; RECHAZADA y SOSPECHOSA no cuentan (PD-03)
            if ("OPCIONADA".equals(e.estado()) && e.diferenciaPorcentual() != null) {
                String proveedor = e.proveedor() != null ? e.proveedor() : "Proveedor " + e.proveedorId();
                casoDeUso.registrarHecho(new HechoNegocio("PRECIO_PROVEEDOR", null, proveedor, e.diferenciaPorcentual(), fecha));
            }
        } else if (evento instanceof StockBajoEvent e) {
            casoDeUso.registrarHecho(new HechoNegocio("STOCK_BAJO", e.sucursalId(), e.nombreProducto(), e.porcentaje(), fecha));
        } else if (evento instanceof SolicitudRegistradaEvent e) {
            casoDeUso.registrarHecho(new HechoNegocio("SOLICITUD_SUCURSAL", e.sucursalId(), nombres.sucursal(e.sucursalId()), BigDecimal.ONE, fecha));
        } else if (evento instanceof OrdenGeneradaEvent e) {
            casoDeUso.registrarHecho(new HechoNegocio("ORDEN_PROVEEDOR", null, e.proveedor(), e.total(), fecha));
        }
    }

}
