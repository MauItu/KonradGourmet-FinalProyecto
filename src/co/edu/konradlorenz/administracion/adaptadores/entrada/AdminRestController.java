package co.edu.konradlorenz.administracion.adaptadores.entrada;

import co.edu.konradlorenz.administracion.dominio.LogError;
import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;
import co.edu.konradlorenz.administracion.puertos.entrada.AdministracionUseCase;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * «REST · /api/admin · GET …/parametros/tema»
 */
public class AdminRestController {

    private final AdministracionUseCase casoDeUso;

    public AdminRestController(AdministracionUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public void parametrizar(String clave, String valor) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public List<RegistroAuditoria> auditoria(LocalDate desde, LocalDate hasta) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public List<LogError> logs(String servicio) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // GET /api/admin/parametros/tema
    public Map<String, String> tema() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
