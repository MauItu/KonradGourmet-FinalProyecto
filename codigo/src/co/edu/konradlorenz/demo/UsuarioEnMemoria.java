package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.identidad.dominio.Usuario;
import co.edu.konradlorenz.identidad.puertos.salida.UsuarioRepositoryPort;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Los usuarios de ms-identidad sin base de datos. Al guardar un usuario nuevo le asigna el id siguiente,
 * como haría la secuencia de PostgreSQL.
 */
public class UsuarioEnMemoria implements UsuarioRepositoryPort {

    private final Map<String, Usuario> porCorreo = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public Usuario buscarPorCorreo(String correo) {
        return porCorreo.get(correo);
    }

    @Override
    public Usuario guardar(Usuario u) {
        Usuario guardado = u.id() != null ? u : conId(u, siguienteId++);
        porCorreo.put(guardado.correo(), guardado);
        return guardado;
    }

    public List<Usuario> todos() {
        return List.copyOf(porCorreo.values());
    }

    /** Copia el usuario con su id; cada rol es una subclase con el mismo constructor. */
    private static Usuario conId(Usuario u, long id) {
        try {
            Constructor<? extends Usuario> c = u.getClass().getConstructor(Long.class, String.class, String.class,
                    co.edu.konradlorenz.identidad.dominio.Sucursal.class);
            Usuario copia = c.newInstance(id, u.nombre(), u.correo(), u.sucursal());
            copia.asignarPasswordHash(u.passwordHash());
            return copia;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo copiar el usuario " + u.correo(), e);
        }
    }

}
