package co.edu.konradlorenz.identidad.configuracion;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Publica la llave pública (JWKS) con la que el gateway y los demás servicios validan la firma del JWT.
 * Es infraestructura del estándar OAuth 2.0 / OIDC, no un caso de uso, por eso no está en la página 4b.
 */
@RestController
public class JwksController {

    private final LlaveFirma llave;

    public JwksController(LlaveFirma llave) {
        this.llave = llave;
    }

    @GetMapping("/api/auth/jwks")
    public Map<String, Object> jwks() {
        return llave.jwksPublico().toJSONObject();
    }

}
