package co.edu.konradlorenz.pedidos.puertos.salida;

import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;

import java.util.List;

public interface PedidoRepositoryPort {

    Pedido guardar(Pedido p);

    List<Pedido> delTurno(Long meseroId);

    List<Mesa> mesasDe(Long sucursalId);

}
