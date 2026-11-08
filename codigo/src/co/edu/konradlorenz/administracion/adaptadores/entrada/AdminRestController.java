package co.edu.konradlorenz.administracion.adaptadores.entrada;

import co.edu.konradlorenz.administracion.dominio.LogError;
import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;
import co.edu.konradlorenz.administracion.puertos.entrada.AdministracionUseCase;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * «REST · /api/admin · GET …/parametros/tema»
 *
 * En el proyecto real: @RestController.
 */
public class AdminRestController {

    private final AdministracionUseCase casoDeUso;

    public AdminRestController(AdministracionUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public void parametrizar(String clave, String valor) {
        casoDeUso.parametrizar(clave, valor);
    }

    public List<RegistroAuditoria> auditoria(LocalDate desde, LocalDate hasta) {
        return casoDeUso.auditoria(desde, hasta);
    }

    public List<LogError> logs(String servicio) {
        return casoDeUso.logs(servicio);
    }

    // GET /api/admin/parametros/tema
    public Map<String, String> tema() {
        return casoDeUso.tema();
    }

}
