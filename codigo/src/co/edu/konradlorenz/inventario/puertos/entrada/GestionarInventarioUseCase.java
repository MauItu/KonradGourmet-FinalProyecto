package co.edu.konradlorenz.inventario.puertos.entrada;

import co.edu.konradlorenz.inventario.dominio.InsumoInventario;
import co.edu.konradlorenz.inventario.dominio.RecetaPlato;

import java.util.List;
import java.util.Map;

public interface GestionarInventarioUseCase {

    void acumularConsumo(Long sucursalId, Map<Long, Integer> lineas);

    void descontarConsumoDiario();

    List<InsumoInventario> alertas(Long sucursalId);

    List<InsumoInventario> insumos(Long sucursalId);

    void actualizarReceta(RecetaPlato r);

    void atenderAlertas(Long sucursalId, List<Long> productoIds);

}
