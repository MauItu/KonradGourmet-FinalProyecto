package co.edu.konradlorenz.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Cada petición que entra por el gateway lleva X-Correlation-Id (DEC-64): si la SPA no la manda, el
 * gateway crea una. El servicio la usa como correlacionId de los eventos que publique y la devuelve en
 * la respuesta (FiltroContexto), para poder buscar el recorrido en los logs.
 */
@Component
public class CorrelacionGateway implements GlobalFilter, Ordered {

    static final String CABECERA = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange intercambio, GatewayFilterChain cadena) {
        String recibida = intercambio.getRequest().getHeaders().getFirst(CABECERA);
        String id = valida(recibida) ? recibida : UUID.randomUUID().toString();
        return cadena.filter(intercambio.mutate()
                .request(r -> r.headers(h -> h.set(CABECERA, id)))
                .build());
    }

    private static boolean valida(String texto) {
        if (texto == null) {
            return false;
        }
        try {
            UUID.fromString(texto);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
