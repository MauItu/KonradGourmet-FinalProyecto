package co.edu.konradlorenz.analitica.adaptadores.salida;

import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;
import co.edu.konradlorenz.analitica.puertos.salida.SugerenciaRepositoryPort;

import java.time.LocalDate;
import java.util.List;

/**
 * En el proyecto real: @Repository.
 */
public class SugerenciaJpaAdapter implements SugerenciaRepositoryPort {

    @Override
    public void guardar(SugerenciaPromocion s) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public SugerenciaPromocion porId(Long id) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<SugerenciaPromocion> historico(LocalDate desde, LocalDate hasta) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public boolean notificadaEl(LocalDate fecha) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
