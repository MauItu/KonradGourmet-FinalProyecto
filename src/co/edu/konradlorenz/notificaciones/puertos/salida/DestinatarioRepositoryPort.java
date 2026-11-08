package co.edu.konradlorenz.notificaciones.puertos.salida;

import java.util.List;

public interface DestinatarioRepositoryPort {

    List<String> jefesDe(Long sucursalId);

    List<String> jefes();

    void guardar(Long sucursalId, String correo);

}
