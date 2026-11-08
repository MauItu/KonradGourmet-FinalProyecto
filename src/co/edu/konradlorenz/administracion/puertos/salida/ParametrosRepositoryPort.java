package co.edu.konradlorenz.administracion.puertos.salida;

import java.util.Map;

public interface ParametrosRepositoryPort {

    Map<String, String> cargar();

    void guardar(String clave, String valor);

}
