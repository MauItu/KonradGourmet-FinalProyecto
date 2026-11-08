package co.edu.konradlorenz.pedidos.adaptadores.entrada;

import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;
import co.edu.konradlorenz.pedidos.dominio.PlatoCatalogo;
import co.edu.konradlorenz.pedidos.puertos.entrada.RegistrarPedidoUseCase;

import java.util.List;

/**
 * «REST · /api/pedidos · GET …/catalogo · GET …/mesas»
 *
 * En el proyecto real: @RestController.
 */
public class PedidoRestController {

    private final RegistrarPedidoUseCase casoDeUso;

    public PedidoRestController(RegistrarPedidoUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    // Atiende la ventana V03 de los mockups
    // Atiende la ventana V04 de los mockups
    // POST /api/pedidos → 201 Created
    public Pedido registrar(Pedido p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V02 de los mockups
    // GET /api/pedidos?turno=actual (el mesero sale del token)
    public List<Pedido> pedidosDelTurno(Long meseroId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V03 de los mockups
    // GET /api/pedidos/catalogo
    public List<PlatoCatalogo> catalogo(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V03 de los mockups
    // GET /api/pedidos/mesas
    public List<Mesa> mesas(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
