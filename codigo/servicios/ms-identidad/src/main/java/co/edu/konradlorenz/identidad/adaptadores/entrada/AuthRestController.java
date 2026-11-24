package co.edu.konradlorenz.identidad.adaptadores.entrada;

import co.edu.konradlorenz.identidad.dominio.ElementoMenu;
import co.edu.konradlorenz.identidad.dominio.TokenAcceso;
import co.edu.konradlorenz.identidad.puertos.entrada.IniciarSesionUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * «REST · POST /api/auth/token»
 *
 * V01: recibe el correo y la clave y responde el JWT con el menú lateral del rol. Si las credenciales
 * no coinciden responde 401 con el mismo mensaje, exista o no el correo.
 */
@RestController
public class AuthRestController {

    private final IniciarSesionUseCase casoDeUso;

    public AuthRestController(IniciarSesionUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public record Credenciales(String correo, String clave) {
    }

    public record Menu(String titulo, String ruta, List<Menu> hijos) {
        static Menu de(ElementoMenu e) {
            return new Menu(e.titulo(), e.ruta(), e.hijos().stream().map(Menu::de).toList());
        }
    }

    public record Respuesta(String jwt, Menu menu) {
    }

    // Atiende la ventana V01 de los mockups
    // POST /api/auth/token
    @PostMapping("/api/auth/token")
    public ResponseEntity<?> iniciarSesion(@RequestBody Credenciales c) {
        try {
            TokenAcceso t = casoDeUso.iniciarSesion(c.correo(), c.clave());
            return ResponseEntity.ok(new Respuesta(t.jwt(), Menu.de(t.menu())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", e.getMessage()));
        }
    }

}
