package co.edu.konradlorenz.administracion.configuracion;

import co.edu.konradlorenz.administracion.aplicacion.AdministracionService;
import co.edu.konradlorenz.administracion.puertos.entrada.AdministracionUseCase;
import co.edu.konradlorenz.administracion.puertos.salida.AuditoriaRepositoryPort;
import co.edu.konradlorenz.administracion.puertos.salida.LogsPort;
import co.edu.konradlorenz.administracion.puertos.salida.ParametrosRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Arma el servicio de aplicación con sus constructores (el núcleo de src/ no lleva anotaciones). */
@Configuration
public class ConfiguracionAdministracion {

    @Bean
    public AdministracionUseCase administracion(AuditoriaRepositoryPort auditoria, LogsPort logs, ParametrosRepositoryPort parametros) {
        return new AdministracionService(auditoria, logs, parametros);
    }

}
