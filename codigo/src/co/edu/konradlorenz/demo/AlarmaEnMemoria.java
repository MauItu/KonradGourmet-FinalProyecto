package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.notificaciones.puertos.salida.AlarmaPushPort;

import java.util.ArrayList;
import java.util.List;

/** Hace las veces del WebSocket: guarda cada alarma que se empuja a la aplicación. */
public class AlarmaEnMemoria implements AlarmaPushPort {

    private final List<String> alarmas = new ArrayList<>();

    @Override
    public void empujar(String destino, String contenido) {
        alarmas.add(destino + ": " + contenido);
    }

    public List<String> alarmas() {
        return List.copyOf(alarmas);
    }

}
