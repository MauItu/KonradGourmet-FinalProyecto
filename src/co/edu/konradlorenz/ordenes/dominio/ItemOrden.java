package co.edu.konradlorenz.ordenes.dominio;

import java.math.BigDecimal;

/** El precio es por unidad: el valor de la línea es cantidad × precio. */
public class ItemOrden {

    private Long productoId;
    private BigDecimal cantidad;
    private String unidad;
    private BigDecimal precio;

    public ItemOrden(Long productoId, BigDecimal cantidad, String unidad, BigDecimal precio) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.precio = precio;
    }

    public Long productoId() {
        return productoId;
    }

    public BigDecimal cantidad() {
        return cantidad;
    }

    public String unidad() {
        return unidad;
    }

    public BigDecimal precio() {
        return precio;
    }

}
