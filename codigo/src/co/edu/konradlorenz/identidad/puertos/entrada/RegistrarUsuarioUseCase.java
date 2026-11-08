package co.edu.konradlorenz.identidad.puertos.entrada;

public interface RegistrarUsuarioUseCase {

    Long registrar(String nombre, String correo, String rol, Long sucursalId, String clave);

}
