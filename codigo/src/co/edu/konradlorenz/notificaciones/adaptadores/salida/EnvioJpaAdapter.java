package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.puertos.salida.EnvioRepositoryPort;

import java.time.LocalDateTime;

/**
 * En el proyecto real: @Repository.
 *
 * Nota del diagrama: Si el correo falla, registrarFallo() deja la traza y el evento pasa a una cola de
 * errores (DLQ) para reintentarlo; el auxiliar también puede reenviar la cotización (HU-12).
 */
public class EnvioJpaAdapter implements EnvioRepositoryPort {

    @Override
    public void registrar(String destino, String tipo, LocalDateTime fechaHora) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void registrarFallo(String destino, String tipo, String error) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
