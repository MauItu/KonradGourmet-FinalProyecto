package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.puertos.salida.DestinatarioRepositoryPort;

import java.util.List;

public class DestinatarioJpaAdapter implements DestinatarioRepositoryPort {

    @Override
    public List<String> jefesDe(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<String> jefes() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void guardar(Long sucursalId, String correo) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
