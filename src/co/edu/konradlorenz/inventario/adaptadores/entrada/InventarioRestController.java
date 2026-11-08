package co.edu.konradlorenz.inventario.adaptadores.entrada;

import co.edu.konradlorenz.inventario.dominio.InsumoInventario;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;

import java.util.List;

/**
 * «REST · /api/inventario»
 */
public class InventarioRestController {

    private final GestionarInventarioUseCase casoDeUso;

    public InventarioRestController(GestionarInventarioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    // Atiende la ventana V05 de los mockups
    // GET /api/inventario/alertas
    public List<InsumoInventario> alertas(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V07 de los mockups
    // GET /api/inventario/insumos
    public List<InsumoInventario> insumos(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
