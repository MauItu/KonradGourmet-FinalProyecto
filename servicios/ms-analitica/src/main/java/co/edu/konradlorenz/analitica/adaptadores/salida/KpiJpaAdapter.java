package co.edu.konradlorenz.analitica.adaptadores.salida;

import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.puertos.salida.KpiRepositoryPort;
import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Los hechos de negocio en ms_analitica (PostgreSQL), en lugar de la lista en memoria del diagrama (DEC-30). Guarda
 * también el correlacionId del evento que dejó el hecho (DEC-64), que HechoNegocio no tiene. Sin una fecha, el rango
 * queda abierto por ese lado; sin sucursal, son todas.
 */
@Repository
public class KpiJpaAdapter implements KpiRepositoryPort {

    private static final LocalDate PRINCIPIO = LocalDate.of(2000, 1, 1);
    private static final LocalDate FIN = LocalDate.of(9999, 12, 31);

    private final HechoJpaRepository hechos;

    public KpiJpaAdapter(HechoJpaRepository hechos) {
        this.hechos = hechos;
    }

    @Override
    @Transactional
    public void guardar(HechoNegocio h) {
        HechoEntidad e = new HechoEntidad();
        e.tipo = h.tipo();
        e.sucursalId = h.sucursalId();
        e.clave = h.clave().length() > 120 ? h.clave().substring(0, 120) : h.clave();
        e.valor = h.valor();
        e.fecha = h.fecha();
        ContextoRecorrido.Datos recorrido = ContextoRecorrido.actual();
        e.correlacionId = recorrido == null ? null : recorrido.correlacionId();
        hechos.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HechoNegocio> hechos(LocalDate desde, LocalDate hasta, Long sucursalId) {
        LocalDate d = desde == null ? PRINCIPIO : desde;
        LocalDate h = hasta == null ? FIN : hasta;
        List<HechoEntidad> filas = sucursalId == null
                ? hechos.findByFechaBetweenOrderByFechaAscIdAsc(d, h)
                : hechos.findByFechaBetweenAndSucursalIdOrderByFechaAscIdAsc(d, h, sucursalId);
        return filas.stream().map(e -> new HechoNegocio(e.tipo, e.sucursalId, e.clave, e.valor, e.fecha)).toList();
    }

}
