package co.edu.konradlorenz.identidad.aplicacion;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.identidad.dominio.TokenAcceso;
import co.edu.konradlorenz.identidad.dominio.Usuario;
import co.edu.konradlorenz.identidad.puertos.entrada.IniciarSesionUseCase;
import co.edu.konradlorenz.identidad.puertos.salida.CifradorPort;
import co.edu.konradlorenz.identidad.puertos.salida.EmisorTokenPort;
import co.edu.konradlorenz.identidad.puertos.salida.UsuarioRepositoryPort;

import java.util.Locale;

public class IniciarSesionService implements IniciarSesionUseCase {

    /** El mismo mensaje si el correo no existe o la clave no coincide: no revela qué correos están registrados. */
    static final String CREDENCIALES_INVALIDAS = "Correo o contraseña incorrectos.";

    private final UsuarioRepositoryPort usuarios;
    private final CifradorPort cifrador;
    private final EmisorTokenPort emisor;
    private final EventPublisherPort eventos;

    public IniciarSesionService(UsuarioRepositoryPort usuarios, CifradorPort cifrador, EmisorTokenPort emisor, EventPublisherPort eventos) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.emisor = emisor;
        this.eventos = eventos;
    }

    /**
     * V01: busca al usuario por su correo, compara la clave con el hash, firma el JWT y arma el menú
     * del rol (Factory Method + Composite). El intento, exitoso o no, queda en la auditoría.
     */
    @Override
    public TokenAcceso iniciarSesion(String correo, String clave) {
        String c = correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
        Usuario u = c.isEmpty() ? null : usuarios.buscarPorCorreo(c);
        if (u == null || clave == null || u.passwordHash() == null || !cifrador.coincide(clave, u.passwordHash())) {
            eventos.publicar(new AuditoriaEvent(c.isEmpty() ? "anónimo" : c, "Intento fallido de inicio de sesión"));
            throw new IllegalArgumentException(CREDENCIALES_INVALIDAS);
        }
        TokenAcceso token = new TokenAcceso(emisor.emitir(u), u.construirMenu());
        eventos.publicar(new AuditoriaEvent(u.correo(), "Inicio de sesión (" + u.rol() + ")"));
        return token;
    }

}
