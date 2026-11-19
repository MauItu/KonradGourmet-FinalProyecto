package co.edu.konradlorenz.pedidos.adaptadores.salida;

import co.edu.konradlorenz.pedidos.dominio.CatalogoPlatos;
import co.edu.konradlorenz.pedidos.dominio.PlatoCatalogo;
import co.edu.konradlorenz.pedidos.puertos.salida.CatalogoRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Nota del diagrama: Cada réplica de ms-pedidos lee el catálogo de su base, no de memoria: así
 * cualquier réplica atiende V03 aunque el evento lo haya consumido otra del mismo grupo.
 *
 * El catálogo trae también los platos desactivados (con activo = false), para que CatalogoPlatos
 * rechace un pedido con un plato que ya salió del menú.
 */
@Repository
public class CatalogoJpaAdapter implements CatalogoRepositoryPort {

    private final PlatoCatalogoJpaRepository catalogo;

    public CatalogoJpaAdapter(PlatoCatalogoJpaRepository catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogoPlatos catalogo(Long sucursalId) {
        CatalogoPlatos c = new CatalogoPlatos(sucursalId);
        catalogo.findBySucursalId(sucursalId).forEach(e ->
                c.actualizar(new PlatoCatalogo(e.platoId, e.sucursalId, e.nombre, e.categoria, e.precio, e.activo)));
        return c;
    }

    /** Agrega el plato o reemplaza el que tenía ese id (así llega un cambio de menu.actualizado). */
    @Override
    @Transactional
    public void guardar(PlatoCatalogo p) {
        PlatoCatalogoEntidad e = catalogo.findById(p.platoId()).orElseGet(PlatoCatalogoEntidad::new);
        e.platoId = p.platoId();
        e.sucursalId = p.sucursalId();
        e.nombre = p.nombre();
        e.categoria = p.categoria();
        e.precio = p.precio();
        e.activo = p.activo();
        catalogo.save(e);
    }

}
