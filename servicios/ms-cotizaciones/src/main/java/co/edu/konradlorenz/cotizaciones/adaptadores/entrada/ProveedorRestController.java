package co.edu.konradlorenz.cotizaciones.adaptadores.entrada;

import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarProveedorUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * «REST · /api/proveedores (Auxiliar de compras)»
 *
 * HU-15: el auxiliar de compras registra, edita e inactiva proveedores. La lista también la lee el
 * director de compras (el filtro de proveedor de V17).
 */
@RestController
@RequestMapping("/api/proveedores")
public class ProveedorRestController {

    private final GestionarProveedorUseCase casoDeUso;

    public ProveedorRestController(GestionarProveedorUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    /** Lo que se escribe en el formulario; el id y el estado no se mandan. */
    public record DatosProveedor(String razonSocial, String nit, String correo, String telefono, String direccion, List<String> categorias) {
    }

    public record ProveedorJson(Long id, String razonSocial, String nit, String correo, String telefono, String direccion,
                                List<String> categorias, boolean activo) {
    }

    // POST /api/proveedores → 201 Created
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('AUXILIAR_COMPRAS')")
    @Transactional // el cambio y sus eventos en la outbox, juntos (DEC-104)
    public ProveedorJson registrar(@RequestBody DatosProveedor datos) {
        return json(casoDeUso.registrar(proveedor(null, datos)));
    }

    // Atiende la ventana V13 de los mockups (el desplegable: ?activos=true)
    // GET /api/proveedores
    @GetMapping
    @PreAuthorize("hasAnyRole('AUXILIAR_COMPRAS', 'DIRECTOR_COMPRAS')")
    public List<ProveedorJson> consultar(@RequestParam(defaultValue = "false") boolean activos) {
        return casoDeUso.consultar(activos).stream().map(ProveedorRestController::json).toList();
    }

    // GET /api/proveedores/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUXILIAR_COMPRAS', 'DIRECTOR_COMPRAS')")
    public ProveedorJson detalle(@PathVariable Long id) {
        return json(casoDeUso.detalle(id));
    }

    // PUT /api/proveedores/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AUXILIAR_COMPRAS')")
    @Transactional // el cambio y sus eventos en la outbox, juntos (DEC-104)
    public ProveedorJson editar(@PathVariable Long id, @RequestBody DatosProveedor datos) {
        return json(casoDeUso.editar(id, proveedor(id, datos)));
    }

    // PATCH /api/proveedores/{id}/inactivar → 204
    @PatchMapping("/{id}/inactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('AUXILIAR_COMPRAS')")
    @Transactional // el cambio y sus eventos en la outbox, juntos (DEC-104)
    public void inactivar(@PathVariable Long id) {
        casoDeUso.inactivar(id);
    }

    /** Los textos llegan sin espacios de sobra y las categorías vacías se descartan. */
    private static Proveedor proveedor(Long id, DatosProveedor d) {
        List<String> categorias = d.categorias() == null ? List.of()
                : d.categorias().stream().filter(c -> c != null && !c.isBlank()).map(String::trim).distinct().toList();
        return new Proveedor(id, limpio(d.razonSocial()), limpio(d.nit()), limpio(d.correo()), limpio(d.telefono()),
                limpio(d.direccion()), categorias);
    }

    private static String limpio(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static ProveedorJson json(Proveedor p) {
        return new ProveedorJson(p.id(), p.razonSocial(), p.nit(), p.correo(), p.telefono(), p.direccion(), p.categorias(), p.activo());
    }

}
