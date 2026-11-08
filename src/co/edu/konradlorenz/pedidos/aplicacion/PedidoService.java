package co.edu.konradlorenz.pedidos.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.pedidos.dominio.CatalogoPlatos;
import co.edu.konradlorenz.pedidos.dominio.DetallePedido;
import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;
import co.edu.konradlorenz.pedidos.dominio.PlatoCatalogo;
import co.edu.konradlorenz.pedidos.eventos.PedidoRegistradoEvent;
import co.edu.konradlorenz.pedidos.puertos.entrada.RegistrarPedidoUseCase;
import co.edu.konradlorenz.pedidos.puertos.salida.CatalogoRepositoryPort;
import co.edu.konradlorenz.pedidos.puertos.salida.PedidoRepositoryPort;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        p.validar();
        Long sucursalId = p.mesa().sucursalId();
        CatalogoPlatos vigente = catalogo.catalogo(sucursalId);
        Map<Long, Integer> lineas = new LinkedHashMap<>();
        for (DetallePedido d : p.detalles()) {
            // el precio de cada plato tiene que ser el del catálogo de la sucursal
            BigDecimal precio = vigente.precioDe(d.platoId());
            if (precio.compareTo(d.precioUnitario()) != 0) {
                throw new IllegalArgumentException("El precio del plato " + d.platoId() + " no coincide con el catálogo");
            }
            lineas.merge(d.platoId(), d.cantidad(), Integer::sum);
        }
        Pedido guardado = repositorio.guardar(p);
        eventos.publicar(new PedidoRegistradoEvent("sistema", guardado.numero(), sucursalId, lineas));
        return guardado;
    }

    @Override
    public List<Pedido> pedidosDelTurno(Long meseroId) {
        return repositorio.delTurno(meseroId);
    }

    @Override
    public List<PlatoCatalogo> catalogo(Long sucursalId) {
        return catalogo.catalogo(sucursalId).vigentes();
    }

    @Override
    public List<Mesa> mesas(Long sucursalId) {
        return repositorio.mesasDe(sucursalId);
    }

    @Override
    public void actualizarCatalogo(PlatoCatalogo p) {
        catalogo.guardar(p);
    }

}
