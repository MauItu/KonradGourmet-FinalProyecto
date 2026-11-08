package co.edu.konradlorenz.notificaciones.dominio;

import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;

public class NotificacionCorreo implements Notificacion {

    private final Correo correo;

    public NotificacionCorreo(Correo correo) {
        this.correo = correo;
    }

    @Override
    public void enviar(String destino, String contenido, String adjuntoUrl) {
        // la primera línea del contenido es el asunto; el resto, el cuerpo
        int salto = contenido.indexOf('\n');
        String asunto = salto < 0 ? "Konrad Gourmet" : contenido.substring(0, salto);
        String cuerpo = salto < 0 ? contenido : contenido.substring(salto + 1);
        correo.enviar(new Mensaje(destino, asunto, cuerpo, adjuntoUrl));
    }

}
