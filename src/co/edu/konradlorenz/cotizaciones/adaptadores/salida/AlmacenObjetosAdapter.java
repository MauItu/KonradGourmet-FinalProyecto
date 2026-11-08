package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import co.edu.konradlorenz.cotizaciones.puertos.salida.AlmacenArchivosPort;

/**
 * «almacén de objetos (S3 compatible) · HTTPS»
 *
 * En el proyecto real: @Component.
 *
 * Nota del diagrama: El PDF se guarda en el almacén de objetos y cotizacion.enviada lleva solo pdfUrl
 * (claim check): Kafka no transporta archivos. pdf(numero) lo lee del almacén para V14.
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
