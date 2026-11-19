package co.edu.konradlorenz.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;

/**
 * El gateway valida la firma y la vigencia del JWT en cada petición (§8.3) antes de enrutarla. Solo
 * el inicio de sesión (/api/auth/**) y el estado del propio gateway pasan sin token. El rol lo
 * comprueba cada servicio en sus endpoints.
 *
 * El WebSocket de las alarmas de V05 (/api/notificaciones/alarmas, PD-01) es la única ruta donde el token
 * puede venir en ?access_token=: el navegador no deja poner la cabecera Authorization al abrirlo. El
 * gateway lo valida al abrir la conexión y ms-notificaciones lo vuelve a validar.
 */
@Configuration
@EnableWebFluxSecurity
public class SeguridadGateway {

    static final String RUTA_ALARMAS = "/api/notificaciones/alarmas";

    @Bean
    public SecurityWebFilterChain cadena(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(a -> a
                        .pathMatchers("/api/auth/**", "/actuator/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(o -> o
                        .bearerTokenConverter(tokenDelWebSocket())
                        .jwt(j -> { })
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    static ServerAuthenticationConverter tokenDelWebSocket() {
        ServerBearerTokenAuthenticationConverter cabecera = new ServerBearerTokenAuthenticationConverter();
        ServerBearerTokenAuthenticationConverter parametro = new ServerBearerTokenAuthenticationConverter();
        parametro.setAllowUriQueryParameter(true);
        return intercambio -> (RUTA_ALARMAS.equals(intercambio.getRequest().getPath().value()) ? parametro : cabecera).convert(intercambio);
    }

}
