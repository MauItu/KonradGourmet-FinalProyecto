package co.edu.konradlorenz.ordenes.adaptadores.salida;

import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import co.edu.konradlorenz.compartido.infraestructura.FiltroContexto;
import co.edu.konradlorenz.ordenes.configuracion.NombresProductos;
import co.edu.konradlorenz.ordenes.dominio.CotizacionOpcionada;
import co.edu.konradlorenz.ordenes.dominio.ItemOrden;
import co.edu.konradlorenz.ordenes.puertos.salida.CotizacionConsultaPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * «REST cliente → GET /api/cotizaciones/{id}»
 *
 * Nota del diagrama: CotizacionConsultaPort solo entrega cotizaciones OPCIONADA: si no, responde «Solo se
 * pueden generar órdenes de compra sobre cotizaciones opcionadas» (HU-14).
 *
 * Lee la cotización del mismo endpoint de ms-cotizaciones que usa V20, dentro de la red y sin pasar por el
 * gateway: urlCotizaciones nombra el servicio en Eureka (http://ms-cotizaciones) y el balanceador escoge la
 * instancia, como PrecioSugeridoRestAdapter en ms-cotizaciones. Lleva el JWT de la directora (ms-cotizaciones
 * vuelve a comprobar su rol) y la correlación del recorrido (DEC-64); en producción, además, mTLS (DEC-58).
 * Cada producto llega con el nombre que guardó ms-cotizaciones y se copia en NombresProductos (DEC-74).
 */
@Component
public class CotizacionRestClient implements CotizacionConsultaPort {

    static final String SOLO_OPCIONADAS = "Solo se pueden generar órdenes de compra sobre cotizaciones opcionadas";

    /** Lo que ms-cotizaciones responde en GET /api/cotizaciones/{id} y ms-ordenes usa (Jackson ignora lo demás). */
    record CotizacionJson(String numero, List<String> solicitudes, String proveedor, String correoProveedor, String estado,
                          List<ItemJson> items) {
    }

    record ItemJson(Long productoId, String producto, BigDecimal cantidad, String unidad, BigDecimal precioProveedor) {
    }

    private final String urlCotizaciones;
    private final LoadBalancerClient balanceador;
    private final NombresProductos nombres;
    private final RestClient cliente;

    public CotizacionRestClient(@Value("${konrad.cotizaciones.url}") String urlCotizaciones, LoadBalancerClient balanceador,
                                NombresProductos nombres, @Value("${konrad.cotizaciones.espera-ms:5000}") long esperaMs) {
        this.urlCotizaciones = urlCotizaciones;
        this.balanceador = balanceador;
        this.nombres = nombres;
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofMillis(esperaMs)).build());
        fabrica.setReadTimeout(Duration.ofMillis(esperaMs));
        this.cliente = RestClient.builder().requestFactory(fabrica).build();
    }

    @Override
    public CotizacionOpcionada opcionada(String numero) {
        URI logica = UriComponentsBuilder.fromUriString(urlCotizaciones).path("/api/cotizaciones/{numero}").buildAndExpand(numero).encode().toUri();
        ServiceInstance instancia = balanceador.choose(logica.getHost());
        if (instancia == null) {
            throw new IllegalStateException("ms-cotizaciones no está registrado en Eureka");
        }
        CotizacionJson c;
        try {
            c = cliente.get().uri(balanceador.reconstructURI(instancia, logica))
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
                    .retrieve().body(CotizacionJson.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("No existe la cotización " + numero);
        }
        if (c == null || !"OPCIONADA".equals(c.estado())) {
            throw new IllegalStateException(SOLO_OPCIONADAS);
        }
        Map<Long, String> productos = new LinkedHashMap<>();
        List<ItemOrden> items = c.items().stream().map(i -> {
            if (i.precioProveedor() == null) {
                throw new IllegalStateException("La cotización " + numero + " no tiene el precio del proveedor de todos sus productos");
            }
            productos.put(i.productoId(), i.producto());
            return new ItemOrden(i.productoId(), i.cantidad(), i.unidad(), i.precioProveedor());
        }).toList();
        nombres.recordar(productos);
        return new CotizacionOpcionada(c.numero(), c.solicitudes(), c.proveedor(), c.correoProveedor(), items);
    }

    /** El JWT de la petición en curso; null fuera de una petición. */
    private static String tokenActual() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a instanceof JwtAuthenticationToken jwt ? jwt.getToken().getTokenValue() : null;
    }

}
