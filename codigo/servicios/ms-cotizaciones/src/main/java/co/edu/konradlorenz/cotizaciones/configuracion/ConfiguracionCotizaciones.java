package co.edu.konradlorenz.cotizaciones.configuracion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.cotizaciones.aplicacion.CotizacionService;
import co.edu.konradlorenz.cotizaciones.aplicacion.GenerarCotizacionFacade;
import co.edu.konradlorenz.cotizaciones.aplicacion.ProveedorService;
import co.edu.konradlorenz.cotizaciones.dominio.ReglaValidacion;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarCotizacionUseCase;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarProveedorUseCase;
import co.edu.konradlorenz.cotizaciones.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.CotizacionRepositoryPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.GeneradorPdfPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.PrecioSugeridoPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.ProveedorRepositoryPort;
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

/**
 * Arma los servicios de aplicación con sus constructores (el núcleo de src/ no lleva anotaciones): la
 * fachada, la cadena de reglas (Rechazada → Sospechosa → Opcionada) y los dos casos de uso. Y el cliente S3.
 */
@Configuration
public class ConfiguracionCotizaciones {

    @Bean
    public ReglaValidacion reglasValidacion() {
        return ReglaValidacion.cadenaPorDefecto();
    }

    @Bean
    public GenerarCotizacionFacade generarCotizacionFacade(CotizacionRepositoryPort repositorio, PrecioSugeridoPort precios,
                                                           GeneradorPdfPort pdf, AlmacenArchivosPort almacen, EventPublisherPort eventos) {
        return new GenerarCotizacionFacade(repositorio, precios, pdf, almacen, eventos);
    }

    @Bean
    public GestionarCotizacionUseCase gestionarCotizacion(GenerarCotizacionFacade fachada, CotizacionRepositoryPort repositorio,
                                                          ReglaValidacion reglas, AlmacenArchivosPort almacen, EventPublisherPort eventos) {
        return new CotizacionService(fachada, repositorio, reglas, almacen, eventos);
    }

    @Bean
    public GestionarProveedorUseCase gestionarProveedor(ProveedorRepositoryPort repositorio, EventPublisherPort eventos) {
        return new ProveedorService(repositorio, eventos);
    }

    /**
     * SeaweedFS en local (DEC-59), igual que en ms-menu: el bucket va en la ruta y el cuerpo se manda entero
     * y sin las sumas opcionales del SDK, o SeaweedFS guarda los encabezados de los trozos dentro del PDF.
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
