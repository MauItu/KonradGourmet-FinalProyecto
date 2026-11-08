package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.cotizaciones.puertos.salida.ProveedorRepositoryPort;

import java.util.List;

/**
 * En el proyecto real: @Repository.
 */
public class ProveedorJpaAdapter implements ProveedorRepositoryPort {

    @Override
    public Proveedor guardar(Proveedor p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public Proveedor porId(Long id) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public boolean existeNit(String nit) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Proveedor> activos() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Proveedor> todos() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
