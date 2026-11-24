package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import co.edu.konradlorenz.cotizaciones.puertos.salida.AlmacenArchivosPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * «almacén de objetos (S3 compatible) · HTTPS»
 *
 * Nota del diagrama: El PDF se guarda en el almacén de objetos y cotizacion.enviada lleva solo pdfUrl
 * (claim check): Kafka no transporta archivos. pdf(numero) lo lee del almacén para V14.
 *
 * Los PDF van en el bucket documentos-pdf, bajo cotizaciones/; la dirección es s3://bucket/llave, la que
 * leerá ms-notificaciones para adjuntarlo al correo del proveedor (corte 5).
 */
@Component
public class AlmacenObjetosAdapter implements AlmacenArchivosPort {

    private static final String CARPETA = "cotizaciones/";

    private final S3Client s3;
    private final String bucket;

    public AlmacenObjetosAdapter(S3Client s3, @Value("${konrad.s3.bucket-documentos}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @Override
    public String guardar(String nombre, byte[] contenido) {
        String llave = CARPETA + nombre;
        s3.putObject(b -> b.bucket(bucket).key(llave).contentType("application/pdf"), RequestBody.fromBytes(contenido));
        return "s3://" + bucket + "/" + llave;
    }

    @Override
    public byte[] obtener(String url) {
        String prefijo = "s3://" + bucket + "/" + CARPETA;
        if (url == null || !url.startsWith(prefijo)) {
            throw new IllegalArgumentException("El PDF no está en el almacén de ms-cotizaciones: " + url);
        }
        try {
            return s3.getObjectAsBytes(b -> b.bucket(bucket).key(url.substring(("s3://" + bucket + "/").length()))).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new IllegalArgumentException("El PDF ya no está en el almacén", e);
        }
    }

}
