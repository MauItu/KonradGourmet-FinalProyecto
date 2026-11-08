package co.edu.konradlorenz.administracion.adaptadores.salida;

import co.edu.konradlorenz.administracion.dominio.LogError;
import co.edu.konradlorenz.administracion.puertos.salida.LogsPort;

import java.util.List;

/**
 * «ELK · Elasticsearch · HTTPS»
 *
 * En el proyecto real: @Component.
 */
public class ElasticsearchLogsAdapter implements LogsPort {

    @Override
    public List<LogError> logs(String servicio) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
