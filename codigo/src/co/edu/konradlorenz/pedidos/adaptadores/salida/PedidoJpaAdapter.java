package co.edu.konradlorenz.pedidos.adaptadores.salida;

import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;
import co.edu.konradlorenz.pedidos.puertos.salida.PedidoRepositoryPort;

import java.util.List;

/**
 * En el proyecto real: @Repository.
 */
public class PedidoJpaAdapter implements PedidoRepositoryPort {

    @Override
    public Pedido guardar(Pedido p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Pedido> delTurno(Long meseroId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Mesa> mesasDe(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
