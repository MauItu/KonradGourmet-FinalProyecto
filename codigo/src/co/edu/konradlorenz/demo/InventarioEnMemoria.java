package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.inventario.dominio.Inventario;
import co.edu.konradlorenz.inventario.dominio.RecetaPlato;
import co.edu.konradlorenz.inventario.puertos.salida.InventarioRepositoryPort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** El inventario, las recetas y el consumo pendiente de cada sucursal en mapas, en vez de la base de ms-inventario. */
public class InventarioEnMemoria implements InventarioRepositoryPort {

    private final Map<Long, Inventario> inventarios = new HashMap<>();
    private final Map<Long, RecetaPlato> recetas = new HashMap<>();
    private final Map<Long, Map<Long, BigDecimal>> consumo = new HashMap<>();

    @Override
    public Inventario inventarioDe(Long sucursalId) {
        return inventarios.computeIfAbsent(sucursalId, id -> new Inventario(id, new ArrayList<>()));
    }

    @Override
    public void guardar(Inventario i) {
        inventarios.put(i.sucursalId(), i);
    }

    /** Suma el consumo; una cantidad negativa lo resta, y los productos que quedan en cero se quitan. */
    @Override
    public void acumular(Long sucursalId, Map<Long, BigDecimal> cantidades) {
        Map<Long, BigDecimal> pendiente = consumo.computeIfAbsent(sucursalId, id -> new LinkedHashMap<>());
        cantidades.forEach((productoId, cantidad) -> pendiente.merge(productoId, cantidad, BigDecimal::add));
        pendiente.values().removeIf(c -> c.signum() <= 0);
    }

    @Override
    public List<Long> sucursalesConConsumo() {
        return consumo.entrySet().stream().filter(e -> !e.getValue().isEmpty()).map(Map.Entry::getKey).toList();
    }

    @Override
    public Map<Long, BigDecimal> consumoPendiente(Long sucursalId) {
        return new LinkedHashMap<>(consumo.getOrDefault(sucursalId, Map.of()));
    }

    @Override
    public void guardarReceta(RecetaPlato r) {
        recetas.put(r.platoId(), r);
    }

    @Override
    public RecetaPlato recetaDe(Long platoId) {
        return recetas.get(platoId);
    }

}
