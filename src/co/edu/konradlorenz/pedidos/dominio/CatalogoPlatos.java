package co.edu.konradlorenz.pedidos.dominio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CatalogoPlatos {

    private Long sucursalId;
    private Map<Long, PlatoCatalogo> platos;

    public void actualizar(PlatoCatalogo p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public BigDecimal precioDe(Long platoId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public List<PlatoCatalogo> vigentes() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
