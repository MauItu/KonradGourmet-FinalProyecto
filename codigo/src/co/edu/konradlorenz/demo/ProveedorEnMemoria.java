package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.cotizaciones.puertos.salida.ProveedorRepositoryPort;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Los proveedores en un mapa, en vez de la tabla de ms-cotizaciones. */
public class ProveedorEnMemoria implements ProveedorRepositoryPort {

    private final Map<Long, Proveedor> datos = new LinkedHashMap<>();

    @Override
    public Proveedor guardar(Proveedor p) {
        datos.put(p.id(), p);
        return p;
    }

    @Override
    public Proveedor porId(Long id) {
        return datos.get(id);
    }

    @Override
    public boolean existeNit(String nit) {
        return datos.values().stream().anyMatch(p -> p.nit().equals(nit));
    }

    @Override
    public List<Proveedor> activos() {
        return datos.values().stream().filter(Proveedor::activo).toList();
    }

    @Override
    public List<Proveedor> todos() {
        return List.copyOf(datos.values());
    }

}
