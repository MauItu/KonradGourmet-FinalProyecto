package co.edu.konradlorenz.inventario.dominio;

import java.math.BigDecimal;
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

    public Map<Long, BigDecimal> consumoPara(int cantidad) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
