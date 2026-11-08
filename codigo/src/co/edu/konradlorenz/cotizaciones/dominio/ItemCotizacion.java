package co.edu.konradlorenz.cotizaciones.dominio;

import java.math.BigDecimal;

/** Los dos precios son por unidad (por kg, por litro...); el valor de la línea es cantidad × precio. */
public class ItemCotizacion {

    private Long productoId;
    private BigDecimal cantidad;
    private String unidad;
    private BigDecimal precioEstandar;
    private BigDecimal precioProveedor;

    /** Un producto por cotizar: todavía sin precio del Ministerio ni del proveedor. */
    public ItemCotizacion(Long productoId, BigDecimal cantidad, String unidad) {
        this(productoId, cantidad, unidad, null, null);
    }

    public ItemCotizacion(Long productoId, BigDecimal cantidad, String unidad, BigDecimal precioEstandar, BigDecimal precioProveedor) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.precioEstandar = precioEstandar;
        this.precioProveedor = precioProveedor;
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

    public BigDecimal precioEstandar() {
        return precioEstandar;
    }

    public BigDecimal precioProveedor() {
        return precioProveedor;
    }

}
