package co.edu.konradlorenz.notificaciones.dominio;

/**
 * «Factory Method · creador»
 */
public abstract class CreadorNotificacion {

    public void notificar(String destino, String contenido) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    protected abstract Notificacion crearNotificacion();

}
