package co.edu.konradlorenz.menu.adaptadores.entrada;

import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;
import co.edu.konradlorenz.menu.puertos.entrada.RegistrarPlatoUseCase;

import java.util.List;

/**
 * «REST · /api/platos · PUT …/{id} · PATCH …/{id}/desactivar · PATCH …/{id}/reactivar · …/{id}/imagen»
 *
 * En el proyecto real: @RestController.
 *
 * Nota del diagrama: El plato queda asociado a la sucursal y al usuario que lo crea, y su nombre es
 * único en la sucursal (existeNombre). crear(), editar(), desactivar() y reactivar() publican
 * menu.actualizado; con activo = false el plato sale del catálogo de los meseros, pero no se borra
 * (HU-04, ventana V06).
 */
public class PlatoRestController {

    private final RegistrarPlatoUseCase casoDeUso;

    public PlatoRestController(RegistrarPlatoUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    // Atiende la ventana V06 de los mockups
    // POST /api/platos
    public Plato crear(Plato p) {
        return casoDeUso.registrar(p);
    }

    // GET /api/platos?sucursal={id}: también los desactivados
    public List<Plato> consultar(Long sucursalId) {
        return casoDeUso.consultar(sucursalId);
    }

    // PUT /api/platos/{id}
    public Plato editar(Long id, Plato p) {
        return casoDeUso.editar(id, p);
    }

    // PATCH /api/platos/{id}/desactivar
    public void desactivar(Long id) {
        casoDeUso.desactivar(id);
    }

    // PATCH /api/platos/{id}/reactivar
    public void reactivar(Long id) {
        casoDeUso.reactivar(id);
    }

    // Atiende la ventana V06 de los mockups
    // GET /api/platos/productos?categoria={id}
    public List<Producto> productosPorCategoria(Long categoriaId) {
        return casoDeUso.productosPorCategoria(categoriaId);
    }

    // Atiende la ventana V06 de los mockups
    // PUT /api/platos/{id}/imagen
    public void subirImagen(Long id, byte[] contenido, String tipo) {
        casoDeUso.guardarImagen(id, contenido, tipo);
    }

    // Atiende la ventana V03 de los mockups
    // Atiende la ventana V06 de los mockups
    // GET /api/platos/{id}/imagen
    public byte[] imagen(Long id) {
        return casoDeUso.imagen(id);
    }

}
