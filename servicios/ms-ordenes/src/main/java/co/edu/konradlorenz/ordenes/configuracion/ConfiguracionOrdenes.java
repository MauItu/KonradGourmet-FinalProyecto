package co.edu.konradlorenz.ordenes.configuracion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.ordenes.aplicacion.OrdenCompraService;
import co.edu.konradlorenz.ordenes.puertos.entrada.GenerarOrdenUseCase;
import co.edu.konradlorenz.ordenes.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.ordenes.puertos.salida.CotizacionConsultaPort;
import co.edu.konradlorenz.ordenes.puertos.salida.GeneradorPdfPort;
import co.edu.konradlorenz.ordenes.puertos.salida.OrdenRepositoryPort;
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

/** Arma el servicio de aplicación con su constructor (el núcleo de src/ no lleva anotaciones) y el cliente S3. */
@Configuration
public class ConfiguracionOrdenes {

    @Bean
    public GenerarOrdenUseCase generarOrden(OrdenRepositoryPort repositorio, CotizacionConsultaPort cotizaciones, GeneradorPdfPort pdf,
                                            AlmacenArchivosPort almacen, EventPublisherPort eventos) {
        return new OrdenCompraService(repositorio, cotizaciones, pdf, almacen, eventos);
    }

    /**
     * SeaweedFS en local (DEC-59), igual que en ms-cotizaciones: el bucket va en la ruta y el cuerpo se manda
     * entero y sin las sumas opcionales del SDK, o SeaweedFS guarda los encabezados de los trozos dentro del PDF.
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
