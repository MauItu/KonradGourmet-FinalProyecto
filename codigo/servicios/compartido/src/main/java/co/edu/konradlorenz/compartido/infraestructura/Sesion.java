package co.edu.konradlorenz.compartido.infraestructura;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Lo que los controladores leen del JWT que firmó ms-identidad: quién es (sub), su correo y su sucursal.
 * Así el mesero o el jefe no pueden mandar otro id ni otra sucursal en el cuerpo de la petición.
 */
public final class Sesion {

    private Sesion() {
    }

    public static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    public static Long sucursalId(Jwt jwt) {
        Object s = jwt.getClaim("sucursalId");
        if (s instanceof Number n) {
            return n.longValue();
        }
        throw new IllegalArgumentException("El token no trae la sucursal");
    }

}
