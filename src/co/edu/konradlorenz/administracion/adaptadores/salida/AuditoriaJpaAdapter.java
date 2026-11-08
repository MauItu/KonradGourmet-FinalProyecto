package co.edu.konradlorenz.administracion.adaptadores.salida;

import co.edu.konradlorenz.administracion.dominio.Bitacora;
import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;
import co.edu.konradlorenz.administracion.puertos.salida.AuditoriaRepositoryPort;

import java.time.LocalDate;
import java.util.List;

/**
 * En el proyecto real: @Repository.
 *
 * En el esqueleto, «bitacora» simula la tabla en memoria; en el proyecto real este adaptador
 * usa un repositorio JPA sobre la base de datos de ms-administracion.
 */
public class AuditoriaJpaAdapter implements AuditoriaRepositoryPort {

    private final Bitacora bitacora;

    public AuditoriaJpaAdapter(Bitacora bitacora) {
        this.bitacora = bitacora;
    }

    @Override
    public void guardar(RegistroAuditoria r) {
        bitacora.registrar(r);
    }

    @Override
    public List<RegistroAuditoria> entre(LocalDate desde, LocalDate hasta) {
        return bitacora.registros().stream()
                .filter(r -> desde == null || !r.fecha().isBefore(desde))
                .filter(r -> hasta == null || !r.fecha().isAfter(hasta))
                .toList();
    }

}
