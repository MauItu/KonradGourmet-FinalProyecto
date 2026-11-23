package co.edu.konradlorenz.administracion.adaptadores.entrada;

import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;
import co.edu.konradlorenz.administracion.puertos.entrada.AdministracionUseCase;
import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.infraestructura.EventosProcesados;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * «Listener · auditoria.evento»
 *
 * Guarda en la bitácora la auditoría que publican los demás servicios en cada CRUD (requisito de mantenimiento 1),
 * con el usuario que trae el evento (DEC-64). El recorrido del evento (InterceptorCorrelacion) llega hasta
 * AuditoriaJpaAdapter, que guarda su correlacionId.
 */
@Component
public class AuditoriaListener implements EventListener<EventoDominio> {

    private final AdministracionUseCase casoDeUso;
    private final EventosProcesados procesados;

    public AuditoriaListener(AdministracionUseCase casoDeUso, EventosProcesados procesados) {
        this.casoDeUso = casoDeUso;
        this.procesados = procesados;
    }

    @Override
    @Transactional
    @KafkaListener(topics = "${konrad.kafka.prefijo-topico:}auditoria.evento")
    public void alRecibir(EventoDominio evento) {
        // un evento repetido se descarta; se marca en la misma transacción que el cambio (DEC-104)
        if (!procesados.marcar(evento, "AuditoriaListener")) {
            return;
        }
        if (evento instanceof AuditoriaEvent a) {
            casoDeUso.registrarAuditoria(new RegistroAuditoria(a.accion(), a.usuario(),
                    a.ocurridoEn().toLocalDate(), a.ocurridoEn().toLocalTime()));
        }
    }

}
