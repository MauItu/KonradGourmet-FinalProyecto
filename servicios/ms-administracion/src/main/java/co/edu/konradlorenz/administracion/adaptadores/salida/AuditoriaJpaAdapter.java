package co.edu.konradlorenz.administracion.adaptadores.salida;

import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;
import co.edu.konradlorenz.administracion.puertos.salida.AuditoriaRepositoryPort;
import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * La bitácora en ms_administracion (PostgreSQL), en lugar de la lista en memoria de Bitacora.
 *
 * Guarda también el correlacionId del recorrido (DEC-64), que RegistroAuditoria no tiene: el del evento recibido
 * (lo abre InterceptorCorrelacion) o el de la petición del administrador (FiltroContexto). Como KafkaEventPublisher
 * en los demás servicios, si el núcleo puso «sistema» y hay alguien con sesión, anota su correo: así las acciones
 * propias de ms-administracion (n_bitacora) quedan a nombre de la administradora. Un evento repetido (Kafka
 * entrega al menos una vez) no se guarda dos veces.
 */
@Repository
public class AuditoriaJpaAdapter implements AuditoriaRepositoryPort {

    private static final String SISTEMA = "sistema";

    private final AuditoriaJpaRepository auditoria;

    public AuditoriaJpaAdapter(AuditoriaJpaRepository auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public void guardar(RegistroAuditoria r) {
        ContextoRecorrido.Datos recorrido = ContextoRecorrido.actual();
        AuditoriaEntidad e = new AuditoriaEntidad();
        e.accion = r.accion();
        e.usuario = SISTEMA.equals(r.usuario()) && recorrido != null && recorrido.usuario() != null ? recorrido.usuario() : r.usuario();
        e.fecha = r.fecha();
        e.hora = r.hora().truncatedTo(ChronoUnit.MICROS); // la precisión de TIME en PostgreSQL
        e.correlacionId = recorrido == null ? null : recorrido.correlacionId();
        if (auditoria.existsByFechaAndHoraAndUsuarioAndAccionAndCorrelacionId(e.fecha, e.hora, e.usuario, e.accion, e.correlacionId)) {
            return;
        }
        try {
            auditoria.saveAndFlush(e);
        } catch (DataIntegrityViolationException repetido) {
            // otra réplica lo guardó al mismo tiempo (índice auditoria_unica)
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroAuditoria> entre(LocalDate desde, LocalDate hasta) {
        return auditoria.findByFechaBetweenOrderByFechaDescHoraDescIdDesc(desde, hasta).stream()
                .map(e -> new RegistroAuditoria(e.accion, e.usuario, e.fecha, e.hora))
                .toList();
    }

}
