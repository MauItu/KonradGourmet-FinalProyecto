package co.edu.konradlorenz.inventario.dominio;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Nota del diagrama: pedido.registrado trae líneas (platoId, cantidad). ms-inventario las multiplica
 * por su copia local de cada receta, que recibe por menu.actualizado, y acumula el consumo de insumos
 * por sucursal (acumular). La receta viaja en la unidad del insumo de inventario: ms-menu convierte al
 * publicar (por ejemplo, de gramos a kilos), así el descuento es exacto.
 */
public class RecetaPlato {

    private Long platoId;
    private Map<Long, BigDecimal> insumos;

    public RecetaPlato(Long platoId, Map<Long, BigDecimal> insumos) {
        this.platoId = platoId;
        this.insumos = insumos;
    }

    public Long platoId() {
        return platoId;
    }

    public Map<Long, BigDecimal> insumos() {
        return insumos;
    }

    public Map<Long, BigDecimal> consumoPara(int cantidad) {
        Map<Long, BigDecimal> consumo = new LinkedHashMap<>();
        insumos.forEach((productoId, porPlato) -> consumo.put(productoId, porPlato.multiply(BigDecimal.valueOf(cantidad))));
        return consumo;
    }

}
