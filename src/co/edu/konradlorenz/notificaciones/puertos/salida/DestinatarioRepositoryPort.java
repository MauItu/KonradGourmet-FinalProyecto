package co.edu.konradlorenz.notificaciones.puertos.salida;

import java.util.List;

public interface DestinatarioRepositoryPort {

    List<String> jefesDe(Long sucursalId);

    List<String> jefes();

    void guardar(Long sucursalId, String correo);

    /** El nombre de la sucursal, para el correo de stock bajo (HU-16, 4; DEC-102). */
    String nombreSucursal(Long sucursalId);

}
