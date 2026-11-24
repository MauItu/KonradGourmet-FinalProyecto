package co.edu.konradlorenz.compartido.infraestructura;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Cada servicio comprueba el JWT y el rol en sus endpoints (§8.3 del reporte), además del gateway.
 * El token lo firma ms-identidad con RS256 y la llave pública se lee de su JWKS (DEC-62). El rol viaja
 * en el claim «rol» y llega como autoridad ROLE_&lt;rol&gt;, para usar @PreAuthorize("hasRole('...')").
 *
 * Rutas públicas: el chequeo de salud y lo que el servicio liste en konrad.seguridad.publicas.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableMethodSecurity
public class SeguridadServicio {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http,
                                               @Value("${konrad.seguridad.publicas:}") String[] publicas) throws Exception {
        http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> {
                a.requestMatchers("/actuator/health/**", "/actuator/health", "/actuator/info", "/error").permitAll();
                if (publicas.length > 0) {
                    a.requestMatchers(publicas).permitAll();
                }
                a.anyRequest().authenticated();
            })
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(convertidorRol())))
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder(@Value("${konrad.seguridad.jwks:http://ms-identidad:8080/api/auth/jwks}") String jwks) {
        return NimbusJwtDecoder.withJwkSetUri(jwks).build();
    }

    public static JwtAuthenticationConverter convertidorRol() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter(roles);
        c.setPrincipalClaimName("correo");
        return c;
    }

}
