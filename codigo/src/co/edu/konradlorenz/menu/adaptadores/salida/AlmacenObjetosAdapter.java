package co.edu.konradlorenz.menu.adaptadores.salida;

import co.edu.konradlorenz.menu.puertos.salida.AlmacenArchivosPort;

/**
 * «almacén de objetos (S3 compatible) · HTTPS»
 *
 * En el proyecto real: @Component.
 *
 * Nota del diagrama: La imagen del plato se guarda en el almacén de objetos (bucket imagenes-platos) y
 * el plato solo guarda su imagenUrl; menu.actualizado no la lleva.
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
