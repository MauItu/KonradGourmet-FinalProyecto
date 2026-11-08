package co.edu.konradlorenz.analitica.adaptadores.salida;

import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.puertos.salida.KpiRepositoryPort;

import java.time.LocalDate;
import java.util.List;

/**
 * En el proyecto real: @Repository.
 *
 * En el esqueleto, «hechos» simula la tabla en memoria; en el proyecto real este adaptador
 * usa un repositorio JPA sobre la base de datos de ms-analitica.
 */
public class KpiJpaAdapter implements KpiRepositoryPort {

    private final List<HechoNegocio> hechos;

    public KpiJpaAdapter(List<HechoNegocio> hechos) {
        this.hechos = hechos;
    }

    @Override
    public void guardar(HechoNegocio h) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<HechoNegocio> hechos(LocalDate desde, LocalDate hasta, Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
