package co.edu.konradlorenz.menu.adaptadores.entrada;

import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;
import co.edu.konradlorenz.menu.puertos.entrada.RegistrarPlatoUseCase;

import java.util.List;

/**
 * «REST · /api/platos · PUT …/{id} · PATCH …/{id}/desactivar»
 *
 * Nota del diagrama: El plato queda asociado a la sucursal y al usuario que lo crea, y su nombre es
 * único en la sucursal (existeNombre). editar() y desactivar() publican menu.actualizado; con activo =
 * false el plato sale del catálogo de los meseros. Sin ventana en el prototipo (HU-04).
 */
public class PlatoRestController {

    private final RegistrarPlatoUseCase casoDeUso;

    public PlatoRestController(RegistrarPlatoUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    // Atiende la ventana V06 de los mockups
    // POST /api/platos
    public Plato crear(Plato p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public List<Plato> consultar(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // PUT /api/platos/{id}
    public Plato editar(Long id, Plato p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // PATCH /api/platos/{id}/desactivar
    public void desactivar(Long id) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V06 de los mockups
    public List<Producto> productosPorCategoria(Long categoriaId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
