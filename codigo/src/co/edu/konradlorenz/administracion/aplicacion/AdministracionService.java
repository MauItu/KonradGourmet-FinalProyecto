package co.edu.konradlorenz.administracion.aplicacion;

import co.edu.konradlorenz.administracion.dominio.LogError;
import co.edu.konradlorenz.administracion.dominio.ParametrosSistema;
import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;
import co.edu.konradlorenz.administracion.puertos.entrada.AdministracionUseCase;
import co.edu.konradlorenz.administracion.puertos.salida.AuditoriaRepositoryPort;
import co.edu.konradlorenz.administracion.puertos.salida.LogsPort;
import co.edu.konradlorenz.administracion.puertos.salida.ParametrosRepositoryPort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("La clave del parámetro es obligatoria");
        }
        if (valor == null) {
            throw new IllegalArgumentException("El valor del parámetro " + clave + " es obligatorio");
        }
        parametros.guardar(clave, valor);
        ParametrosSistema.getInstancia().actualizar(clave, valor); // refresca la caché de esta réplica
        // ms-administracion no publica auditoria.evento: escribe directo en su bitácora (H10)
        LocalDateTime ahora = LocalDateTime.now();
        registrarAuditoria(new RegistroAuditoria("Parámetro " + clave + " actualizado", "administrador", ahora.toLocalDate(), ahora.toLocalTime()));
    }

    @Override
    public void registrarAuditoria(RegistroAuditoria r) {
        repositorio.guardar(r);
    }

    @Override
    public List<RegistroAuditoria> auditoria(LocalDate desde, LocalDate hasta) {
        return repositorio.entre(desde, hasta);
    }

    @Override
    public List<LogError> logs(String servicio) {
        return logs.logs(servicio);
    }

    @Override
    public Map<String, String> tema() {
        // el Singleton es la caché de la base: se refresca y se devuelven solo los parámetros del tema
        ParametrosSistema cache = ParametrosSistema.getInstancia();
        parametros.cargar().forEach(cache::actualizar);
        Map<String, String> tema = new TreeMap<>();
        cache.valores().forEach((clave, valor) -> {
            if (clave.startsWith("tema.")) {
                tema.put(clave, valor);
            }
        });
        return tema;
    }

}
