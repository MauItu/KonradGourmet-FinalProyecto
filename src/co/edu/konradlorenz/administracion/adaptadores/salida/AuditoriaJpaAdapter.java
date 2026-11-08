package co.edu.konradlorenz.administracion.adaptadores.salida;

import co.edu.konradlorenz.administracion.dominio.Bitacora;
import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;
import co.edu.konradlorenz.administracion.puertos.salida.AuditoriaRepositoryPort;

import java.time.LocalDate;
import java.util.List;

/**
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
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<RegistroAuditoria> entre(LocalDate desde, LocalDate hasta) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
