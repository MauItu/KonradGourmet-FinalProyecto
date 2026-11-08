package co.edu.konradlorenz.pedidos.puertos.entrada;

import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;
import co.edu.konradlorenz.pedidos.dominio.PlatoCatalogo;

import java.util.List;

public interface RegistrarPedidoUseCase {

    Pedido registrar(Pedido p);

    List<Pedido> pedidosDelTurno(Long meseroId);

    List<PlatoCatalogo> catalogo(Long sucursalId);

    List<Mesa> mesas(Long sucursalId);

    void actualizarCatalogo(PlatoCatalogo p);

}
