package co.edu.konradlorenz.pedidos.adaptadores.salida;

import co.edu.konradlorenz.pedidos.dominio.CatalogoPlatos;
import co.edu.konradlorenz.pedidos.dominio.PlatoCatalogo;
import co.edu.konradlorenz.pedidos.puertos.salida.CatalogoRepositoryPort;

/**
 * En el proyecto real: @Repository.
 */
public class CatalogoJpaAdapter implements CatalogoRepositoryPort {

    @Override
    public CatalogoPlatos catalogo(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void guardar(PlatoCatalogo p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
