package co.edu.konradlorenz.integracion.configuracion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.integracion.adaptadores.entrada.AbastecimientoSoapEndpoint;
import co.edu.konradlorenz.integracion.adaptadores.salida.MinisterioSoapClient;
import co.edu.konradlorenz.integracion.aplicacion.ConsultarPrecioService;
import co.edu.konradlorenz.integracion.dominio.PrecioEstandar;
import co.edu.konradlorenz.integracion.puertos.entrada.ConsultarPrecioUseCase;
import co.edu.konradlorenz.integracion.puertos.salida.PrecioReferenciaPort;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

/**
 * Arma lo que el núcleo no anota: el cliente SOAP del Ministerio (con su endpoint), el mapa de Redis
 * del Proxy, ConsultarPrecioService (que recibe el Proxy por ser @Primary) y la publicación del
 * endpoint SOAP en Apache CXF.
 */
@Configuration
public class ConfiguracionIntegracion {

    @Bean
    public MinisterioSoapClient ministerioSoapClient(@Value("${konrad.ministerio.endpoint}") String endpoint) {
        return new MinisterioSoapClient(endpoint);
    }

    @Bean
    public Map<Long, PrecioEstandar> cachePrecios(StringRedisTemplate redis) {
        return new MapaPreciosRedis(redis);
    }

    @Bean
    public ConsultarPrecioUseCase consultarPrecio(PrecioReferenciaPort precios, EventPublisherPort eventos) {
        return new ConsultarPrecioService(precios, eventos);
    }

    @Bean
    public AbastecimientoSoapEndpoint abastecimientoSoapEndpoint(ConsultarPrecioUseCase casoDeUso) {
        return new AbastecimientoSoapEndpoint(casoDeUso);
    }

    /** /api/integracion/abastecimiento; el WSDL anuncia la dirección del gateway, no la de la réplica. */
    @Bean
    public Endpoint endpointAbastecimiento(Bus bus, AbastecimientoSoapEndpoint implementacion,
                                           @Value("${konrad.integracion.url-publica}") String urlPublica) {
        EndpointImpl endpoint = new EndpointImpl(bus, implementacion);
        endpoint.setPublishedEndpointUrl(urlPublica + "/abastecimiento");
        endpoint.publish("/abastecimiento");
        return endpoint;
    }

}
