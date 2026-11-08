package co.edu.konradlorenz.ordenes.adaptadores.salida;

import co.edu.konradlorenz.ordenes.dominio.EstadoOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import co.edu.konradlorenz.ordenes.puertos.salida.OrdenRepositoryPort;

import java.util.List;

public class OrdenJpaAdapter implements OrdenRepositoryPort {

    @Override
    public OrdenCompra guardar(OrdenCompra o) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<OrdenCompra> porEstado(EstadoOrden e) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public boolean existePorCotizacion(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
