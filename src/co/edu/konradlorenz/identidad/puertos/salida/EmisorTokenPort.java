package co.edu.konradlorenz.identidad.puertos.salida;

import co.edu.konradlorenz.identidad.dominio.Usuario;

/**
 * Firma el token de acceso del usuario (DEC-62). El núcleo no conoce la librería de JWT ni la llave.
 */
public interface EmisorTokenPort {

    String emitir(Usuario u);

}
