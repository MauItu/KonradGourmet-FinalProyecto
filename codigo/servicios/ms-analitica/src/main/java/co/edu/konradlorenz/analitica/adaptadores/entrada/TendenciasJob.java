package co.edu.konradlorenz.analitica.adaptadores.entrada;

import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;
import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * «Scheduler · semanal»
 *
 * Cada lunes a las 6:00 de Bogotá (en el compose local, cada 30 s: TENDENCIAS) analiza las tendencias y sugiere
 * promociones (PD-04). @SchedulerLock (ShedLock, sobre la tabla shedlock) hace que con varias réplicas lo ejecute una
 * sola (DEC-36). Corre sin usuario: la auditoría y tendencia.detectada dicen «sistema» y comparten un correlacionId
 * (DEC-64). Si el análisis falla (por ejemplo, la API de redes no responde), el error queda en el log —y en
 * Elasticsearch, DEC-84— y el resto del sistema sigue (HU-19); la siguiente ejecución lo vuelve a intentar. Las
 * sugerencias y sus eventos (outbox) se guardan en una sola transacción (DEC-104): si falla, no queda nada a medias.
 */
@Component
public class TendenciasJob {

    private static final Logger LOG = LoggerFactory.getLogger(TendenciasJob.class);

    private final TableroUseCase casoDeUso;
    private final TransactionTemplate transaccion;

    public TendenciasJob(TableroUseCase casoDeUso, PlatformTransactionManager transacciones) {
        this.casoDeUso = casoDeUso;
        this.transaccion = new TransactionTemplate(transacciones);
    }

    @Scheduled(cron = "${konrad.analitica.tendencias}", zone = "America/Bogota")
    @SchedulerLock(name = "TendenciasJob", lockAtMostFor = "PT10M")
    public void ejecutar() {
        ContextoRecorrido.comoRecorridoNuevo(() -> {
            try {
                transaccion.executeWithoutResult(t -> casoDeUso.detectarTendencias());
            } catch (RuntimeException e) {
                LOG.error("Falló el análisis de tendencias de promociones: {}", e.getMessage(), e);
            }
        });
    }

}
