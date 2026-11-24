package co.edu.konradlorenz.analitica.configuracion;

import co.edu.konradlorenz.analitica.adaptadores.salida.RedesSocialesAdapter;
import co.edu.konradlorenz.analitica.aplicacion.TableroService;
import co.edu.konradlorenz.analitica.dominio.CalculadorKpi;
import co.edu.konradlorenz.analitica.dominio.ProductoMayorDemanda;
import co.edu.konradlorenz.analitica.dominio.ProveedorMejoresPrecios;
import co.edu.konradlorenz.analitica.dominio.SucursalMayorDemanda;
import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;
import co.edu.konradlorenz.analitica.puertos.salida.KpiRepositoryPort;
import co.edu.konradlorenz.analitica.puertos.salida.SugerenciaRepositoryPort;
import co.edu.konradlorenz.analitica.puertos.salida.TendenciasPort;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

/**
 * Arma TableroService con sus constructores (el núcleo de src/ no lleva anotaciones): las tres estrategias del
 * Strategy, el cliente de la API de redes (Java puro, copiado de src/) y ShedLock para TendenciasJob.
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
public class ConfiguracionAnalitica {

    @Bean
    public List<CalculadorKpi> estrategias() {
        return List.of(new ProductoMayorDemanda(), new SucursalMayorDemanda(), new ProveedorMejoresPrecios());
    }

    @Bean
    public TendenciasPort redesSociales(@Value("${konrad.analitica.redes.url}") String url,
                                        @Value("${konrad.analitica.redes.token:}") String token) {
        return new RedesSocialesAdapter(url, token);
    }

    @Bean
    public TableroUseCase tablero(List<CalculadorKpi> estrategias, KpiRepositoryPort repositorio, TendenciasPort redes,
                                  EventPublisherPort eventos, SugerenciaRepositoryPort sugerencias) {
        return new TableroService(estrategias, repositorio, redes, eventos, sugerencias);
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
