package co.edu.konradlorenz.identidad.puertos.salida;

import co.edu.konradlorenz.identidad.dominio.Usuario;

public interface UsuarioRepositoryPort {

    Usuario buscarPorCorreo(String correo);

    Usuario guardar(Usuario u);

}
