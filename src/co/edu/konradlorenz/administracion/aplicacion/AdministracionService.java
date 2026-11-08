package co.edu.konradlorenz.administracion.aplicacion;

import co.edu.konradlorenz.administracion.dominio.LogError;
import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;
import co.edu.konradlorenz.administracion.puertos.entrada.AdministracionUseCase;
import co.edu.konradlorenz.administracion.puertos.salida.AuditoriaRepositoryPort;
import co.edu.konradlorenz.administracion.puertos.salida.LogsPort;
import co.edu.konradlorenz.administracion.puertos.salida.ParametrosRepositoryPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AdministracionService implements AdministracionUseCase {

    private final AuditoriaRepositoryPort repositorio;
    private final LogsPort logs;
    private final ParametrosRepositoryPort parametros;

    public AdministracionService(AuditoriaRepositoryPort repositorio, LogsPort logs, ParametrosRepositoryPort parametros) {
        this.repositorio = repositorio;
        this.logs = logs;
        this.parametros = parametros;
    }

    @Override
    public void parametrizar(String clave, String valor) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void registrarAuditoria(RegistroAuditoria r) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<RegistroAuditoria> auditoria(LocalDate desde, LocalDate hasta) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<LogError> logs(String servicio) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public Map<String, String> tema() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
