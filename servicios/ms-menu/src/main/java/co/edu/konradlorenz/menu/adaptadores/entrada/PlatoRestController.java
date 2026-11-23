package co.edu.konradlorenz.menu.adaptadores.entrada;

import co.edu.konradlorenz.compartido.infraestructura.Sesion;
import co.edu.konradlorenz.menu.dominio.Ingrediente;
import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;
import co.edu.konradlorenz.menu.dominio.UnidadMedida;
import co.edu.konradlorenz.menu.puertos.entrada.RegistrarPlatoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * «REST · /api/platos · PUT …/{id} · PATCH …/{id}/desactivar · PATCH …/{id}/reactivar · …/{id}/imagen»
 *
 * Nota del diagrama: El plato queda asociado a la sucursal y al usuario que lo crea, y su nombre es
 * único en la sucursal (existeNombre). crear(), editar(), desactivar() y reactivar() publican
 * menu.actualizado; con activo = false el plato sale del catálogo de los meseros, pero no se borra
 * (HU-04, ventana V06).
 *
 * La sucursal y quién crea el plato salen del JWT, no del cuerpo. Solo el jefe de cocina gestiona
 * platos, y solo los de su sucursal; la imagen también la puede ver el mesero de esa sucursal (V03).
 */
@RestController
@RequestMapping("/api/platos")
public class PlatoRestController {

    private final RegistrarPlatoUseCase casoDeUso;

    public PlatoRestController(RegistrarPlatoUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    /** Lo que manda V06 al crear o editar: unidad «kg» o «g». */
    public record DatosPlato(String nombre, String categoria, String descripcion, BigDecimal precioVenta, List<DatosIngrediente> ingredientes) {
    }

    public record DatosIngrediente(Long productoId, BigDecimal cantidad, String unidad) {
    }

    public record PlatoJson(Long id, Long sucursalId, String nombre, String categoria, String descripcion, BigDecimal precioVenta,
                            boolean activo, boolean imagen, List<IngredienteJson> ingredientes) {
    }

    public record IngredienteJson(Long productoId, String producto, String categoria, BigDecimal cantidad, String unidad, BigDecimal kg) {
    }

    public record ProductoJson(Long id, String nombre, Long categoriaId, String categoria) {
    }

    // Atiende la ventana V06 de los mockups
    // POST /api/platos
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('JEFE_COCINA')")
    @Transactional // el cambio y sus eventos en la outbox, juntos (DEC-104)
    public PlatoJson crear(@RequestBody DatosPlato p, @AuthenticationPrincipal Jwt jwt) {
        return json(casoDeUso.registrar(plato(p, jwt)));
    }

    // GET /api/platos: los de la sucursal del token, también los desactivados
    @GetMapping
    @PreAuthorize("hasRole('JEFE_COCINA')")
    public List<PlatoJson> consultar(@AuthenticationPrincipal Jwt jwt) {
        return casoDeUso.consultar(Sesion.sucursalId(jwt)).stream().map(PlatoRestController::json).toList();
    }

    // PUT /api/platos/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('JEFE_COCINA')")
    @Transactional // el cambio y sus eventos en la outbox, juntos (DEC-104)
    public PlatoJson editar(@PathVariable Long id, @RequestBody DatosPlato p, @AuthenticationPrincipal Jwt jwt) {
        deLaSucursal(id, jwt);
        return json(casoDeUso.editar(id, plato(p, jwt)));
    }

    // PATCH /api/platos/{id}/desactivar
    @PatchMapping("/{id}/desactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('JEFE_COCINA')")
    @Transactional // el cambio y sus eventos en la outbox, juntos (DEC-104)
    public void desactivar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        deLaSucursal(id, jwt);
        casoDeUso.desactivar(id);
    }

