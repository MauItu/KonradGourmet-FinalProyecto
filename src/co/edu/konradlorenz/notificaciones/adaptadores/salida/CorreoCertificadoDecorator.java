package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.dominio.Mensaje;
import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;

/**
 * «Decorator concreto»
 */
public class CorreoCertificadoDecorator extends CorreoDecorator {

    public CorreoCertificadoDecorator(Correo envuelto) {
        super(envuelto);
    }

    @Override
    public void enviar(Mensaje m) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
