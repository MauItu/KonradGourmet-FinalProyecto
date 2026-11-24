package co.edu.konradlorenz.inventario.adaptadores.entrada;

import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * «Scheduler · diario 23:59»
 *
 * Descuenta el consumo del día y publica inventario.stock-bajo por cada insumo que cruzó el umbral.
 * @SchedulerLock (ShedLock, sobre la tabla shedlock) hace que con varias réplicas lo ejecute una sola
 * (DEC-36). El bloqueo va en su propia transacción, por eso el método no es @Transactional: el
 * descuento corre dentro del candado, en una sola transacción (el inventario, el consumo pendiente y
 * los eventos de la outbox), así que si se cae a mitad no queda nada aplicado y la siguiente ejecución
 * lo hace completo, sin repetirlo (C7-9). Corre sin usuario: sus eventos dicen «sistema» y comparten un
 * correlacionId (DEC-64).
 */
@Component
public class ConsumoDiarioJob {

    private final GestionarInventarioUseCase casoDeUso;
    private final TransactionTemplate transaccion;

    public ConsumoDiarioJob(GestionarInventarioUseCase casoDeUso, PlatformTransactionManager transacciones) {
        this.casoDeUso = casoDeUso;
        this.transaccion = new TransactionTemplate(transacciones);
    }

    @Scheduled(cron = "${konrad.inventario.cierre:0 59 23 * * *}", zone = "America/Bogota")
    @SchedulerLock(name = "ConsumoDiarioJob", lockAtMostFor = "PT10M")
    public void ejecutar() {
        ContextoRecorrido.comoRecorridoNuevo(() -> transaccion.executeWithoutResult(t -> casoDeUso.descontarConsumoDiario()));
    }

}
