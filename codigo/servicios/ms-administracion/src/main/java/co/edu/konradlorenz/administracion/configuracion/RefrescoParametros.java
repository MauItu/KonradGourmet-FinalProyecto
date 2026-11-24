package co.edu.konradlorenz.administracion.configuracion;

import co.edu.konradlorenz.administracion.dominio.ParametrosSistema;
import co.edu.konradlorenz.administracion.puertos.salida.ParametrosRepositoryPort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Nota n_parametros de la 4e: ParametrosSistema es la caché de la base en cada réplica, y cada réplica la refresca
 * cada minuto (10 s en el compose local, REFRESCO_PARAMETROS). Así, un parámetro que cambió otra réplica llega a
 * esta sin redespliegue. Fuera del diagrama, como CanalAlarmas en ms-notificaciones.
 */
@Component
public class RefrescoParametros {

    private final ParametrosRepositoryPort parametros;

    public RefrescoParametros(ParametrosRepositoryPort parametros) {
        this.parametros = parametros;
    }

    @Scheduled(initialDelayString = "${konrad.administracion.refresco-parametros}", fixedDelayString = "${konrad.administracion.refresco-parametros}")
    public void refrescar() {
        ParametrosSistema cache = ParametrosSistema.getInstancia();
        parametros.cargar().forEach(cache::actualizar);
    }

}
