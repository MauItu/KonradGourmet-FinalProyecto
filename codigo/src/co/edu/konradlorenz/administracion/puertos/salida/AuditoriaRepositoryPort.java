package co.edu.konradlorenz.administracion.puertos.salida;

import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;

import java.time.LocalDate;
import java.util.List;

public interface AuditoriaRepositoryPort {

    void guardar(RegistroAuditoria r);

    List<RegistroAuditoria> entre(LocalDate desde, LocalDate hasta);

}
