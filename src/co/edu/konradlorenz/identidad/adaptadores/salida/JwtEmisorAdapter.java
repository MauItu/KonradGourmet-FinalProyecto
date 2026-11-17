package co.edu.konradlorenz.identidad.adaptadores.salida;

import co.edu.konradlorenz.identidad.dominio.Usuario;
import co.edu.konradlorenz.identidad.puertos.salida.EmisorTokenPort;

/**
 * «JWT · RS256»
 *
 * En el proyecto real: @Component.
 *
 * Firma el JWT con la llave privada de ms-identidad (RS256). El token lleva el id,
 * el correo, el nombre, el rol y la sucursal; el gateway valida la firma con la llave pública que
 * ms-identidad publica (JWKS), y nadie más tiene la llave privada (DEC-62).
 */
public class JwtEmisorAdapter implements EmisorTokenPort {

    @Override
    public String emitir(Usuario u) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
