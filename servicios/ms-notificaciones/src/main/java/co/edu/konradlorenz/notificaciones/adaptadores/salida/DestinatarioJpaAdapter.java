package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.puertos.salida.DestinatarioRepositoryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * La libreta de jefes de cocina por sucursal, en ms_notificaciones. Guardar dos veces el mismo correo no lo repite.
 * Los nombres de las sucursales son la copia de la carga inicial (tabla sucursal, DEC-102).
 */
@Repository
public class DestinatarioJpaAdapter implements DestinatarioRepositoryPort {

    private final DestinatarioJpaRepository destinatarios;
    private final JdbcTemplate jdbc;

    public DestinatarioJpaAdapter(DestinatarioJpaRepository destinatarios, JdbcTemplate jdbc) {
        this.destinatarios = destinatarios;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> jefesDe(Long sucursalId) {
        return destinatarios.findBySucursalIdOrderByCorreo(sucursalId).stream().map(d -> d.correo).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> jefes() {
        return destinatarios.findAll().stream().map(d -> d.correo).distinct().sorted().toList();
    }

    @Override
    @Transactional
    public void guardar(Long sucursalId, String correo) {
        if (!destinatarios.existsBySucursalIdAndCorreo(sucursalId, correo)) {
            DestinatarioEntidad d = new DestinatarioEntidad();
            d.sucursalId = sucursalId;
            d.correo = correo;
            destinatarios.save(d);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String nombreSucursal(Long sucursalId) {
        List<String> nombre = jdbc.queryForList("SELECT nombre FROM sucursal WHERE id = ?", String.class, sucursalId);
        return nombre.isEmpty() ? "Sucursal " + sucursalId : nombre.get(0);
    }

}
