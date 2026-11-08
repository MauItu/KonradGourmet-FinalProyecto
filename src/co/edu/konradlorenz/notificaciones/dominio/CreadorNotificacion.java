package co.edu.konradlorenz.notificaciones.dominio;

/**
 * «Factory Method · creador»
 */
public abstract class CreadorNotificacion {

    public void notificar(String destino, String contenido) {
        // Factory Method: la subclase decide qué producto crear; aquí solo se usa
        crearNotificacion().enviar(destino, contenido);
    }

    protected abstract Notificacion crearNotificacion();

}
