package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.notificaciones.dominio.Mensaje;
import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;

import java.util.ArrayList;
import java.util.List;

/**
 * El componente concreto del Decorator en la demo: en vez de hablar SMTP, guarda cada mensaje tal como
 * le llega, con las cabeceras que le agregaron los decoradores.
 */
public class CorreoEnMemoria implements Correo {

    private final List<Mensaje> enviados = new ArrayList<>();

    @Override
    public void enviar(Mensaje m) {
        enviados.add(m);
    }

    public List<Mensaje> enviados() {
        return List.copyOf(enviados);
    }

}
