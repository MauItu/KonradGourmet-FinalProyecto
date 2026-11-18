package co.edu.konradlorenz.analitica.adaptadores.salida;

import co.edu.konradlorenz.analitica.dominio.EstadoSugerencia;
import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;
import co.edu.konradlorenz.analitica.puertos.salida.SugerenciaRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Las promociones sugeridas en ms_analitica (PostgreSQL). Una sugerencia nueva se inserta (una por producto y día de
 * inicio: si otra réplica ya la guardó, no se repite); una que ya existe solo cambia su estado, y solo si sigue
 * SUGERIDA: si dos jefes la resuelven a la vez, el segundo recibe «Esta sugerencia ya fue aceptada o descartada.».
 * notificadaEl(fecha) es true si ese día ya se sugirió algo: detectarTendencias() publica tendencia.detectada con las
 * primeras sugerencias del día.
 */
@Repository
public class SugerenciaJpaAdapter implements SugerenciaRepositoryPort {

    private final SugerenciaJpaRepository sugerencias;

    public SugerenciaJpaAdapter(SugerenciaJpaRepository sugerencias) {
        this.sugerencias = sugerencias;
    }

    @Override
    @Transactional
    public void guardar(SugerenciaPromocion s) {
        if (s.id() != null) {
            if (sugerencias.resolver(s.id(), s.estado().name()) == 0) {
                throw new IllegalStateException(SugerenciaPromocion.YA_RESUELTA);
            }
            return;
        }
        String producto = s.producto().length() > 120 ? s.producto().substring(0, 120) : s.producto();
        sugerencias.insertar(producto, s.motivo(), s.vigenciaDesde(), s.vigenciaHasta(), s.estado().name());
    }

    @Override
    @Transactional(readOnly = true)
    public SugerenciaPromocion porId(Long id) {
        return sugerencias.findById(id).map(SugerenciaJpaAdapter::dominio).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SugerenciaPromocion> historico(LocalDate desde, LocalDate hasta) {
        List<SugerenciaEntidad> filas = desde == null || hasta == null
                ? sugerencias.findAllByOrderByVigenciaDesdeDescIdAsc()
                : sugerencias.findByVigenciaHastaGreaterThanEqualAndVigenciaDesdeLessThanEqualOrderByVigenciaDesdeDescIdAsc(desde, hasta);
        return filas.stream().map(SugerenciaJpaAdapter::dominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean notificadaEl(LocalDate fecha) {
        return sugerencias.existsByVigenciaDesde(fecha);
    }

    private static SugerenciaPromocion dominio(SugerenciaEntidad e) {
        return new SugerenciaPromocion(e.id, e.producto, e.motivo, e.vigenciaDesde, e.vigenciaHasta, EstadoSugerencia.valueOf(e.estado));
    }

}
