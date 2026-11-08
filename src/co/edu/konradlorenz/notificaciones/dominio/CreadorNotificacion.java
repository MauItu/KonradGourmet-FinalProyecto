package co.edu.konradlorenz.notificaciones.dominio;

/**
 * «Factory Method · creador»
 */
public abstract class CreadorNotificacion {

    public void notificar(String destino, String contenido, String adjuntoUrl) {
        // Factory Method: la subclase decide qué producto crear; aquí solo se usa
        crearNotificacion().enviar(destino, contenido, adjuntoUrl);
    }

    protected abstract Notificacion crearNotificacion();

}
