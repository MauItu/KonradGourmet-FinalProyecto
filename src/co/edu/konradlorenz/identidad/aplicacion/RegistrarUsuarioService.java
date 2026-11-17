package co.edu.konradlorenz.identidad.aplicacion;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.identidad.dominio.Administrador;
import co.edu.konradlorenz.identidad.dominio.AuxiliarCompras;
import co.edu.konradlorenz.identidad.dominio.DirectorComercial;
import co.edu.konradlorenz.identidad.dominio.DirectorCompras;
import co.edu.konradlorenz.identidad.dominio.JefeCocina;
import co.edu.konradlorenz.identidad.dominio.Mesero;
import co.edu.konradlorenz.identidad.dominio.PoliticaContrasena;
import co.edu.konradlorenz.identidad.dominio.Sucursal;
import co.edu.konradlorenz.identidad.dominio.Usuario;
import co.edu.konradlorenz.identidad.puertos.entrada.RegistrarUsuarioUseCase;
import co.edu.konradlorenz.identidad.puertos.salida.CifradorPort;
import co.edu.konradlorenz.identidad.puertos.salida.UsuarioRepositoryPort;

import java.util.Locale;
import java.util.regex.Pattern;

public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UsuarioRepositoryPort usuarios;
    private final CifradorPort cifrador;
    private final PoliticaContrasena politica;
    private final EventPublisherPort eventos;

    public RegistrarUsuarioService(UsuarioRepositoryPort usuarios, CifradorPort cifrador, PoliticaContrasena politica, EventPublisherPort eventos) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.politica = politica;
        this.eventos = eventos;
    }

    /**
     * Alta de un usuario por el administrador (DEC-10): valida los datos y la política de contraseña,
     * crea la subclase de Usuario que corresponde al rol, guarda solo el hash y devuelve el id.
     */
    @Override
    public Long registrar(String nombre, String correo, String rol, Long sucursalId, String clave) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        String c = correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
        if (!CORREO.matcher(c).matches()) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido.");
        }
        if (sucursalId == null) {
            throw new IllegalArgumentException("La sucursal es obligatoria.");
        }
        politica.validar(clave);
        if (usuarios.buscarPorCorreo(c) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo " + c + ".");
        }
        Usuario u = crear(rol, nombre.trim(), c, new Sucursal(sucursalId, null));
        u.asignarPasswordHash(cifrador.cifrar(clave));
        Usuario guardado = usuarios.guardar(u);
        eventos.publicar(new AuditoriaEvent("sistema", "Usuario registrado: " + c + " (" + guardado.rol() + ")"));
        return guardado.id();
    }

    /** Factory Method: el rol decide qué subclase de Usuario se crea, y con ella qué menú arma. */
    private static Usuario crear(String rol, String nombre, String correo, Sucursal sucursal) {
        String r = rol == null ? "" : rol.trim().toUpperCase(Locale.ROOT);
        return switch (r) {
            case "MESERO" -> new Mesero(null, nombre, correo, sucursal);
            case "JEFE_COCINA" -> new JefeCocina(null, nombre, correo, sucursal);
            case "AUXILIAR_COMPRAS" -> new AuxiliarCompras(null, nombre, correo, sucursal);
            case "DIRECTOR_COMPRAS" -> new DirectorCompras(null, nombre, correo, sucursal);
            case "DIRECTOR_COMERCIAL" -> new DirectorComercial(null, nombre, correo, sucursal);
            case "ADMINISTRADOR" -> new Administrador(null, nombre, correo, sucursal);
            default -> throw new IllegalArgumentException("El rol " + rol + " no existe.");
        };
    }

}
