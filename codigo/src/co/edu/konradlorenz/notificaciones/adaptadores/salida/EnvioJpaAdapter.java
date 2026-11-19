package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.puertos.salida.EnvioRepositoryPort;

import java.time.LocalDateTime;

/**
 * En el proyecto real: @Repository.
 *
 * Nota del diagrama: Si el correo falla, registrarFallo() deja la traza; tras dos reintentos el evento pasa
 * a la cola de errores notificaciones.dlq, que se vuelve a procesar cada 5 minutos, hasta tres veces; el
 * auxiliar también puede reenviar la cotización (HU-12).
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
