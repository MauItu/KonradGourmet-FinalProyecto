package co.edu.konradlorenz.demo;

import java.util.HashMap;
import java.util.Map;

/**
 * Almacén de objetos en memoria. Lo usan los tres servicios que guardan o leen PDF, y cada uno
 * lo ve por su propio puerto AlmacenArchivosPort.
 */
public class AlmacenEnMemoria implements
        co.edu.konradlorenz.cotizaciones.puertos.salida.AlmacenArchivosPort,
        co.edu.konradlorenz.ordenes.puertos.salida.AlmacenArchivosPort,
        co.edu.konradlorenz.notificaciones.puertos.salida.AlmacenArchivosPort {

    private final Map<String, byte[]> objetos = new HashMap<>();

    @Override
    public String guardar(String nombre, byte[] contenido) {
        String url = "almacen://konradgourmet/" + nombre;
        objetos.put(url, contenido);
        return url;
    }

    @Override
    public byte[] obtener(String url) {
        byte[] contenido = objetos.get(url);
        if (contenido == null) {
            throw new IllegalArgumentException("No hay ningún objeto en " + url);
        }
        return contenido;
    }

}
