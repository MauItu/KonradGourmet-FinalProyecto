package co.edu.konradlorenz.notificaciones.dominio;

/**
 * «Factory Method · producto»
 */
public interface Notificacion {

    void enviar(String destino, String contenido);

}
