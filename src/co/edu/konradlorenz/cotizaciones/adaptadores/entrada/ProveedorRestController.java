package co.edu.konradlorenz.cotizaciones.adaptadores.entrada;

import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarProveedorUseCase;

import java.util.List;

/**
 * «REST · /api/proveedores (Auxiliar de compras)»
 *
 * En el proyecto real: @RestController.
 */
public class ProveedorRestController {

    private final GestionarProveedorUseCase casoDeUso;

    public ProveedorRestController(GestionarProveedorUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public Proveedor registrar(Proveedor p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V13 de los mockups
    // GET /api/proveedores
    public List<Proveedor> consultar(boolean soloActivos) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public Proveedor detalle(Long id) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public Proveedor editar(Long id, Proveedor p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void inactivar(Long id) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
