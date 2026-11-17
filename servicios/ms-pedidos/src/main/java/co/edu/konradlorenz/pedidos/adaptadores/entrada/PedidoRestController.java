package co.edu.konradlorenz.pedidos.adaptadores.entrada;

import co.edu.konradlorenz.compartido.infraestructura.Sesion;
import co.edu.konradlorenz.pedidos.dominio.DetallePedido;
import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;
import co.edu.konradlorenz.pedidos.dominio.PlatoCatalogo;
import co.edu.konradlorenz.pedidos.puertos.entrada.RegistrarPedidoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * «REST · /api/pedidos · GET …/catalogo · GET …/mesas»
 *
 * El mesero y la sucursal salen del JWT (HU-01, DEC-51): el pedido queda a nombre de quien lo tomó y
 * nadie puede registrarlo por otro. Solo el rol Mesero usa estas rutas.
 */
@RestController
@RequestMapping("/api/pedidos")
@PreAuthorize("hasRole('MESERO')")
public class PedidoRestController {

    private final RegistrarPedidoUseCase casoDeUso;

    public PedidoRestController(RegistrarPedidoUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    /** V03: la mesa y una línea por plato, con el precio que el mesero vio en la carta. */
    public record NuevoPedido(Integer mesa, List<Linea> detalles) {
    }

    public record Linea(Long platoId, int cantidad, String nota, BigDecimal precioUnitario) {
    }

    public record PedidoJson(String numero, int mesa, Long meseroId, LocalDateTime fecha, String estado, BigDecimal total,
                             int platos, List<DetalleJson> detalles) {
    }

    public record DetalleJson(Long platoId, String plato, int cantidad, String nota, BigDecimal precioUnitario, BigDecimal subtotal) {
    }

    public record PlatoJson(Long platoId, String nombre, String categoria, BigDecimal precio) {
    }

    public record MesaJson(int numero, int capacidad) {
    }

    // Atiende la ventana V03 de los mockups
    // Atiende la ventana V04 de los mockups
    // POST /api/pedidos → 201 Created
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoJson registrar(@RequestBody NuevoPedido datos, @AuthenticationPrincipal Jwt jwt) {
        Long sucursal = Sesion.sucursalId(jwt);
        if (datos.mesa() == null) {
            throw new IllegalArgumentException("Debe escoger una mesa activa");
        }
        // una mesa que no existe o no está activa llega inactiva y Pedido.validar() la rechaza
        Mesa mesa = casoDeUso.mesas(sucursal).stream().filter(m -> m.numero() == datos.mesa()).findFirst()
                .orElse(new Mesa(datos.mesa(), sucursal, 0, false));
        Pedido p = new Pedido(null, mesa, Sesion.usuarioId(jwt));
        if (datos.detalles() != null) {
            datos.detalles().forEach(l -> p.agregarPlato(l.platoId(), l.cantidad(), l.nota() == null ? null : l.nota().trim(), l.precioUnitario()));
        }
        return json(casoDeUso.registrar(p), nombres(sucursal));
    }

    // Atiende la ventana V02 de los mockups
    // GET /api/pedidos?turno=actual (el mesero sale del token)
    @GetMapping
    public List<PedidoJson> pedidosDelTurno(@RequestParam(defaultValue = "actual") String turno, @AuthenticationPrincipal Jwt jwt) {
        if (!"actual".equals(turno)) {
            throw new IllegalArgumentException("Solo se consulta el turno actual");
        }
        Map<Long, String> nombres = nombres(Sesion.sucursalId(jwt));
        return casoDeUso.pedidosDelTurno(Sesion.usuarioId(jwt)).stream().map(p -> json(p, nombres)).toList();
    }

    // Atiende la ventana V03 de los mockups
    // GET /api/pedidos/catalogo
    @GetMapping("/catalogo")
    public List<PlatoJson> catalogo(@AuthenticationPrincipal Jwt jwt) {
        return casoDeUso.catalogo(Sesion.sucursalId(jwt)).stream()
                .map(c -> new PlatoJson(c.platoId(), c.nombre(), c.categoria(), c.precio())).toList();
    }

    // Atiende la ventana V03 de los mockups
    // GET /api/pedidos/mesas
    @GetMapping("/mesas")
    public List<MesaJson> mesas(@AuthenticationPrincipal Jwt jwt) {
        return casoDeUso.mesas(Sesion.sucursalId(jwt)).stream().map(m -> new MesaJson(m.numero(), m.capacidad())).toList();
    }

    /** Los nombres de los platos del catálogo vigente; uno que ya salió del menú se muestra por su id. */
    private Map<Long, String> nombres(Long sucursal) {
        return casoDeUso.catalogo(sucursal).stream().collect(Collectors.toMap(PlatoCatalogo::platoId, PlatoCatalogo::nombre));
    }

    private static PedidoJson json(Pedido p, Map<Long, String> nombres) {
        Function<DetallePedido, DetalleJson> detalle = d -> new DetalleJson(d.platoId(),
                nombres.getOrDefault(d.platoId(), "Plato " + d.platoId()), d.cantidad(), d.nota(), d.precioUnitario(), d.subtotal());
        return new PedidoJson(p.numero(), p.mesa().numero(), p.meseroId(), p.fecha(), p.estado().name(), p.total(),
                p.detalles().stream().mapToInt(DetallePedido::cantidad).sum(), p.detalles().stream().map(detalle).toList());
    }

}
