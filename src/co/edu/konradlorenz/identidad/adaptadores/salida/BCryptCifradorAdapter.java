package co.edu.konradlorenz.identidad.adaptadores.salida;

import co.edu.konradlorenz.identidad.puertos.salida.CifradorPort;

/**
 * En el proyecto real: @Component.
 */
public class BCryptCifradorAdapter implements CifradorPort {

    @Override
    public String cifrar(String clave) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public boolean coincide(String clave, String hash) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
