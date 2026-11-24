package co.edu.konradlorenz.notificaciones.configuracion;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * GET /api/notificaciones/envios?correlacion=… (DEC-76): los envíos y fallos de un recorrido, para que V14
 * marque «Correo enviado» cuando ms-notificaciones ya envió la cotización al proveedor. Solo lectura y fuera
 * del diagrama, como CierreDiaController: EnvioRepositoryPort solo registra.
 */
@RestController
public class EnviosController {

    public record EnvioJson(String destino, String tipo, LocalDateTime fechaHora, boolean exito, String error) {
    }

    private final JdbcTemplate jdbc;

    public EnviosController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/notificaciones/envios")
    @PreAuthorize("hasAnyRole('AUXILIAR_COMPRAS', 'DIRECTOR_COMPRAS', 'ADMINISTRADOR')")
    public List<EnvioJson> porCorrelacion(@RequestParam String correlacion) {
        UUID id;
        try {
            id = UUID.fromString(correlacion.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("La correlación no es válida");
        }
        return jdbc.query("SELECT destino, tipo, fecha_hora, exito, error FROM envio WHERE correlacion_id = ? ORDER BY id",
                (r, n) -> new EnvioJson(r.getString(1), r.getString(2), r.getTimestamp(3).toLocalDateTime(), r.getBoolean(4), r.getString(5)), id);
    }

}
