package co.edu.konradlorenz.menu.adaptadores.salida;

import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;
import co.edu.konradlorenz.menu.puertos.salida.PlatoRepositoryPort;

import java.util.List;

/**
 * En el proyecto real: @Repository.
 */
public class PlatoJpaAdapter implements PlatoRepositoryPort {

    @Override
    public Plato guardar(Plato p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Producto> productosPorCategoria(Long categoriaId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public Plato porId(Long id) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Plato> porSucursal(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public boolean existeNombre(Long sucursalId, String nombre) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
