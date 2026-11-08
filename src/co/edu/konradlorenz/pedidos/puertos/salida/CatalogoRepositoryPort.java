package co.edu.konradlorenz.pedidos.puertos.salida;

import co.edu.konradlorenz.pedidos.dominio.CatalogoPlatos;
import co.edu.konradlorenz.pedidos.dominio.PlatoCatalogo;

/**
 * Nota del diagrama: Cada réplica de ms-pedidos lee el catálogo de su base, no de memoria: así
 * cualquier réplica atiende V03 aunque el evento lo haya consumido otra del mismo grupo.
 */
public interface CatalogoRepositoryPort {

    CatalogoPlatos catalogo(Long sucursalId);

    void guardar(PlatoCatalogo p);

}
