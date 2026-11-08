package co.edu.konradlorenz.identidad.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.identidad.dominio.TokenAcceso;
import co.edu.konradlorenz.identidad.puertos.entrada.IniciarSesionUseCase;
import co.edu.konradlorenz.identidad.puertos.salida.CifradorPort;
import co.edu.konradlorenz.identidad.puertos.salida.UsuarioRepositoryPort;

public class IniciarSesionService implements IniciarSesionUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final CifradorPort cifrador;
    private final EventPublisherPort eventos;

    public IniciarSesionService(UsuarioRepositoryPort usuarios, CifradorPort cifrador, EventPublisherPort eventos) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.eventos = eventos;
    }

    @Override
    public TokenAcceso iniciarSesion(String correo, String clave) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
