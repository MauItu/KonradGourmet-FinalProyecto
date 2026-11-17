package co.edu.konradlorenz.compartido.infraestructura;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Guarda por petición el correo del JWT y el X-Correlation-Id (DEC-64). Corre después de la cadena de
 * Spring Security, que ya validó el token. El gateway crea la cabecera si el navegador no la manda; si
 * aun así falta (una llamada directa al servicio), la petición abre un recorrido nuevo.
 */
public class FiltroContexto extends OncePerRequestFilter {

    public static final String CABECERA = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        UUID correlacion = ContextoRecorrido.uuid(peticion.getHeader(CABECERA));
        if (correlacion == null) {
            correlacion = UUID.randomUUID();
        }
        ContextoRecorrido.abrir(correoAutenticado(), correlacion);
        respuesta.setHeader(CABECERA, correlacion.toString());
        try {
            cadena.doFilter(peticion, respuesta);
        } finally {
            ContextoRecorrido.cerrar();
        }
    }

    /** El claim «correo» del JWT (lo firma ms-identidad); null si la petición no trae token. */
    static String correoAutenticado() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a instanceof JwtAuthenticationToken jwt) {
            String correo = jwt.getToken().getClaimAsString("correo");
            return correo != null ? correo : jwt.getName();
        }
        return null;
    }

}
