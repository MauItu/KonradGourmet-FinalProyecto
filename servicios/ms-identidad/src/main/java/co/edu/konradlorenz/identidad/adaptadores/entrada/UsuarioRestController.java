package co.edu.konradlorenz.identidad.adaptadores.entrada;

import co.edu.konradlorenz.identidad.puertos.entrada.RegistrarUsuarioUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * «REST · POST /api/usuarios (Administrador)»
 *
 * Nota del diagrama: Alta de usuarios (sin ventana en el prototipo, supuesto S5): el administrador
 * registra al usuario con su rol y sucursal; el servicio crea la subclase de Usuario que corresponde
 * al rol, aplica la PoliticaContrasena, guarda solo el hash BCrypt, publica auditoria.evento y
 * devuelve el id, nunca el hash.
 */
@RestController
public class UsuarioRestController {

    private final RegistrarUsuarioUseCase casoDeUso;

    public UsuarioRestController(RegistrarUsuarioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public record NuevoUsuario(String nombre, String correo, String rol, Long sucursalId, String clave) {
    }

    // POST /api/usuarios
    @PostMapping("/api/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Map<String, Long> registrar(@RequestBody NuevoUsuario u) {
        return Map.of("id", casoDeUso.registrar(u.nombre(), u.correo(), u.rol(), u.sucursalId(), u.clave()));
    }

}
