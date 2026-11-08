package co.edu.konradlorenz.menu.puertos.salida;

import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;

import java.util.List;

public interface PlatoRepositoryPort {

    Plato guardar(Plato p);

    List<Producto> productosPorCategoria(Long categoriaId);

    Plato porId(Long id);

    List<Plato> porSucursal(Long sucursalId);

    boolean existeNombre(Long sucursalId, String nombre);

}
