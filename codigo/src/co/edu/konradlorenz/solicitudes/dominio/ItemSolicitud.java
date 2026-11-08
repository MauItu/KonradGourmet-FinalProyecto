package co.edu.konradlorenz.solicitudes.dominio;

import java.math.BigDecimal;

public class ItemSolicitud {

    private Long productoId;
    private BigDecimal cantidad;
    private String marca;
    private String unidad;
    private boolean cotizado;
    private BigDecimal precioSugerido;

    /** Un producto recién pedido: sin cotizar y sin precio sugerido. */
    public ItemSolicitud(Long productoId, BigDecimal cantidad, String marca, String unidad) {
        this(productoId, cantidad, marca, unidad, false, null);
    }

    public ItemSolicitud(Long productoId, BigDecimal cantidad, String marca, String unidad, boolean cotizado, BigDecimal precioSugerido) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.marca = marca;
        this.unidad = unidad;
        this.cotizado = cotizado;
        this.precioSugerido = precioSugerido;
    }

    public Long productoId() {
        return productoId;
    }

    public BigDecimal cantidad() {
        return cantidad;
    }

    public String marca() {
        return marca;
    }

    public String unidad() {
        return unidad;
    }

    public boolean cotizado() {
        return cotizado;
    }

    public BigDecimal precioSugerido() {
        return precioSugerido;
    }

}
