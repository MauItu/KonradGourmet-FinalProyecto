package co.edu.konradlorenz.notificaciones.dominio;

import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;

public class CreadorCorreo extends CreadorNotificacion {

    private final Correo correo;

    public CreadorCorreo(Correo correo) {
        this.correo = correo;
    }

    @Override
    protected Notificacion crearNotificacion() {
        return new NotificacionCorreo(correo);
    }

}
