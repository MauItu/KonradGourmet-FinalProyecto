package co.edu.konradlorenz.ordenes.adaptadores.salida;

import co.edu.konradlorenz.ordenes.puertos.salida.AlmacenArchivosPort;

/**
 * «almacén de objetos (S3 compatible) · HTTPS»
 *
 * En el proyecto real: @Component.
 */
public class AlmacenObjetosAdapter implements AlmacenArchivosPort {

    @Override
    public String guardar(String nombre, byte[] contenido) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public byte[] obtener(String url) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
