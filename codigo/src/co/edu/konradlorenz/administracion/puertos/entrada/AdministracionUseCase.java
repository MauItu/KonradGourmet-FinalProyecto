package co.edu.konradlorenz.administracion.puertos.entrada;

import co.edu.konradlorenz.administracion.dominio.LogError;
import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AdministracionUseCase {

    void parametrizar(String clave, String valor);

    void registrarAuditoria(RegistroAuditoria r);

    List<RegistroAuditoria> auditoria(LocalDate desde, LocalDate hasta);

    List<LogError> logs(String servicio);

    Map<String, String> tema();

}
