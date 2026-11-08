package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.puertos.salida.AlarmaPushPort;

/**
 * «WebSocket → V05»
 *
 * En el proyecto real: @Component.
 */
public class AlarmaPushAdapter implements AlarmaPushPort {

    @Override
    public void empujar(String destino, String contenido) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
