package co.edu.konradlorenz.identidad.adaptadores.salida;

import co.edu.konradlorenz.identidad.puertos.salida.CifradorPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Cifra las claves con BCrypt (costo 10): el hash lleva su propia sal y nunca se guarda la clave en claro. */
@Component
public class BCryptCifradorAdapter implements CifradorPort {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String cifrar(String clave) {
        return bcrypt.encode(clave);
    }

    @Override
    public boolean coincide(String clave, String hash) {
        return bcrypt.matches(clave, hash);
    }

}
