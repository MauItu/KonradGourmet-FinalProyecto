package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import co.edu.konradlorenz.compartido.infraestructura.FiltroContexto;
import co.edu.konradlorenz.cotizaciones.puertos.salida.PrecioSugeridoPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * «REST cliente → ms-integracion»
 *
 * Pide el precio de referencia al REST interno de ms-integracion (GET /interno/precios/{id}, que no pasa
 * por el gateway). urlIntegracion nombra el servicio en Eureka (http://ms-integracion) y el balanceador
 * escoge la instancia. Lleva el JWT de quien genera la cotización (el REST interno solo deja entrar a
 * compras) y la correlación del recorrido (DEC-64); en producción, además, mTLS (DEC-58). Si
 * ms-integracion no responde o el Ministerio no conoce el producto, lanza la excepción y la fachada
 * guarda la cotización como PENDIENTE_PRECIO.
 */
@Component
public class PrecioSugeridoRestAdapter implements PrecioSugeridoPort {

    private final String urlIntegracion;
    private final LoadBalancerClient balanceador;
    private final RestClient cliente;

    public PrecioSugeridoRestAdapter(@Value("${konrad.integracion.url}") String urlIntegracion, LoadBalancerClient balanceador,
                                     @Value("${konrad.integracion.espera-ms:5000}") long esperaMs) {
        this.urlIntegracion = urlIntegracion;
        this.balanceador = balanceador;
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofMillis(esperaMs)).build());
        fabrica.setReadTimeout(Duration.ofMillis(esperaMs));
        this.cliente = RestClient.builder().requestFactory(fabrica).build();
    }

    @Override
    public BigDecimal precioSugerido(Long productoId) {
        URI logica = URI.create(urlIntegracion + "/interno/precios/" + productoId);
        ServiceInstance instancia = balanceador.choose(logica.getHost());
        if (instancia == null) {
            throw new IllegalStateException("ms-integracion no está registrado en Eureka");
        }
        BigDecimal precio = cliente.get().uri(balanceador.reconstructURI(instancia, logica))
                .headers(h -> {
                    String jwt = tokenActual();
                    if (jwt != null) {
                        h.setBearerAuth(jwt);
                    }
                    ContextoRecorrido.Datos contexto = ContextoRecorrido.actual();
                    if (contexto != null && contexto.correlacionId() != null) {
                        h.set(FiltroContexto.CABECERA, contexto.correlacionId().toString());
                    }
                })
                .retrieve().body(BigDecimal.class);
        if (precio == null) {
            throw new IllegalStateException("ms-integracion no devolvió el precio del producto " + productoId);
        }
        return precio;
    }

    /** El JWT de la petición en curso; null fuera de una petición (por ejemplo, desde un listener). */
    private static String tokenActual() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a instanceof JwtAuthenticationToken jwt ? jwt.getToken().getTokenValue() : null;
    }

}
