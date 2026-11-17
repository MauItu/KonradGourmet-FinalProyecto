package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;
import co.edu.konradlorenz.menu.puertos.salida.PlatoRepositoryPort;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Los platos del menú en un mapa, en vez de la tabla de ms-menu. */
public class PlatoEnMemoria implements PlatoRepositoryPort {

    private final Map<Long, Plato> datos = new LinkedHashMap<>();
    private long ultimoId = 0;

    /** Un plato nuevo recibe el siguiente id; uno que ya tenía id reemplaza al anterior. */
    @Override
    public Plato guardar(Plato p) {
        if (p.id() == null) {
            p.asignarId(++ultimoId);
        }
        datos.put(p.id(), p);
        return p;
    }

    /** La demo no tiene tabla de productos: las recetas se arman con los ids del inventario. */
    @Override
    public List<Producto> productosPorCategoria(Long categoriaId) {
        return List.of();
    }

    @Override
    public Plato porId(Long id) {
        return datos.get(id);
    }

    @Override
    public List<Plato> porSucursal(Long sucursalId) {
        return datos.values().stream().filter(p -> p.sucursalId().equals(sucursalId)).toList();
    }

    @Override
    public boolean existeNombre(Long sucursalId, String nombre) {
        return datos.values().stream().anyMatch(p -> p.sucursalId().equals(sucursalId) && p.nombre().equalsIgnoreCase(nombre));
    }

}
