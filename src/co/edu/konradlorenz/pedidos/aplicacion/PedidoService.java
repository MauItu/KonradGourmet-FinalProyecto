package co.edu.konradlorenz.pedidos.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;
import co.edu.konradlorenz.pedidos.dominio.PlatoCatalogo;
import co.edu.konradlorenz.pedidos.puertos.entrada.RegistrarPedidoUseCase;
import co.edu.konradlorenz.pedidos.puertos.salida.CatalogoRepositoryPort;
import co.edu.konradlorenz.pedidos.puertos.salida.PedidoRepositoryPort;

import java.util.List;

public class PedidoService implements RegistrarPedidoUseCase {

    private final PedidoRepositoryPort repositorio;
    private final CatalogoRepositoryPort catalogo;
    private final EventPublisherPort eventos;

    public PedidoService(PedidoRepositoryPort repositorio, CatalogoRepositoryPort catalogo, EventPublisherPort eventos) {
        this.repositorio = repositorio;
        this.catalogo = catalogo;
        this.eventos = eventos;
    }

    @Override
    public Pedido registrar(Pedido p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Pedido> pedidosDelTurno(Long meseroId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<PlatoCatalogo> catalogo(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Mesa> mesas(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void actualizarCatalogo(PlatoCatalogo p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
