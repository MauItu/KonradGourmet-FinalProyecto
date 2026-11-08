package co.edu.konradlorenz.cotizaciones.puertos.salida;

public interface AlmacenArchivosPort {

    String guardar(String nombre, byte[] contenido);

    byte[] obtener(String url);

}
