package co.edu.konradlorenz.inventario.configuracion;

import co.edu.konradlorenz.inventario.adaptadores.entrada.ConsumoDiarioJob;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Solo para la demostración (DEC-68, no está en el diagrama): el jefe de cocina puede adelantar el cierre
 * de las 23:59 con el botón «Cierre del día» de V07, como en la interfaz en memoria. Llama al mismo
 * ConsumoDiarioJob, con su ShedLock: si el cierre ya está corriendo, no se repite.
 */
@RestController
public class CierreDiaController {

    private final ConsumoDiarioJob job;

    public CierreDiaController(ConsumoDiarioJob job) {
        this.job = job;
    }

    // POST /api/inventario/cierre
    @PostMapping("/api/inventario/cierre")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('JEFE_COCINA')")
    public void cerrarDia() {
        job.ejecutar();
    }

}
