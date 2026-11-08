package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.ordenes.dominio.EstadoOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import co.edu.konradlorenz.ordenes.puertos.salida.OrdenRepositoryPort;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Las órdenes en un mapa. Le da el número a la orden nueva, como lo haría una secuencia de la base:
 * «OC-» y cuatro dígitos, a partir del que sigue al último.
 */
public class OrdenEnMemoria implements OrdenRepositoryPort {

    private final Map<String, OrdenCompra> datos = new LinkedHashMap<>();
    private int ultimo;

    public OrdenEnMemoria(int ultimoNumero) {
        this.ultimo = ultimoNumero;
    }

    @Override
    public OrdenCompra guardar(OrdenCompra o) {
        OrdenCompra guardada = o.numero() == null ? new OrdenCompra(String.format("OC-%04d", ++ultimo), o) : o;
        datos.put(guardada.numero(), guardada);
        return guardada;
    }

    @Override
    public List<OrdenCompra> porEstado(EstadoOrden e) {
        return datos.values().stream().filter(o -> o.estado() == e).toList();
    }

    @Override
    public boolean existePorCotizacion(String numero) {
        return datos.values().stream().anyMatch(o -> o.cotizacionNumero().equals(numero));
    }

}
