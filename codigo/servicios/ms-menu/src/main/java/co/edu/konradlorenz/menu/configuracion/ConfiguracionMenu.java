package co.edu.konradlorenz.menu.configuracion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.menu.aplicacion.PlatoService;
import co.edu.konradlorenz.menu.puertos.entrada.RegistrarPlatoUseCase;
import co.edu.konradlorenz.menu.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.menu.puertos.salida.PlatoRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/** Arma PlatoService con sus constructores (el núcleo de src/ no lleva anotaciones) y el cliente S3. */
@Configuration
public class ConfiguracionMenu {

    @Bean
    public RegistrarPlatoUseCase registrarPlato(PlatoRepositoryPort repositorio, AlmacenArchivosPort almacen, EventPublisherPort eventos) {
        return new PlatoService(repositorio, almacen, eventos);
    }

    /**
     * SeaweedFS en local (DEC-59): la misma API S3; el bucket va en la ruta, no en el nombre del host. El
     * cuerpo se manda entero y sin las sumas de verificación opcionales del SDK: con la codificación por
     * trozos firmados, SeaweedFS guardaba los encabezados de cada trozo dentro de la imagen.
     */
    @Bean
    public S3Client s3(@Value("${konrad.s3.endpoint}") String endpoint, @Value("${konrad.s3.usuario}") String usuario,
                       @Value("${konrad.s3.clave}") String clave) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .serviceConfiguration(S3Configuration.builder().chunkedEncodingEnabled(false).build())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(usuario, clave)))
                .build();
    }

}
