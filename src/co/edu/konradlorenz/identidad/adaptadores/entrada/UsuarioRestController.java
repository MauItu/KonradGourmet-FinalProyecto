package co.edu.konradlorenz.identidad.adaptadores.entrada;

import co.edu.konradlorenz.identidad.puertos.entrada.RegistrarUsuarioUseCase;

/**
 * «REST · POST /api/usuarios (Administrador)»
 *
 * Nota del diagrama: Alta de usuarios (sin ventana en el prototipo, supuesto S5): el administrador
 * registra al usuario con su rol y sucursal; el servicio crea la subclase de Usuario que corresponde
 * al rol, aplica la PoliticaContrasena, guarda solo el hash BCrypt, publica auditoria.evento y
 * devuelve el id, nunca el hash.
 */
public class UsuarioRestController {

    private final RegistrarUsuarioUseCase casoDeUso;

    public UsuarioRestController(RegistrarUsuarioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    // POST /api/usuarios
    public Long registrar(String nombre, String correo, String rol, Long sucursalId, String clave) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
