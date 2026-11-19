package co.edu.konradlorenz.menu.puertos.salida;

/**
 * Guarda y lee la imagen de cada plato en el almacén de objetos (V06).
 */
public interface AlmacenArchivosPort {

    String guardar(String nombre, byte[] contenido);

    byte[] obtener(String url);

}
