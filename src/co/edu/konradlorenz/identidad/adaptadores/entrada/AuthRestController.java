package co.edu.konradlorenz.identidad.adaptadores.entrada;

import co.edu.konradlorenz.identidad.dominio.TokenAcceso;
import co.edu.konradlorenz.identidad.puertos.entrada.IniciarSesionUseCase;

/**
 * «REST · POST /api/auth/token»
 *
 * En el proyecto real: @RestController.
 */
public class AuthRestController {

    private final IniciarSesionUseCase casoDeUso;

    public AuthRestController(IniciarSesionUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    // Atiende la ventana V01 de los mockups
    // POST /api/auth/token
    public TokenAcceso iniciarSesion(String correo, String clave) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
