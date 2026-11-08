package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.pedidos.dominio.CatalogoPlatos;
import co.edu.konradlorenz.pedidos.dominio.PlatoCatalogo;
import co.edu.konradlorenz.pedidos.puertos.salida.CatalogoRepositoryPort;

import java.util.HashMap;
import java.util.Map;

/** El catálogo de platos de cada sucursal en un mapa, en vez de la tabla de ms-pedidos. */
public class CatalogoEnMemoria implements CatalogoRepositoryPort {

    private final Map<Long, CatalogoPlatos> porSucursal = new HashMap<>();

    @Override
    public CatalogoPlatos catalogo(Long sucursalId) {
        return porSucursal.computeIfAbsent(sucursalId, CatalogoPlatos::new);
    }

    @Override
    public void guardar(PlatoCatalogo p) {
        catalogo(p.sucursalId()).actualizar(p);
    }

}
