package co.edu.konradlorenz.identidad.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.identidad.dominio.PoliticaContrasena;
import co.edu.konradlorenz.identidad.puertos.entrada.RegistrarUsuarioUseCase;
import co.edu.konradlorenz.identidad.puertos.salida.CifradorPort;
import co.edu.konradlorenz.identidad.puertos.salida.UsuarioRepositoryPort;

public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

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

    @Override
    public Long registrar(String nombre, String correo, String rol, Long sucursalId, String clave) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
