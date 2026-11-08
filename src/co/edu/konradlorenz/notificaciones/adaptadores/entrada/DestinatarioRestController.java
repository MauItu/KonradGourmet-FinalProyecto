package co.edu.konradlorenz.notificaciones.adaptadores.entrada;

import co.edu.konradlorenz.notificaciones.puertos.entrada.NotificarUseCase;

import java.util.List;

/**
 * «REST · /api/notificaciones/destinatarios (Administrador)»
 *
 * En el proyecto real: @RestController.
 */
public class DestinatarioRestController {

    private final NotificarUseCase casoDeUso;

    public DestinatarioRestController(NotificarUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public void registrar(Long sucursalId, String correo) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public List<String> consultar(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
