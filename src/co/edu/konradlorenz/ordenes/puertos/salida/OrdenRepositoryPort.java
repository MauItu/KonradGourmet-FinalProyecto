package co.edu.konradlorenz.ordenes.puertos.salida;

import co.edu.konradlorenz.ordenes.dominio.EstadoOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;

import java.util.List;

public interface OrdenRepositoryPort {

    OrdenCompra guardar(OrdenCompra o);

    List<OrdenCompra> porEstado(EstadoOrden e);

    boolean existePorCotizacion(String numero);

}
