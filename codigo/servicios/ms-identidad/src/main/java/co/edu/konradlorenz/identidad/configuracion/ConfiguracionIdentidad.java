package co.edu.konradlorenz.identidad.configuracion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.identidad.aplicacion.IniciarSesionService;
import co.edu.konradlorenz.identidad.aplicacion.RegistrarUsuarioService;
import co.edu.konradlorenz.identidad.dominio.PoliticaContrasena;
import co.edu.konradlorenz.identidad.puertos.entrada.IniciarSesionUseCase;
import co.edu.konradlorenz.identidad.puertos.entrada.RegistrarUsuarioUseCase;
import co.edu.konradlorenz.identidad.puertos.salida.CifradorPort;
import co.edu.konradlorenz.identidad.puertos.salida.EmisorTokenPort;
import co.edu.konradlorenz.identidad.puertos.salida.UsuarioRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * La clase de configuración que el README anunciaba: arma los servicios de aplicación con sus
 * constructores. El núcleo (src/) no lleva anotaciones de Spring.
 */
@Configuration
public class ConfiguracionIdentidad {

    @Bean
    public PoliticaContrasena politicaContrasena() {
        return new PoliticaContrasena();
    }

    @Bean
    public IniciarSesionUseCase iniciarSesion(UsuarioRepositoryPort usuarios, CifradorPort cifrador,
                                              EmisorTokenPort emisor, EventPublisherPort eventos) {
        return new IniciarSesionService(usuarios, cifrador, emisor, eventos);
    }

    @Bean
    public RegistrarUsuarioUseCase registrarUsuario(UsuarioRepositoryPort usuarios, CifradorPort cifrador,
                                                    PoliticaContrasena politica, EventPublisherPort eventos) {
        return new RegistrarUsuarioService(usuarios, cifrador, politica, eventos);
    }

    /** ms-identidad valida sus propios tokens con la llave pública local, sin llamarse a sí mismo. */
    @Bean
    public JwtDecoder jwtDecoder(LlaveFirma llave) {
        return NimbusJwtDecoder.withPublicKey(llave.publica()).build();
    }

}
