package co.edu.konradlorenz.inventario.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.inventario.dominio.InsumoInventario;
import co.edu.konradlorenz.inventario.dominio.RecetaPlato;
import co.edu.konradlorenz.inventario.dominio.UmbralStock;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;
import co.edu.konradlorenz.inventario.puertos.salida.InventarioRepositoryPort;

import java.util.List;
import java.util.Map;

public class InventarioService implements GestionarInventarioUseCase {

    private final InventarioRepositoryPort repositorio;
    private final UmbralStock umbral;
    private final EventPublisherPort eventos;

    public InventarioService(InventarioRepositoryPort repositorio, UmbralStock umbral, EventPublisherPort eventos) {
        this.repositorio = repositorio;
        this.umbral = umbral;
        this.eventos = eventos;
    }

    @Override
    public void acumularConsumo(Long sucursalId, Map<Long, Integer> lineas) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void descontarConsumoDiario() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<InsumoInventario> alertas(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<InsumoInventario> insumos(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void actualizarReceta(RecetaPlato r) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void atenderAlertas(Long sucursalId, List<Long> productoIds) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
