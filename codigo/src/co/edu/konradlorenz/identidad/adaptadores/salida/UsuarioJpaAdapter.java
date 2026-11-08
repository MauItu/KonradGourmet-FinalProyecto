package co.edu.konradlorenz.identidad.adaptadores.salida;

import co.edu.konradlorenz.identidad.dominio.Usuario;
import co.edu.konradlorenz.identidad.puertos.salida.UsuarioRepositoryPort;

/**
 * En el proyecto real: @Repository.
 */
public class UsuarioJpaAdapter implements UsuarioRepositoryPort {

    @Override
    public Usuario buscarPorCorreo(String correo) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public Usuario guardar(Usuario u) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
