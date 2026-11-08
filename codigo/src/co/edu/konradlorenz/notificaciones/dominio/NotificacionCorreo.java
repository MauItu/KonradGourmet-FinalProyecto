package co.edu.konradlorenz.notificaciones.dominio;

import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;

public class NotificacionCorreo implements Notificacion {

    private final Correo correo;

    public NotificacionCorreo(Correo correo) {
        this.correo = correo;
    }

    @Override
    public void enviar(String destino, String contenido) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
