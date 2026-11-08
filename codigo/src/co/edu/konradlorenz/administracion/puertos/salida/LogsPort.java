package co.edu.konradlorenz.administracion.puertos.salida;

import co.edu.konradlorenz.administracion.dominio.LogError;

import java.util.List;

public interface LogsPort {

    List<LogError> logs(String servicio);

}
