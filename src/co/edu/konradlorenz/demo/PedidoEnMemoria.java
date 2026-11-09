package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;
import co.edu.konradlorenz.pedidos.puertos.salida.PedidoRepositoryPort;

import java.util.ArrayList;
import java.util.List;

/** Los pedidos y las mesas en listas, en vez de la base de ms-pedidos. */
public class PedidoEnMemoria implements PedidoRepositoryPort {

    private final List<Pedido> pedidos = new ArrayList<>();
    private final List<Mesa> mesas = new ArrayList<>();

    public void agregarMesa(Mesa m) {
        mesas.add(m);
    }

    @Override
    public Pedido guardar(Pedido p) {
        pedidos.add(p);
        return p;
    }

    /** Los pedidos que tomó el mesero (DEC-51); la demo no separa turnos, así que son todos los suyos del día. */
    @Override
    public List<Pedido> delTurno(Long meseroId) {
        return pedidos.stream().filter(p -> p.meseroId().equals(meseroId)).toList();
    }

    @Override
    public List<Mesa> mesasDe(Long sucursalId) {
        return mesas.stream().filter(m -> m.sucursalId().equals(sucursalId) && m.activa()).toList();
    }

}
