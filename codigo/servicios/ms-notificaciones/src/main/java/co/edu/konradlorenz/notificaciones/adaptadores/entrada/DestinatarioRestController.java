package co.edu.konradlorenz.notificaciones.adaptadores.entrada;

import co.edu.konradlorenz.notificaciones.puertos.entrada.NotificarUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * «REST · /api/notificaciones/destinatarios (Administrador)»
 *
 * La libreta de jefes de cocina por sucursal, que reciben la alarma y el correo de stock bajo. La ventana
 * del administrador llega en el corte 6; mientras, la base empieza con el jefe de la demo (Sucursal Centro).
 */
@RestController
@RequestMapping("/api/notificaciones/destinatarios")
public class DestinatarioRestController {

    private final NotificarUseCase casoDeUso;

    public DestinatarioRestController(NotificarUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public record Destinatario(Long sucursalId, String correo) {
    }

    // POST /api/notificaciones/destinatarios
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional // el cambio y sus eventos en la outbox, juntos (DEC-104)
    public void registrar(@RequestBody Destinatario d) {
        if (d == null || d.sucursalId() == null) {
            throw new IllegalArgumentException("Indica la sucursal del destinatario");
        }
        casoDeUso.registrarDestinatario(d.sucursalId(), d.correo() == null ? null : d.correo().trim().toLowerCase());
    }

    // GET /api/notificaciones/destinatarios?sucursalId=
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<String> consultar(@RequestParam Long sucursalId) {
        return casoDeUso.destinatarios(sucursalId);
    }

}
