package co.edu.konradlorenz.inventario.adaptadores.entrada;

import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * «Scheduler · diario 23:59»
 *
 * Descuenta el consumo del día y publica inventario.stock-bajo por cada insumo que cruzó el umbral.
 * @SchedulerLock (ShedLock, sobre la tabla shedlock) hace que con varias réplicas lo ejecute una sola
 * (DEC-36); el bloqueo va en su propia transacción, por eso el método no es @Transactional. Si se
 * cae a mitad, la siguiente ejecución no repite lo descontado (el núcleo resta del pendiente lo que
 * aplicó). Corre sin usuario: sus eventos dicen «sistema» y comparten un correlacionId (DEC-64).
 */
@Component
public class ConsumoDiarioJob {

    private final GestionarInventarioUseCase casoDeUso;

    public ConsumoDiarioJob(GestionarInventarioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Scheduled(cron = "${konrad.inventario.cierre:0 59 23 * * *}", zone = "America/Bogota")
    @SchedulerLock(name = "ConsumoDiarioJob", lockAtMostFor = "PT10M")
    public void ejecutar() {
        ContextoRecorrido.comoRecorridoNuevo(casoDeUso::descontarConsumoDiario);
    }

}
