package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.dominio.Mensaje;
import co.edu.konradlorenz.notificaciones.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;

/**
 * «Decorator · componente concreto (SMTP/TLS)»
 */
public class CorreoSmtpAdapter implements Correo {

    private final AlmacenArchivosPort almacen;

    public CorreoSmtpAdapter(AlmacenArchivosPort almacen) {
        this.almacen = almacen;
    }

    @Override
    public void enviar(Mensaje m) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
