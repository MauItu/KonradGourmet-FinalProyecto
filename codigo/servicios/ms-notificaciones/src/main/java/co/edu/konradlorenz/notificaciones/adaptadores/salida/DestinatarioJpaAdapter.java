package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.puertos.salida.DestinatarioRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** La libreta de jefes de cocina por sucursal, en ms_notificaciones. Guardar dos veces el mismo correo no lo repite. */
@Repository
public class DestinatarioJpaAdapter implements DestinatarioRepositoryPort {

    private final DestinatarioJpaRepository destinatarios;

    public DestinatarioJpaAdapter(DestinatarioJpaRepository destinatarios) {
        this.destinatarios = destinatarios;
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

}
