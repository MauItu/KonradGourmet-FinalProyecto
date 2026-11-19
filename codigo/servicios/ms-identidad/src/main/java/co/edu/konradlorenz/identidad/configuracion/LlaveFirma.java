package co.edu.konradlorenz.identidad.configuracion;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * La llave RSA con la que ms-identidad firma los JWT (DEC-62). Se crea la primera vez y se guarda en su
 * base, para que reiniciar el servicio no invalide las sesiones abiertas. Solo la pública sale del servicio.
 */
@Component
public class LlaveFirma {

    private final RSAKey llave;

    public LlaveFirma(JdbcTemplate db) {
        this.llave = cargarOCrear(db);
    }

    public RSAKey rsa() {
        return llave;
    }

    public RSAPublicKey publica() {
        try {
            return llave.toRSAPublicKey();
        } catch (com.nimbusds.jose.JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    /** El JWKS que publica GET /api/auth/jwks: solo la parte pública. */
    public JWKSet jwksPublico() {
        return new JWKSet(llave.toPublicJWK());
    }

    private static RSAKey cargarOCrear(JdbcTemplate db) {
        List<RSAKey> guardadas = db.query("SELECT kid, publica, privada FROM llave_firma ORDER BY creada DESC LIMIT 1",
                (rs, i) -> armar(rs.getString("kid"), rs.getString("publica"), rs.getString("privada")));
        if (!guardadas.isEmpty()) {
            return guardadas.get(0);
        }
        try {
            KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
            g.initialize(2048);
            KeyPair par = g.generateKeyPair();
            String kid = UUID.randomUUID().toString();
            String pub = Base64.getEncoder().encodeToString(par.getPublic().getEncoded());
            String priv = Base64.getEncoder().encodeToString(par.getPrivate().getEncoded());
            db.update("INSERT INTO llave_firma (kid, publica, privada) VALUES (?, ?, ?)", kid, pub, priv);
            return armar(kid, pub, priv);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static RSAKey armar(String kid, String pub, String priv) {
        try {
            KeyFactory f = KeyFactory.getInstance("RSA");
            RSAPublicKey publica = (RSAPublicKey) f.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pub)));
            RSAPrivateKey privada = (RSAPrivateKey) f.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(priv)));
            return new RSAKey.Builder(publica).privateKey(privada).keyID(kid).build();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("La llave guardada en llave_firma no es válida", e);
        }
    }

}
