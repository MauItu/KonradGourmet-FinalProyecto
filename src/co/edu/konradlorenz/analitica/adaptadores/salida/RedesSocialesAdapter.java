package co.edu.konradlorenz.analitica.adaptadores.salida;

import co.edu.konradlorenz.analitica.puertos.salida.TendenciasPort;

import java.util.List;

/**
 * «HTTPS → APIs de redes»
 *
 * En el proyecto real: @Component.
 */
public class RedesSocialesAdapter implements TendenciasPort {

    @Override
    public List<String> productosEnTendencia() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
