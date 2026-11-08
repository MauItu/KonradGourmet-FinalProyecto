package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.administracion.puertos.salida.ParametrosRepositoryPort;

import java.util.HashMap;
import java.util.Map;

/** Los parámetros del sistema en un mapa, en vez de la tabla de ms-administracion. */
public class ParametrosEnMemoria implements ParametrosRepositoryPort {

    private final Map<String, String> datos = new HashMap<>();

    @Override
    public Map<String, String> cargar() {
        return new HashMap<>(datos);
    }

    @Override
    public void guardar(String clave, String valor) {
        datos.put(clave, valor);
    }

}
