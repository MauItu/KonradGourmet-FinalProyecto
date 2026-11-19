package co.edu.konradlorenz.inventario.adaptadores.entrada;

import co.edu.konradlorenz.compartido.infraestructura.Sesion;
import co.edu.konradlorenz.inventario.dominio.InsumoInventario;
import co.edu.konradlorenz.inventario.dominio.UmbralStock;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * «REST · /api/inventario»
 *
 * La sucursal sale del JWT. Solo el jefe de cocina consulta el inventario y sus alertas.
 */
@RestController
@RequestMapping("/api/inventario")
@PreAuthorize("hasRole('JEFE_COCINA')")
public class InventarioRestController {

    private final GestionarInventarioUseCase casoDeUso;
    private final UmbralStock umbral;

    public InventarioRestController(GestionarInventarioUseCase casoDeUso, UmbralStock umbral) {
        this.casoDeUso = casoDeUso;
        this.umbral = umbral;
    }

    /** minimo es el stock que corresponde al umbral (25 % de la referencia). */
    public record InsumoJson(Long productoId, String nombre, BigDecimal stockActual, BigDecimal stockReferencia, String unidad,
                             BigDecimal porcentaje, BigDecimal minimo, boolean bajoUmbral, boolean alertaAtendida) {
    }

    // Atiende la ventana V05 de los mockups
    // GET /api/inventario/alertas
    @GetMapping("/alertas")
    public List<InsumoJson> alertas(@AuthenticationPrincipal Jwt jwt) {
        return casoDeUso.alertas(Sesion.sucursalId(jwt)).stream().map(this::json).toList();
    }

    // Atiende la ventana V07 de los mockups
    // GET /api/inventario/insumos
    @GetMapping("/insumos")
    public List<InsumoJson> insumos(@AuthenticationPrincipal Jwt jwt) {
        return casoDeUso.insumos(Sesion.sucursalId(jwt)).stream().map(this::json).toList();
    }

    private InsumoJson json(InsumoInventario i) {
        BigDecimal minimo = i.stockReferencia().multiply(umbral.porcentaje()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new InsumoJson(i.productoId(), i.nombre(), i.stockActual(), i.stockReferencia(), i.unidad(),
                i.porcentajeExistencia(), minimo, umbral.superado(i), i.alertaAtendida());
    }

}
