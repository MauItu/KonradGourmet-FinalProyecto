package co.edu.konradlorenz.administracion.adaptadores.salida;

import co.edu.konradlorenz.administracion.puertos.salida.ParametrosRepositoryPort;

import java.util.Map;

public class ParametrosJpaAdapter implements ParametrosRepositoryPort {

    @Override
    public Map<String, String> cargar() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void guardar(String clave, String valor) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
