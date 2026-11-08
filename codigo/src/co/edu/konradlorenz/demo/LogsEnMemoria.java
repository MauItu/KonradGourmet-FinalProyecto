package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.administracion.dominio.LogError;
import co.edu.konradlorenz.administracion.puertos.salida.LogsPort;

import java.util.List;

/** Hace las veces de Elasticsearch: la demo no genera errores, así que no tiene nada que mostrar. */
public class LogsEnMemoria implements LogsPort {

    @Override
    public List<LogError> logs(String servicio) {
        return List.of();
    }

}
