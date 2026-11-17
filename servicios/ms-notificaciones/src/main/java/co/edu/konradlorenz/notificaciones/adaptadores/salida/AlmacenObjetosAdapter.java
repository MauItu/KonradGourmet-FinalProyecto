package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.puertos.salida.AlmacenArchivosPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * «almacén de objetos (S3 compatible) · HTTPS»
 *
 * Lee el PDF que guardaron ms-cotizaciones (cotizaciones/) o ms-ordenes (ordenes/) en el bucket
 * documentos-pdf: el evento solo trae la dirección s3://bucket/llave (claim check). Solo lee de ese bucket.
 */
@Component
public class AlmacenObjetosAdapter implements AlmacenArchivosPort {

    private final S3Client s3;
    private final String bucket;

    public AlmacenObjetosAdapter(S3Client s3, @Value("${konrad.s3.bucket-documentos}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @Override
    public byte[] obtener(String url) {
        String prefijo = "s3://" + bucket + "/";
        if (url == null || !url.startsWith(prefijo) || url.length() == prefijo.length() || url.contains("..")) {
            throw new IllegalArgumentException("El adjunto no está en el almacén de documentos: " + url);
        }
        try {
            return s3.getObjectAsBytes(b -> b.bucket(bucket).key(url.substring(prefijo.length()))).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new IllegalArgumentException("El PDF ya no está en el almacén: " + url, e);
        }
    }

}
