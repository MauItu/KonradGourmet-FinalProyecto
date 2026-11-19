package co.edu.konradlorenz.solicitudes.configuracion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.solicitudes.aplicacion.SolicitudService;
import co.edu.konradlorenz.solicitudes.puertos.entrada.GestionarSolicitudUseCase;
import co.edu.konradlorenz.solicitudes.puertos.salida.SolicitudRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Arma SolicitudService con su constructor (el núcleo de src/ no lleva anotaciones). */
@Configuration
public class ConfiguracionSolicitudes {

    @Bean
    public GestionarSolicitudUseCase gestionarSolicitud(SolicitudRepositoryPort repositorio, EventPublisherPort eventos) {
        return new SolicitudService(repositorio, eventos);
    }

}
