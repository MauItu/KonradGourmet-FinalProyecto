package co.edu.konradlorenz.inventario.configuracion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.inventario.aplicacion.InventarioService;
import co.edu.konradlorenz.inventario.dominio.UmbralStock;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;
import co.edu.konradlorenz.inventario.puertos.salida.InventarioRepositoryPort;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;

/**
 * Arma InventarioService con su constructor (el núcleo de src/ no lleva anotaciones), el umbral propio
 * de ms-inventario (25 %, nota del diagrama) y ShedLock para el cierre diario.
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
public class ConfiguracionInventario {

    @Bean
    public UmbralStock umbralStock(@Value("${konrad.inventario.umbral:25}") BigDecimal porcentaje) {
        return new UmbralStock(porcentaje);
    }

    @Bean
    public GestionarInventarioUseCase gestionarInventario(InventarioRepositoryPort repositorio, UmbralStock umbral, EventPublisherPort eventos) {
        return new InventarioService(repositorio, umbral, eventos);
    }

    /** La hora del bloqueo es la de PostgreSQL, así las réplicas no dependen de su propio reloj. */
    @Bean
    public LockProvider lockProvider(JdbcTemplate db) {
        return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(db)
                .usingDbTime()
                .build());
    }

}
