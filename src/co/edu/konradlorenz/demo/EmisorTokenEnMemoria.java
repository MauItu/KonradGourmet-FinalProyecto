package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.identidad.dominio.Usuario;
import co.edu.konradlorenz.identidad.puertos.salida.EmisorTokenPort;

/** Emisor de la demo: un token legible y sin firma. En ms-identidad, JwtEmisorAdapter firma un JWT con RS256. */
public class EmisorTokenEnMemoria implements EmisorTokenPort {

    @Override
    public String emitir(Usuario u) {
        return "demo." + u.rol() + "." + u.correo();
    }

}
