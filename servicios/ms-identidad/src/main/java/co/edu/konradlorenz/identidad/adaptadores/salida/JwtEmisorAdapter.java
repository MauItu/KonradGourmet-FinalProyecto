package co.edu.konradlorenz.identidad.adaptadores.salida;

import co.edu.konradlorenz.identidad.configuracion.LlaveFirma;
import co.edu.konradlorenz.identidad.dominio.ElementoMenu;
import co.edu.konradlorenz.identidad.dominio.Usuario;
import co.edu.konradlorenz.identidad.puertos.salida.EmisorTokenPort;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * «JWT · RS256»
 *
 * Nota del diagrama: Firma el JWT con la llave privada de ms-identidad (RS256). El token lleva el id,
 * el correo, el nombre, el rol y la sucursal; el gateway valida la firma con la llave pública que
 * ms-identidad publica (JWKS), y nadie más tiene la llave privada (DEC-62).
 *
 * También lleva el menú lateral (§8.3 del reporte), para que la SPA lo pinte sin otra llamada.
 */
@Component
public class JwtEmisorAdapter implements EmisorTokenPort {

    private final JwtEncoder firmador;
    private final Duration vigencia;

    public JwtEmisorAdapter(LlaveFirma llave, @Value("${konrad.jwt.vigencia:PT10H}") Duration vigencia) {
        this.firmador = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(llave.rsa())));
        this.vigencia = vigencia;
    }

    @Override
    public String emitir(Usuario u) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("konrad-gourmet/ms-identidad")
                .subject(String.valueOf(u.id()))
                .issuedAt(ahora)
                .expiresAt(ahora.plus(vigencia))
                .claim("correo", u.correo())
                .claim("nombre", u.nombre())
                .claim("rol", u.rol())
                .claim("sucursalId", u.sucursal().id())
                .claim("sucursal", u.sucursal().nombre())
                .claim("menu", menu(u.construirMenu()))
                .build();
        JwsHeader cabecera = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return firmador.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
    }

    private static Map<String, Object> menu(ElementoMenu e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("titulo", e.titulo());
        m.put("ruta", e.ruta());
        List<Map<String, Object>> hijos = e.hijos().stream().map(JwtEmisorAdapter::menu).toList();
        m.put("hijos", hijos);
        return m;
    }

}
