package co.edu.konradlorenz.pedidos.dominio;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogoPlatos {

    private Long sucursalId;
    private Map<Long, PlatoCatalogo> platos = new HashMap<>();

    public CatalogoPlatos(Long sucursalId) {
        this.sucursalId = sucursalId;
    }

    public Long sucursalId() {
        return sucursalId;
    }

    /** Agrega el plato o reemplaza el que ya tenía ese id (así llega un cambio de menu.actualizado). */
    public void actualizar(PlatoCatalogo p) {
        platos.put(p.platoId(), p);
    }

    /** Precio de un plato activo; si no existe o está desactivado, lanza IllegalArgumentException. */
    public BigDecimal precioDe(Long platoId) {
        PlatoCatalogo p = platos.get(platoId);
        if (p == null || !p.activo()) {
            throw new IllegalArgumentException("El plato " + platoId + " no está disponible en el catálogo");
        }
        return p.precio();
    }

    /** Los platos activos, ordenados por nombre. */
    public List<PlatoCatalogo> vigentes() {
        return platos.values().stream()
                .filter(PlatoCatalogo::activo)
                .sorted(Comparator.comparing(PlatoCatalogo::nombre))
                .toList();
    }

}
