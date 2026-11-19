package co.edu.konradlorenz.pedidos.adaptadores.salida;

import co.edu.konradlorenz.pedidos.dominio.DetallePedido;
import co.edu.konradlorenz.pedidos.dominio.EstadoPedido;
import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;
import co.edu.konradlorenz.pedidos.puertos.salida.PedidoRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Guarda los pedidos en ms_pedidos (PostgreSQL). Un pedido que llega sin número recibe el siguiente de
 * la secuencia (PED-0251, PED-0252…) y se devuelve con él. El turno del mesero es el día de hoy.
 */
@Repository
public class PedidoJpaAdapter implements PedidoRepositoryPort {

    private final PedidoJpaRepository pedidos;
    private final MesaJpaRepository mesas;

    public PedidoJpaAdapter(PedidoJpaRepository pedidos, MesaJpaRepository mesas) {
        this.pedidos = pedidos;
        this.mesas = mesas;
    }

    @Override
    @Transactional
    public Pedido guardar(Pedido p) {
        Pedido conNumero = p;
        if (p.numero() == null) {
            conNumero = new Pedido(String.format("PED-%04d", pedidos.siguienteNumero()), p.mesa(), p.meseroId(), p.fecha(), p.estado());
            for (DetallePedido d : p.detalles()) {
                conNumero.agregarPlato(d.platoId(), d.cantidad(), d.nota(), d.precioUnitario());
            }
        }
        PedidoEntidad e = new PedidoEntidad();
        e.numero = conNumero.numero();
        e.sucursalId = conNumero.mesa().sucursalId();
        e.mesaNumero = conNumero.mesa().numero();
        e.meseroId = conNumero.meseroId();
        e.fecha = conNumero.fecha();
        e.estado = conNumero.estado().name();
        for (DetallePedido d : conNumero.detalles()) {
            e.detalles.add(new PedidoEntidad.Detalle(d.platoId(), d.cantidad(), d.nota(), d.precioUnitario()));
        }
        pedidos.save(e);
        return conNumero;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> delTurno(Long meseroId) {
        List<PedidoEntidad> filas = pedidos.findByMeseroIdAndFechaGreaterThanEqualOrderByFechaDesc(meseroId, LocalDate.now().atStartOfDay());
        Map<String, Mesa> todas = filas.stream().map(f -> f.sucursalId).distinct()
                .flatMap(s -> mesas.findBySucursalId(s).stream())
                .collect(Collectors.toMap(m -> m.sucursalId + "-" + m.numero, PedidoJpaAdapter::mesa));
        return filas.stream().map(f -> {
            Pedido p = new Pedido(f.numero, todas.get(f.sucursalId + "-" + f.mesaNumero), f.meseroId, f.fecha, EstadoPedido.valueOf(f.estado));
            f.detalles.forEach(d -> p.agregarPlato(d.platoId, d.cantidad, d.nota, d.precioUnitario));
            return p;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mesa> mesasDe(Long sucursalId) {
        return mesas.findBySucursalIdAndActivaTrueOrderByNumero(sucursalId).stream().map(PedidoJpaAdapter::mesa).toList();
    }

    private static Mesa mesa(MesaEntidad m) {
        return new Mesa(m.numero, m.sucursalId, m.capacidad, m.activa);
    }

}
