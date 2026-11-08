package co.edu.konradlorenz.inventario.adaptadores.salida;

import co.edu.konradlorenz.inventario.dominio.Inventario;
import co.edu.konradlorenz.inventario.dominio.RecetaPlato;
import co.edu.konradlorenz.inventario.puertos.salida.InventarioRepositoryPort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class InventarioJpaAdapter implements InventarioRepositoryPort {

    @Override
    public Inventario inventarioDe(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void guardar(Inventario i) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void acumular(Long sucursalId, Map<Long, BigDecimal> consumo) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Long> sucursalesConConsumo() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public Map<Long, BigDecimal> consumoPendiente(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void guardarReceta(RecetaPlato r) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public RecetaPlato recetaDe(Long platoId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
