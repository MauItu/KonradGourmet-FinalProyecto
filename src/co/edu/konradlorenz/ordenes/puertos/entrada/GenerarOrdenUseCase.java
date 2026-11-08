package co.edu.konradlorenz.ordenes.puertos.entrada;

import co.edu.konradlorenz.ordenes.dominio.EstadoOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;

import java.util.List;

public interface GenerarOrdenUseCase {

    OrdenCompra generar(String cotizacionNumero);

    List<OrdenCompra> consultar(EstadoOrden estado);

}
