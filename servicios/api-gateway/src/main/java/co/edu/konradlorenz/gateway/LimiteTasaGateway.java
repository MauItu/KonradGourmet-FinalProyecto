package co.edu.konradlorenz.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.security.Principal;

/**
 * El límite de tasa del gateway (página 3a: «valida el JWT, limita la tasa y enruta»; DEC-104). Cada usuario
 * con sesión tiene su propio balde (el subject del JWT); sin sesión (el inicio de sesión), cuenta la IP del
 * cliente, la primera de X-Forwarded-For que pone Nginx. Los baldes viven en Redis, así que el límite es el
 * mismo con varias réplicas del gateway.
 */
@Configuration(proxyBeanMethods = false)
public class LimiteTasaGateway {

    @Bean
    public KeyResolver claveLimiteTasa() {
        return intercambio -> intercambio.getPrincipal().map(Principal::getName).filter(n -> !n.isBlank())
                .map(n -> "usuario:" + n)
                .switchIfEmpty(Mono.fromSupplier(() -> "ip:" + ip(intercambio)));
    }

    static String ip(ServerWebExchange intercambio) {
        String reenviada = intercambio.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (reenviada != null && !reenviada.isBlank()) {
            return reenviada.split(",")[0].trim();
        }
        InetSocketAddress remota = intercambio.getRequest().getRemoteAddress();
        return remota == null || remota.getAddress() == null ? "desconocida" : remota.getAddress().getHostAddress();
    }

}
