package co.edu.konradlorenz.identidad.puertos.entrada;

import co.edu.konradlorenz.identidad.dominio.TokenAcceso;

public interface IniciarSesionUseCase {

    TokenAcceso iniciarSesion(String correo, String clave);

}
