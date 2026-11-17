package co.edu.konradlorenz.menu.puertos.entrada;

import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;

import java.util.List;

public interface RegistrarPlatoUseCase {

    Plato registrar(Plato p);

    /** Todos los platos de la sucursal, también los desactivados. */
    List<Plato> consultar(Long sucursalId);

    Plato editar(Long id, Plato p);

    void desactivar(Long id);

    void reactivar(Long id);

    List<Producto> productosPorCategoria(Long categoriaId);

}
