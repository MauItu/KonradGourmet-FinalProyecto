package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.puertos.salida.AlmacenArchivosPort;

/**
 * «almacén de objetos (S3 compatible) · HTTPS»
 */
public class AlmacenObjetosAdapter implements AlmacenArchivosPort {

    @Override
    public byte[] obtener(String url) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
