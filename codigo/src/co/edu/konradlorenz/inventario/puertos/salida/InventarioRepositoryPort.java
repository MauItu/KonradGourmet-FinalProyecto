package co.edu.konradlorenz.inventario.puertos.salida;

import co.edu.konradlorenz.inventario.dominio.Inventario;
import co.edu.konradlorenz.inventario.dominio.RecetaPlato;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface InventarioRepositoryPort {

    Inventario inventarioDe(Long sucursalId);

    void guardar(Inventario i);

    void acumular(Long sucursalId, Map<Long, BigDecimal> consumo);

    List<Long> sucursalesConConsumo();

    Map<Long, BigDecimal> consumoPendiente(Long sucursalId);

    void guardarReceta(RecetaPlato r);

    RecetaPlato recetaDe(Long platoId);

}
