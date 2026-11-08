package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.dominio.Mensaje;
import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;

/**
 * «Decorator»
 */
public abstract class CorreoDecorator implements Correo {

    protected final Correo envuelto;

    protected CorreoDecorator(Correo envuelto) {
        this.envuelto = envuelto;
    }

    @Override
    public void enviar(Mensaje m) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
