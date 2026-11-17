package co.edu.konradlorenz.menu.adaptadores.salida;

import co.edu.konradlorenz.menu.puertos.salida.AlmacenArchivosPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * «almacén de objetos (S3 compatible) · HTTPS»
 *
 * Nota del diagrama: La imagen del plato se guarda en el almacén de objetos (bucket imagenes-platos) y
 * el plato solo guarda su imagenUrl; menu.actualizado no la lleva.
 *
 * La dirección que devuelve es s3://bucket/llave, la misma forma que usan los PDF.
 */
@Component
public class AlmacenObjetosAdapter implements AlmacenArchivosPort {

    private final S3Client s3;
    private final String bucket;

    public AlmacenObjetosAdapter(S3Client s3, @Value("${konrad.s3.bucket-imagenes}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @Override
    public String guardar(String nombre, byte[] contenido) {
        s3.putObject(b -> b.bucket(bucket).key(nombre).contentType(tipo(nombre)), RequestBody.fromBytes(contenido));
        return "s3://" + bucket + "/" + nombre;
    }

    @Override
    public byte[] obtener(String url) {
        String prefijo = "s3://" + bucket + "/";
        if (url == null || !url.startsWith(prefijo)) {
            throw new IllegalArgumentException("La imagen no está en el almacén de ms-menu: " + url);
        }
        try {
            return s3.getObjectAsBytes(b -> b.bucket(bucket).key(url.substring(prefijo.length()))).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new IllegalArgumentException("La imagen ya no está en el almacén", e);
        }
    }

    private static String tipo(String nombre) {
        return nombre.endsWith(".png") ? "image/png" : nombre.endsWith(".webp") ? "image/webp" : "image/jpeg";
    }

}