    // PATCH /api/platos/{id}/reactivar
    @PatchMapping("/{id}/reactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('JEFE_COCINA')")
    @Transactional // el cambio y sus eventos en la outbox, juntos (DEC-104)
    public void reactivar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        deLaSucursal(id, jwt);
        casoDeUso.reactivar(id);
    }

    // Atiende la ventana V06 de los mockups
    // GET /api/platos/productos?categoria={id}; sin categoría, todos
    // (también la usan V08 para escoger el producto y V11–V12, del auxiliar, para nombrarlo: DEC-72)
    @GetMapping("/productos")
    @PreAuthorize("hasAnyRole('JEFE_COCINA', 'AUXILIAR_COMPRAS')")
    public List<ProductoJson> productosPorCategoria(@RequestParam(name = "categoria", required = false) Long categoriaId) {
        return casoDeUso.productosPorCategoria(categoriaId).stream()
                .map(p -> new ProductoJson(p.id(), p.nombre(), p.categoria().id(), p.categoria().nombre())).toList();
    }

    // Atiende la ventana V06 de los mockups
    // PUT /api/platos/{id}/imagen (el cuerpo es la imagen, con su Content-Type)
    @PutMapping(value = "/{id}/imagen", consumes = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, "image/webp", "image/gif"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('JEFE_COCINA')")
    @Transactional // el cambio y sus eventos en la outbox, juntos (DEC-104)
    public void subirImagen(@PathVariable Long id, @RequestBody byte[] contenido, @RequestHeader("Content-Type") String tipo,
                            @AuthenticationPrincipal Jwt jwt) {
        deLaSucursal(id, jwt);
        casoDeUso.guardarImagen(id, contenido, MediaType.parseMediaType(tipo).getType() + "/" + MediaType.parseMediaType(tipo).getSubtype());
    }

    // Atiende la ventana V03 de los mockups
    // Atiende la ventana V06 de los mockups
    // GET /api/platos/{id}/imagen (404 si el plato no tiene)
    @GetMapping("/{id}/imagen")
    @PreAuthorize("hasAnyRole('JEFE_COCINA', 'MESERO')")
    public ResponseEntity<byte[]> imagen(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        deLaSucursal(id, jwt);
        byte[] b;
        try {
            b = casoDeUso.imagen(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(tipoDe(b)).header("Cache-Control", "no-cache").body(b);
    }

    /** El plato no es de la sucursal del token (o no existe): se responde como si no existiera. */
    static class PlatoNoDisponible extends RuntimeException {
        PlatoNoDisponible() {
            super("El plato consultado no está disponible", null, false, false);
        }
    }

    @ExceptionHandler(PlatoNoDisponible.class)
    public ResponseEntity<Map<String, String>> noDisponible(PlatoNoDisponible e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
    }

    /**
     * Cada jefe gestiona solo los platos de su sucursal, y el mesero solo ve las imágenes de la suya: la
     * sucursal sale del JWT, como en GET /api/platos (C7-1).
     */
    private void deLaSucursal(Long id, Jwt jwt) {
        Long sucursal = Sesion.sucursalId(jwt);
        if (id == null || casoDeUso.consultar(sucursal).stream().noneMatch(p -> id.equals(p.id()))) {
            throw new PlatoNoDisponible();
        }
    }

    private static MediaType tipoDe(byte[] b) {
        if (b.length > 3 && (b[0] & 0xFF) == 0x89 && b[1] == 'P') {
            return MediaType.IMAGE_PNG;
        }
        if (b.length > 11 && b[0] == 'R' && b[8] == 'W' && b[9] == 'E') {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }

    private static Plato plato(DatosPlato d, Jwt jwt) {
        Plato p = new Plato(Sesion.sucursalId(jwt), Sesion.usuarioId(jwt), d.nombre(), d.categoria(), d.descripcion(), d.precioVenta());
        if (d.ingredientes() != null) {
            for (DatosIngrediente i : d.ingredientes()) {
                if (i.productoId() == null) {
                    throw new IllegalArgumentException("Cada ingrediente necesita un producto");
                }
                p.agregarIngrediente(new Ingrediente(new Producto(i.productoId(), null, null), i.cantidad(), unidad(i.unidad())));
            }
        }
        return p;
    }

    private static UnidadMedida unidad(String u) {
        if (u == null || u.isBlank() || u.equalsIgnoreCase("kg")) {
            return UnidadMedida.KG;
        }
        if (u.equalsIgnoreCase("g")) {
            return UnidadMedida.GRAMO;
        }
        try {
            return UnidadMedida.valueOf(u.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unidad desconocida: " + u);
        }
    }

    private static PlatoJson json(Plato p) {
        return new PlatoJson(p.id(), p.sucursalId(), p.nombre(), p.categoria(), p.descripcion(), p.precioVenta(), p.activo(),
                p.imagenUrl() != null, p.ingredientes().stream().map(i -> new IngredienteJson(i.producto().id(), i.producto().nombre(),
                        i.producto().categoria() == null ? null : i.producto().categoria().nombre(), i.cantidad(), i.unidad() == UnidadMedida.GRAMO ? "g" : "kg", i.enKg())).toList());
    }

}
