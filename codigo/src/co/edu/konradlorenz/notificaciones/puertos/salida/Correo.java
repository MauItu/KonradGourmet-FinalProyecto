package co.edu.konradlorenz.notificaciones.puertos.salida;

import co.edu.konradlorenz.notificaciones.dominio.Mensaje;

/**
 * «Decorator · componente»
 */
public interface Correo {

    void enviar(Mensaje m);

}
